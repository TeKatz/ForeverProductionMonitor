package de.timo.foreverproductionmonitor.network;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.stacks.AEKey;
import appeng.me.helpers.PlayerSource;
import de.timo.foreverproductionmonitor.blockentity.CraftingDiagnostics;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.ClientCraftingDiagnosticsState;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Separate packet IDs; existing monitor/map packet formats remain unchanged. */
public final class CraftingDiagnosticsNetwork {
    private CraftingDiagnosticsNetwork() {}

    static void register(PayloadRegistrar registrar) {
        registrar.playToServer(Request.TYPE, Request.CODEC, CraftingDiagnosticsNetwork::handleRequest);
        registrar.playToClient(Response.TYPE, Response.CODEC, CraftingDiagnosticsNetwork::handleResponse);
        registrar.playToServer(CraftRequest.TYPE, CraftRequest.CODEC, CraftingDiagnosticsNetwork::handleCraftRequest);
        registrar.playToClient(CraftResult.TYPE, CraftResult.CODEC, CraftingDiagnosticsNetwork::handleCraftResult);
    }

    public static void request(ProductionTabletItem.MonitorLink link) {
        PacketDistributor.sendToServer(new Request(link.dimension(), link.pos()));
    }

    public static void startCraft(ProductionTabletItem.MonitorLink link, AEKey target, long amount) {
        if (target == null || amount <= 0) return;
        PacketDistributor.sendToServer(new CraftRequest(link.dimension(), link.pos(), target, amount));
    }

