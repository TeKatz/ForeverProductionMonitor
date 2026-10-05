package de.timo.foreverproductionmonitor.network;

import de.timo.foreverproductionmonitor.blockentity.AssemblerMatrixPatternImporter;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.ClientPatternImportFeedback;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Separate packet IDs; no existing monitor/crafting payload format is changed. */
public final class PatternImportNetwork {
    private PatternImportNetwork() {}

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(Request.TYPE, Request.CODEC, PatternImportNetwork::handleRequest);
        registrar.playToClient(Result.TYPE, Result.CODEC, PatternImportNetwork::handleResult);
    }

    public static void request(ProductionTabletItem.MonitorLink link, ResourceLocation recipeId) {
        if (link == null || recipeId == null) return;
        PacketDistributor.sendToServer(new Request(link.dimension(), link.pos(), recipeId));
    }

    private static void handleRequest(Request request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ProductionMonitorBlockEntity monitor =
                    MonitorNetwork.linkedMonitor(player, request.dimension(), request.pos());
            AssemblerMatrixPatternImporter.Result status = monitor == null
                    ? AssemblerMatrixPatternImporter.Result.INVALID_LINK
                    : AssemblerMatrixPatternImporter.upload(player, monitor, request.recipeId());
            if (!player.isRemoved()) {
                PacketDistributor.sendToPlayer(player, new Result(request.recipeId(), status));
            }
        });
    }

    private static void handleResult(Result result, IPayloadContext context) {
        ClientPatternImportFeedback.accept(result.recipeId(), result.status());
    }

    public record Request(ResourceLocation dimension, BlockPos pos, ResourceLocation recipeId)
            implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
                "forever_production_monitor", "request_pattern_import"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = StreamCodec.of(
                (buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.dimension());
                    buf.writeBlockPos(value.pos());
                    ResourceLocation.STREAM_CODEC.encode(buf, value.recipeId());
                },
                buf -> new Request(ResourceLocation.STREAM_CODEC.decode(buf), buf.readBlockPos(),
                        ResourceLocation.STREAM_CODEC.decode(buf)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Result(ResourceLocation recipeId, AssemblerMatrixPatternImporter.Result status)
            implements CustomPacketPayload {
        public static final Type<Result> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
                "forever_production_monitor", "pattern_import_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Result> CODEC = StreamCodec.of(
                (buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.recipeId());
                    buf.writeVarInt(value.status().ordinal());
                },
                buf -> {
                    ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
                    int ordinal = buf.readVarInt();
                    AssemblerMatrixPatternImporter.Result[] values = AssemblerMatrixPatternImporter.Result.values();
                    if (ordinal < 0 || ordinal >= values.length) ordinal = AssemblerMatrixPatternImporter.Result.ENCODE_FAILED.ordinal();
                    return new Result(id, values[ordinal]);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