    private static void handleRequest(Request request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ProductionMonitorBlockEntity monitor =
                    MonitorNetwork.linkedMonitor(player, request.dimension(), request.pos());
            CraftingDiagnostics.Snapshot snapshot = monitor == null
                    ? new CraftingDiagnostics.Snapshot(false, false, List.of(), List.of(), List.of())
                    : CraftingDiagnostics.inspect(monitor);
            PacketDistributor.sendToPlayer(player, new Response(request.dimension(), request.pos(), snapshot));
        });
    }

    private static void handleResponse(Response response, IPayloadContext context) {
        ClientCraftingDiagnosticsState.accept(response);
    }

    private static void handleCraftRequest(CraftRequest request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (request.amount() <= 0 || request.amount() > 1_000_000_000L) {
                sendCraftResult(player, request, CraftStatus.INVALID_AMOUNT, "");
                return;
            }

            ProductionMonitorBlockEntity monitor =
                    MonitorNetwork.linkedMonitor(player, request.dimension(), request.pos());
            if (monitor == null) {
                sendCraftResult(player, request, CraftStatus.INVALID_LINK, "");
                return;
            }
            if (!monitor.isMonitorOnline() || monitor.getMainNode().getGrid() == null) {
                sendCraftResult(player, request, CraftStatus.NETWORK_OFFLINE, "");
                return;
            }

            var grid = monitor.getMainNode().getGrid();
            var crafting = grid.getCraftingService();
            if (!crafting.isCraftable(request.target())) {
                sendCraftResult(player, request, CraftStatus.NOT_CRAFTABLE, "");
                return;
            }

            var source = new PlayerSource(player, monitor);
            final var future = crafting.beginCraftingCalculation(
                    player.level(),
                    () -> source,
                    request.target(),
                    request.amount(),
                    CalculationStrategy.REPORT_MISSING_ITEMS);

            CompletableFuture.runAsync(() -> {
                try {
                    var plan = future.get();
                    player.server.execute(() -> {
                        try {
                            ProductionMonitorBlockEntity current =
                                    MonitorNetwork.linkedMonitor(player, request.dimension(), request.pos());
                            if (current == null) {
                                sendCraftResult(player, request, CraftStatus.INVALID_LINK, "");
                                return;
                            }
                            if (!current.isMonitorOnline() || current.getMainNode().getGrid() == null) {
                                sendCraftResult(player, request, CraftStatus.NETWORK_OFFLINE, "");
                                return;
                            }
                            if (plan.simulation()) {
                                sendCraftResult(player, request, CraftStatus.MISSING_INGREDIENTS, "");
                                return;
                            }

                            var currentSource = new PlayerSource(player, current);
                            var submit = current.getMainNode().getGrid().getCraftingService()
                                    .submitJob(plan, null, null, true, currentSource);
                            if (submit.successful()) {
                                sendCraftResult(player, request, CraftStatus.STARTED, "");
                            } else {
                                String detail = submit.errorCode() == null ? "" : submit.errorCode().name();
                                sendCraftResult(player, request, CraftStatus.SUBMIT_FAILED, detail);
                            }
                        } catch (Throwable error) {
                            sendCraftResult(player, request, CraftStatus.ERROR,
                                    error.getClass().getSimpleName());
                        }
                    });
                } catch (Throwable error) {
                    player.server.execute(() -> sendCraftResult(player, request, CraftStatus.ERROR,
                            error.getClass().getSimpleName()));
                }
            });
        });
    }

    private static void sendCraftResult(ServerPlayer player, CraftRequest request,
                                        CraftStatus status, String detail) {
        if (!player.isRemoved()) {
            PacketDistributor.sendToPlayer(player, new CraftResult(
                    request.dimension(), request.pos(), request.target(), request.amount(), status, detail));
        }
    }

    private static void handleCraftResult(CraftResult response, IPayloadContext context) {
        ClientCraftingDiagnosticsState.acceptCraftResult(response);
    }

    private static void writeText(RegistryFriendlyByteBuf buf, String value) {
        buf.writeUtf(value.length() > 120 ? value.substring(0, 120) : value, 120);
    }

    private static List<String> readTexts(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > 32) throw new IllegalArgumentException("Invalid diagnostics text count");
        List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) result.add(buf.readUtf(120));
        return result;
    }

    private static void writeTexts(RegistryFriendlyByteBuf buf, List<String> values) {
        buf.writeVarInt(values.size());
        for (String value : values) writeText(buf, value);
    }

    private static List<Integer> readIndexes(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > CraftingDiagnostics.MAX_PATTERNS) {
            throw new IllegalArgumentException("Invalid diagnostics index count");
        }
        List<Integer> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) result.add(buf.readVarInt());
        return result;
    }

    private static void writeIndexes(RegistryFriendlyByteBuf buf, List<Integer> values) {
        buf.writeVarInt(values.size());
        for (int value : values) buf.writeVarInt(value);
    }

    private static void writeIcons(RegistryFriendlyByteBuf buf, List<AEKey> icons) {
        buf.writeVarInt(icons.size());
        for (AEKey icon : icons) AEKey.STREAM_CODEC.encode(buf, icon);
    }

    private static List<AEKey> readIcons(RegistryFriendlyByteBuf buf) {
        int size = Response.bounded(buf.readVarInt(), 32);
        List<AEKey> icons = new ArrayList<>(size);
        for (int i = 0; i < size; i++) icons.add(AEKey.STREAM_CODEC.decode(buf));
        return icons;
    }

    public record Request(ResourceLocation dimension, BlockPos pos) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("forever_production_monitor", "request_crafting_diagnostics"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC =
                StreamCodec.of((buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.dimension());
                    buf.writeBlockPos(value.pos());
                }, buf -> new Request(ResourceLocation.STREAM_CODEC.decode(buf), buf.readBlockPos()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record CraftRequest(ResourceLocation dimension, BlockPos pos,
                               AEKey target, long amount) implements CustomPacketPayload {
        public static final Type<CraftRequest> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("forever_production_monitor", "request_crafting_start"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CraftRequest> CODEC =
                StreamCodec.of((buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.dimension());
                    buf.writeBlockPos(value.pos());
                    AEKey.STREAM_CODEC.encode(buf, value.target());
                    buf.writeVarLong(value.amount());
                }, buf -> new CraftRequest(
                        ResourceLocation.STREAM_CODEC.decode(buf),
                        buf.readBlockPos(),
                        AEKey.STREAM_CODEC.decode(buf),
                        buf.readVarLong()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public enum CraftStatus {
        STARTED,
        INVALID_AMOUNT,
        INVALID_LINK,
        NETWORK_OFFLINE,
        NOT_CRAFTABLE,
        MISSING_INGREDIENTS,
        SUBMIT_FAILED,
        ERROR
    }

    public record CraftResult(ResourceLocation dimension, BlockPos pos, AEKey target,
                              long amount, CraftStatus status, String detail)
            implements CustomPacketPayload {
        public static final Type<CraftResult> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("forever_production_monitor", "crafting_start_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CraftResult> CODEC =
                StreamCodec.of((buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.dimension());
                    buf.writeBlockPos(value.pos());
                    AEKey.STREAM_CODEC.encode(buf, value.target());
                    buf.writeVarLong(value.amount());
                    buf.writeEnum(value.status());
                    writeText(buf, value.detail());
                }, buf -> new CraftResult(
                        ResourceLocation.STREAM_CODEC.decode(buf),
                        buf.readBlockPos(),
                        AEKey.STREAM_CODEC.decode(buf),
                        buf.readVarLong(),
                        buf.readEnum(CraftStatus.class),
                        buf.readUtf(120)));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Response(ResourceLocation dimension, BlockPos pos,
                           CraftingDiagnostics.Snapshot snapshot) implements CustomPacketPayload {
        public static final Type<Response> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("forever_production_monitor", "crafting_diagnostics_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Response> CODEC =
                StreamCodec.of((buf, response) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, response.dimension());
                    buf.writeBlockPos(response.pos());
                    var data = response.snapshot();
                    buf.writeBoolean(data.online());
                    buf.writeBoolean(data.limited());
                    buf.writeVarInt(data.jobs().size());
                    for (var job : data.jobs()) {
                        writeText(buf, job.cpuName());
                        buf.writeVarLong(job.storage());
                        buf.writeVarInt(job.coProcessors());
                        writeText(buf, job.target());
                        AEKey.OPTIONAL_STREAM_CODEC.encode(buf, job.targetIcon());
                    }
                    buf.writeVarInt(data.providers().size());
                    for (var provider : data.providers()) {
                        writeText(buf, provider.name());
                        buf.writeBoolean(provider.dimension() != null && provider.pos() != null);
                        if (provider.dimension() != null && provider.pos() != null) {
                            ResourceLocation.STREAM_CODEC.encode(buf, provider.dimension());
                            buf.writeBlockPos(provider.pos());
                        }
                        buf.writeBoolean(provider.powered());
                        buf.writeBoolean(provider.channel());
                        buf.writeBoolean(provider.booted());
                        buf.writeBoolean(provider.active());
                        buf.writeVarInt(provider.priority());
                        AEKey.OPTIONAL_STREAM_CODEC.encode(buf, provider.visualIcon());
                    }
                    buf.writeVarInt(data.patterns().size());
                    for (var pattern : data.patterns()) {
                        writeText(buf, pattern.type());
                        writeText(buf, pattern.output());
                        buf.writeVarLong(pattern.amount());
                        writeTexts(buf, pattern.inputs());
                        writeTexts(buf, pattern.outputs());
                        writeIndexes(buf, pattern.providers());
                        writeIndexes(buf, pattern.dependencies());
                        buf.writeVarInt(pattern.copies());
                        buf.writeBoolean(pattern.multipleProviders());
                        buf.writeBoolean(pattern.outputVariants());
                        AEKey.STREAM_CODEC.encode(buf, pattern.outputIcon());
                        writeIcons(buf, pattern.inputIcons());
                        writeIcons(buf, pattern.outputIcons());
                    }
                }, buf -> {
                    ResourceLocation dimension = ResourceLocation.STREAM_CODEC.decode(buf);
                    BlockPos pos = buf.readBlockPos();
                    boolean online = buf.readBoolean();
                    boolean limited = buf.readBoolean();
                    int jobCount = bounded(buf.readVarInt(), CraftingDiagnostics.MAX_CPUS);
                    List<CraftingDiagnostics.Job> jobs = new ArrayList<>(jobCount);
                    for (int i = 0; i < jobCount; i++) {
                        jobs.add(new CraftingDiagnostics.Job(buf.readUtf(120), buf.readVarLong(),
                                buf.readVarInt(), buf.readUtf(120),
                                AEKey.OPTIONAL_STREAM_CODEC.decode(buf)));
                    }
                    int providerCount = bounded(buf.readVarInt(), CraftingDiagnostics.MAX_PROVIDERS);
                    List<CraftingDiagnostics.Provider> providers = new ArrayList<>(providerCount);
                    for (int i = 0; i < providerCount; i++) {
                        String name = buf.readUtf(120);
                        boolean hasLocation = buf.readBoolean();
                        ResourceLocation locationDimension = hasLocation
                                ? ResourceLocation.STREAM_CODEC.decode(buf) : null;
                        BlockPos locationPos = hasLocation ? buf.readBlockPos() : null;
                        providers.add(new CraftingDiagnostics.Provider(name, locationDimension, locationPos,
                                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                                buf.readVarInt(), AEKey.OPTIONAL_STREAM_CODEC.decode(buf)));
                    }
                    int patternCount = bounded(buf.readVarInt(), CraftingDiagnostics.MAX_PATTERNS);
                    List<CraftingDiagnostics.Pattern> patterns = new ArrayList<>(patternCount);
                    for (int i = 0; i < patternCount; i++) {
                        patterns.add(new CraftingDiagnostics.Pattern(buf.readUtf(120), buf.readUtf(120),
                                buf.readVarLong(), readTexts(buf), readTexts(buf), readIndexes(buf),
                                readIndexes(buf), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(),
                                AEKey.STREAM_CODEC.decode(buf), readIcons(buf), readIcons(buf)));
                    }
                    return new Response(dimension, pos,
                            new CraftingDiagnostics.Snapshot(online, limited, jobs, providers, patterns));
                });
        private static int bounded(int count, int max) {
            if (count < 0 || count > max) throw new IllegalArgumentException("Invalid diagnostics count");
            return count;
        }
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
