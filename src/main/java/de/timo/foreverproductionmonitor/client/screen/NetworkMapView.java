/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 */
package de.timo.foreverproductionmonitor.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.screen.InterfaceTheme;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

final class NetworkMapView {
    private static final int DETAILS_WIDTH = 184;
    private static final int VIEW_BUTTON_HEIGHT = 16;
    private static final int VIEW_BUTTON_GAP = 3;
    private static final int VIEW_TOOLBAR_HEIGHT = 70;
    private static final int HELP_AREA_HEIGHT = 30;
    private static final double DRAG_THRESHOLD_SQUARED = 12.0;
    private static final float MIN_CAMERA_PITCH = 5.0f;
    private static final float MAX_CAMERA_PITCH = 85.0f;
    private static final double MAX_SCENE_DEPTH = 4096.0;
    private static String rememberedNetwork;
    private static CameraState rememberedCamera;
    private static final Map<String, CameraState[]> CAMERA_BOOKMARKS;
    private static final Map<String, ArrayDeque<MapEvent>> EVENT_LOGS;
    private static final DateTimeFormatter EVENT_TIME;
    private final Minecraft minecraft;
    private final Font font;
    private MonitorNetwork.NetworkMapPayload snapshot;
    private int left;
    private int top;
    private int width;
    private int height;
    private float yaw = 225.0f;
    private float pitch = 30.0f;
    private double zoom = 1.0;
    private double panX;
    private double panY;
    private int hoveredIndex = -1;
    private int selectedIndex = -1;
    private int dragButton = -1;
    private double pressX;
    private double pressY;
    private boolean dragged;
    private boolean focusedOnBlock;
    private boolean heatmap;
    private boolean showEvents;
    private MapFilter filter = MapFilter.ALL;
    private String search = "";
    private int searchCursor = -1;
    private final Map<Long, Integer> cableLoads = new HashMap<Long, Integer>();
    private long lastClickTime;
    private int lastClickIndex = -1;
    private double centerX;
    private double centerY;
    private double centerZ;
    private double fitScale = 8.0;
    private double sceneDepthSpan = 2.0;

    NetworkMapView(Minecraft minecraft, Font font) {
        this.minecraft = minecraft;
        this.font = font;
    }

    void setBounds(int n, int n2, int n3, int n4) {
        this.left = n;
        this.top = n2;
        this.width = n3;
        this.height = n4;
        this.recomputeFit();
    }

    void accept(MonitorNetwork.NetworkMapPayload networkMapPayload) {
        boolean bl;
        boolean bl2 = bl = this.snapshot == null || !this.snapshot.dimension().equals((Object)networkMapPayload.dimension()) || !this.snapshot.pos().equals((Object)networkMapPayload.pos());
        if (!bl && this.snapshot != null) {
            this.recordEvents(this.snapshot, networkMapPayload);
        }
        this.snapshot = networkMapPayload;
        this.recomputeCableLoads();
        if (this.selectedIndex >= networkMapPayload.nodes().size()) {
            this.selectedIndex = -1;
        }
        if (bl) {
            String string = String.valueOf(networkMapPayload.dimension()) + "@" + networkMapPayload.pos().asLong();
            if (((Boolean)ClientConfig.VALUES.mapRememberCamera.get()).booleanValue() && string.equals(rememberedNetwork) && rememberedCamera != null) {
                this.restoreCamera(rememberedCamera);
            } else {
                this.applyDefaultView();
            }
        } else {
            this.recomputeFit();
        }
    }

    void setSearch(String string) {
        String string2;
        String string3 = string2 = string == null ? "" : string.strip().toLowerCase(Locale.ROOT);
        if (Objects.equals(this.search, string2)) {
            return;
        }
        this.search = string2;
        this.searchCursor = -1;
        if (this.selectedIndex >= 0 && this.snapshot != null && !this.shouldRender(this.snapshot.nodes().get(this.selectedIndex))) {
            this.selectedIndex = -1;
        }
        this.focusedOnBlock = false;
        this.recomputeFit();
    }

    void focusNextSearchMatch() {
        if (this.snapshot == null || this.snapshot.nodes().isEmpty()) {
            return;
        }
        int n = this.snapshot.nodes().size();
        for (int i = 1; i <= n; ++i) {
            int n2 = Math.floorMod(this.searchCursor + i, n);
            if (!this.shouldRender(this.snapshot.nodes().get(n2))) continue;
            this.searchCursor = n2;
            this.selectedIndex = n2;
            this.focusSelected();
            return;
        }
    }

    private void applyDefaultView() {
        switch ((ClientConfig.MapDefaultView)((Object)ClientConfig.VALUES.mapDefaultView.get())) {
            case ISO: {
                this.setView(225.0f, 30.0f, true);
                break;
            }
            case TOP: {
                this.setView(180.0f, 85.0f, true);
                break;
            }
            case FRONT: {
                this.setView(180.0f, 0.0f, true);
            }
        }
    }

    void resetView() {
        this.setView(225.0f, 30.0f, true);
    }

    private void fitView() {
        this.focusedOnBlock = false;
        this.panX = 0.0;
        this.panY = 0.0;
        this.zoom = 1.0;
        this.recomputeFit();
        this.rememberCamera();
    }

    private void setView(float f, float f2, boolean bl) {
        this.focusedOnBlock = false;
        this.yaw = NetworkMapView.normalizeYaw(f);
        this.pitch = NetworkMapView.clampPitch(f2);
        this.panX = 0.0;
        this.panY = 0.0;
        if (bl) {
            this.zoom = 1.0;
        }
        this.recomputeFit();
        this.rememberCamera();
    }

    void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n3 = this.sceneRight();
        guiGraphics.fill(this.left, this.top, this.left + this.width, this.top + this.height, palette.tableOuter());
        guiGraphics.fill(this.left + 1, this.top + 1, n3 - 1, this.top + this.height - 1, -15723491);
        if (((Boolean)ClientConfig.VALUES.mapShowDetails.get()).booleanValue()) {
            guiGraphics.fill(n3 + 4, this.top + 1, this.left + this.width - 1, this.top + this.height - 1, palette.rowEven());
            guiGraphics.fill(n3, this.top, n3 + 1, this.top + this.height, palette.accentA());
        }
        if (this.snapshot == null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.loading"), this.left + (n3 - this.left) / 2, this.top + this.height / 2, -14740);
            this.drawHelp(guiGraphics, n3);
            return;
        }
        if (this.snapshot.nodes().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.empty"), this.left + (n3 - this.left) / 2, this.top + this.height / 2, -7366746);
            this.drawDetails(guiGraphics);
            return;
        }
        this.updateHover(n, n2);
        this.renderScene(guiGraphics, n3);
        this.drawToolbar(guiGraphics, n, n2, n3);
        this.drawHelp(guiGraphics, n3);
        this.drawDetails(guiGraphics);
    }

    private void renderScene(GuiGraphics guiGraphics, int n) {
        int n2;
                double d = this.scale();
        double d2 = (double)(this.left + n) * 0.5 + this.panX;
        double d3 = (double)(this.sceneContentTop() + this.sceneContentBottom()) * 0.5 + this.panY;
        guiGraphics.flush();
        guiGraphics.enableScissor(this.left + 1, this.sceneContentTop(), n - 1, this.sceneContentBottom());
        RenderSystem.clearDepth((double)1.0);
        RenderSystem.clear((int)256, (boolean)Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(d2, d3, 180.0);
        float f = (float)Math.min(d, 4096.0 / this.sceneDepthSpan);
        poseStack.scale((float)d, (float)(-d), f);
        poseStack.mulPose(Axis.XP.rotationDegrees(this.pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(this.yaw));
        poseStack.translate(-this.centerX, -this.centerY, -this.centerZ);
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        List<MonitorNetwork.MapNode> list = this.snapshot.nodes();
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        for (MonitorNetwork.MapNode mapNode22 : list) {
            if (!this.shouldRender(mapNode22)) continue;
            hashSet.add(mapNode22.pos());
        }
        for (MonitorNetwork.MapNode mapNode22 : list) {
            if (!this.shouldRender(mapNode22) || mapNode22.renderKind() != ProductionMonitorBlockEntity.MapRenderKind.BLOCK) continue;
            this.renderBlock(poseStack, bufferSource, mapNode22);
        }
        bufferSource.endBatch();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.debugQuads());
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());
        for (MonitorNetwork.MapNode object2 : list) {
            if (!this.shouldRender(object2) || object2.renderKind() != ProductionMonitorBlockEntity.MapRenderKind.PART) continue;
            this.renderPartGeometry(poseStack, vertexConsumer, lineConsumer, object2, hashSet);
        }
        bufferSource.endBatch(RenderType.debugQuads());
        bufferSource.endBatch(RenderType.lines());
        if (this.heatmap) {
            VertexConsumer vertexConsumer2 = bufferSource.getBuffer(RenderType.lines());
            for (MonitorNetwork.MapNode mapNode : list) {
                if (!this.shouldRender(mapNode)) continue;
                float[] fArray = this.heatColor(mapNode);
                LevelRenderer.renderLineBox((PoseStack)poseStack, (VertexConsumer)vertexConsumer2, (AABB)NetworkMapView.boundsFor(mapNode).inflate(0.025), (float)fArray[0], (float)fArray[1], (float)fArray[2], (float)0.96f);
            }
            bufferSource.endBatch(RenderType.lines());
        }
        int n3 = n2 = this.selectedIndex >= 0 ? this.selectedIndex : this.hoveredIndex;
        if (n2 >= 0 && n2 < list.size() && this.shouldRender(list.get(n2))) {
            MonitorNetwork.MapNode mapNode = list.get(n2);
            float[] fArray = NetworkMapView.stateColor(mapNode.state());
            LevelRenderer.renderLineBox((PoseStack)poseStack, (VertexConsumer)bufferSource.getBuffer(RenderType.lines()), (AABB)NetworkMapView.boundsFor(mapNode).inflate(0.035), (float)fArray[0], (float)fArray[1], (float)fArray[2], (float)1.0f);
            bufferSource.endBatch(RenderType.lines());
        }
        poseStack.popPose();
        RenderSystem.disableDepthTest();
        guiGraphics.disableScissor();
    }

    private void renderBlock(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MonitorNetwork.MapNode mapNode) {
        BlockState blockState = Block.stateById((int)mapNode.blockStateId());
        if (NetworkMapView.isQuantumComputerBlock(blockState, mapNode)) {
            this.renderQuantumComputerBlock(poseStack, bufferSource, blockState, mapNode);
            return;
        }
        poseStack.pushPose();
        poseStack.translate((float)mapNode.pos().getX(), (float)mapNode.pos().getY(), (float)mapNode.pos().getZ());
        this.minecraft.getBlockRenderer().renderSingleBlock(blockState, poseStack, (MultiBufferSource)bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static boolean isQuantumComputerBlock(BlockState blockState, MonitorNetwork.MapNode mapNode) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        return NetworkMapView.isQuantumComputerId(resourceLocation.getNamespace(), resourceLocation.getPath()) || NetworkMapView.isQuantumComputerId(mapNode.visualId().getNamespace(), mapNode.visualId().getPath());
    }

    private static boolean isQuantumComputerId(String string, String string2) {
        return string.equals("advanced_ae") && (string2.startsWith("quantum_") || string2.equals("data_entangler"));
    }

    private void renderQuantumComputerBlock(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockState blockState, MonitorNetwork.MapNode mapNode) {
        ItemStack itemStack = new ItemStack((ItemLike)blockState.getBlock().asItem());
        if (itemStack.isEmpty()) {
            itemStack = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(mapNode.visualId()));
        }
        if (itemStack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate((double)mapNode.pos().getX() + 0.5, (double)mapNode.pos().getY() + 0.5, (double)mapNode.pos().getZ() + 0.5);
        this.minecraft.getItemRenderer().renderStatic(itemStack, ItemDisplayContext.NONE, 0xF000F0, OverlayTexture.NO_OVERLAY, poseStack, (MultiBufferSource)bufferSource, (Level)this.minecraft.level, mapNode.pos().hashCode());
        poseStack.popPose();
    }

    private void renderPartGeometry(PoseStack poseStack, VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2, MonitorNetwork.MapNode mapNode, Set<BlockPos> set) {
        float f;
        String string = mapNode.visualId().getPath();
        int n = this.heatmap ? this.heatColorInt(mapNode) : NetworkMapView.partColor(string, mapNode.state());
        float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n & 0xFF) / 255.0f;
        float f5 = f = string.contains("dense") ? 0.185f : 0.095f;
        if (NetworkMapView.isCable(string)) {
            BlockPos blockPos = mapNode.pos();
            NetworkMapView.addBox(poseStack, vertexConsumer, vertexConsumer2, new AABB((double)blockPos.getX() + 0.5 - (double)f, (double)blockPos.getY() + 0.5 - (double)f, (double)blockPos.getZ() + 0.5 - (double)f, (double)blockPos.getX() + 0.5 + (double)f, (double)blockPos.getY() + 0.5 + (double)f, (double)blockPos.getZ() + 0.5 + (double)f), f2, f3, f4, true);
            for (Direction direction : Direction.values()) {
                if (!set.contains(blockPos.relative(direction))) continue;
                NetworkMapView.addBox(poseStack, vertexConsumer, vertexConsumer2, NetworkMapView.cableArm(blockPos, direction, f), f2, f3, f4, false);
            }
        } else {
            NetworkMapView.addBox(poseStack, vertexConsumer, vertexConsumer2, NetworkMapView.partPlate(mapNode), f2, f3, f4, true);
        }
    }

    private static boolean isCable(String string) {
        return string.contains("cable") && !string.contains("bus");
    }

    private static AABB cableArm(BlockPos blockPos, Direction direction, double d) {
        double d2 = (double)blockPos.getX() + 0.5 - d;
        double d3 = (double)blockPos.getY() + 0.5 - d;
        double d4 = (double)blockPos.getZ() + 0.5 - d;
        double d5 = (double)blockPos.getX() + 0.5 + d;
        double d6 = (double)blockPos.getY() + 0.5 + d;
        double d7 = (double)blockPos.getZ() + 0.5 + d;
        return switch (direction) {
            default -> throw new IncompatibleClassChangeError();
            case Direction.DOWN -> new AABB(d2, (double)blockPos.getY(), d4, d5, d6, d7);
            case Direction.UP -> new AABB(d2, d3, d4, d5, (double)blockPos.getY() + 1.0, d7);
            case Direction.NORTH -> new AABB(d2, d3, (double)blockPos.getZ(), d5, d6, d7);
            case Direction.SOUTH -> new AABB(d2, d3, d4, d5, d6, (double)blockPos.getZ() + 1.0);
            case Direction.WEST -> new AABB((double)blockPos.getX(), d3, d4, d5, d6, d7);
            case Direction.EAST -> new AABB(d2, d3, d4, (double)blockPos.getX() + 1.0, d6, d7);
        };
    }

    private static AABB partPlate(MonitorNetwork.MapNode mapNode) {
        BlockPos blockPos = mapNode.pos();
        double d = 0.22;
        double d2 = 0.78;
        double d3 = 0.14;
        if (mapNode.side() < 0) {
            return new AABB((double)blockPos.getX() + 0.3, (double)blockPos.getY() + 0.3, (double)blockPos.getZ() + 0.3, (double)blockPos.getX() + 0.7, (double)blockPos.getY() + 0.7, (double)blockPos.getZ() + 0.7);
        }
        return switch (Direction.from3DDataValue((int)mapNode.side())) {
            default -> throw new IncompatibleClassChangeError();
            case Direction.DOWN -> new AABB((double)blockPos.getX() + d, (double)blockPos.getY(), (double)blockPos.getZ() + d, (double)blockPos.getX() + d2, (double)blockPos.getY() + d3, (double)blockPos.getZ() + d2);
            case Direction.UP -> new AABB((double)blockPos.getX() + d, (double)blockPos.getY() + 1.0 - d3, (double)blockPos.getZ() + d, (double)blockPos.getX() + d2, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + d2);
            case Direction.NORTH -> new AABB((double)blockPos.getX() + d, (double)blockPos.getY() + d, (double)blockPos.getZ(), (double)blockPos.getX() + d2, (double)blockPos.getY() + d2, (double)blockPos.getZ() + d3);
            case Direction.SOUTH -> new AABB((double)blockPos.getX() + d, (double)blockPos.getY() + d, (double)blockPos.getZ() + 1.0 - d3, (double)blockPos.getX() + d2, (double)blockPos.getY() + d2, (double)blockPos.getZ() + 1.0);
            case Direction.WEST -> new AABB((double)blockPos.getX(), (double)blockPos.getY() + d, (double)blockPos.getZ() + d, (double)blockPos.getX() + d3, (double)blockPos.getY() + d2, (double)blockPos.getZ() + d2);
            case Direction.EAST -> new AABB((double)blockPos.getX() + 1.0 - d3, (double)blockPos.getY() + d, (double)blockPos.getZ() + d, (double)blockPos.getX() + 1.0, (double)blockPos.getY() + d2, (double)blockPos.getZ() + d2);
        };
    }

    private static AABB boundsFor(MonitorNetwork.MapNode mapNode) {
        if (mapNode.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.BLOCK) {
            return new AABB(mapNode.pos());
        }
        return NetworkMapView.isCable(mapNode.visualId().getPath()) ? new AABB(mapNode.pos()).deflate(0.27) : NetworkMapView.partPlate(mapNode);
    }

    private static void addBox(PoseStack poseStack, VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2, AABB aABB, float f, float f2, float f3, boolean bl) {
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.DOWN, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.58f), (float)(f2 * 0.58f), (float)(f3 * 0.58f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.UP, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)Math.min(1.0f, f * 1.1f), (float)Math.min(1.0f, f2 * 1.1f), (float)Math.min(1.0f, f3 * 1.1f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.NORTH, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.72f), (float)(f2 * 0.72f), (float)(f3 * 0.72f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.SOUTH, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.9f), (float)(f2 * 0.9f), (float)(f3 * 0.9f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.WEST, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.66f), (float)(f2 * 0.66f), (float)(f3 * 0.66f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.EAST, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.82f), (float)(f2 * 0.82f), (float)(f3 * 0.82f), (float)1.0f);
        if (bl) {
            LevelRenderer.renderLineBox((PoseStack)poseStack, (VertexConsumer)vertexConsumer2, (AABB)aABB, (float)(f * 0.32f), (float)(f2 * 0.32f), (float)(f3 * 0.32f), (float)0.82f);
        }
    }

    private static int partColor(String string, ProductionMonitorBlockEntity.MapNodeState mapNodeState) {
        if (mapNodeState == ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL) {
            return 16741500;
        }
        if (mapNodeState == ProductionMonitorBlockEntity.MapNodeState.UNPOWERED) {
            return 16747896;
        }
        if (string.contains("light_blue")) {
            return 5740473;
        }
        if (string.contains("light_gray")) {
            return 9608098;
        }
        if (string.contains("red")) {
            return 10305093;
        }
        if (string.contains("orange")) {
            return 11955757;
        }
        if (string.contains("yellow")) {
            return 12034110;
        }
        if (string.contains("lime")) {
            return 7381564;
        }
        if (string.contains("green")) {
            return 3639371;
        }
        if (string.contains("cyan")) {
            return 3905181;
        }
        if (string.contains("blue")) {
            return 4217756;
        }
        if (string.contains("magenta")) {
            return 10177947;
        }
        if (string.contains("purple")) {
            return 7358619;
        }
        if (string.contains("pink")) {
            return 12086410;
        }
        if (string.contains("brown")) {
            return 6768691;
        }
        if (string.contains("black")) {
            return 2698548;
        }
        if (string.contains("gray")) {
            return 0x555C65;
        }
        if (string.contains("white")) {
            return 12109001;
        }
        return string.contains("dense") ? 7492763 : 4757413;
    }

    private void drawToolbar(GuiGraphics guiGraphics, int n, int n2, int n3) {
        int n4;
        int n5;
        int n6 = this.viewButtonsTop();
        int n7 = this.left + 7;
        int n8 = n3 - this.left - 14;
        int n9 = n7;
        int n10 = (n8 - 12) / 5;
        for (MapFilter n52 : MapFilter.values()) {
            this.drawToolbarButton(guiGraphics, n, n2, n9, n6, n10, (Component)Component.translatable((String)("screen.forever_production_monitor.map.filter." + n52.name().toLowerCase(Locale.ROOT))), this.filter == n52, ToolbarGroup.FILTER);
            n9 += n10 + 3;
        }
        String[] stringArray = new String[]{"fit", "iso", "top", "front"};
        int n11 = n6 + 16 + 4;
        int n12 = (n8 - 9) / 4;
        for (n5 = 0; n5 < stringArray.length; ++n5) {
            n4 = n7 + n5 * (n12 + 3);
            this.drawToolbarButton(guiGraphics, n, n2, n4, n11, n12, (Component)Component.translatable((String)("screen.forever_production_monitor.map.view." + stringArray[n5])), false, ToolbarGroup.CAMERA);
        }
        n5 = n11 + 16 + 4;
        n4 = (n8 - 12) / 5;
        n9 = n7;
        this.drawToolbarButton(guiGraphics, n, n2, n9, n5, n4, (Component)Component.translatable((String)"screen.forever_production_monitor.map.heatmap"), this.heatmap, ToolbarGroup.DIAGNOSTIC);
        this.drawToolbarButton(guiGraphics, n, n2, n9 += n4 + 3, n5, n4, (Component)Component.translatable((String)"screen.forever_production_monitor.map.events"), this.showEvents, ToolbarGroup.DIAGNOSTIC);
        n9 += n4 + 3;
        for (int i = 0; i < 3; ++i) {
            boolean bl = this.cameraBookmarks()[i] != null;
            this.drawToolbarButton(guiGraphics, n, n2, n9, n5, n4, (Component)Component.literal((String)((bl ? "\u25c6 " : "\u25c7 ") + (i + 1))), false, ToolbarGroup.BOOKMARK);
            if (n >= n9 && n < n9 + n4 && n2 >= n5 && n2 < n5 + 16) {
                guiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.bookmark_hint"), n, n2);
            }
            n9 += n4 + 3;
        }
    }

    private void drawToolbarButton(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, Component component, boolean bl, ToolbarGroup toolbarGroup) {
        int n6;
        boolean bl2 = n >= n3 && n < n3 + n5 && n2 >= n4 && n2 < n4 + 16;
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n7 = switch (toolbarGroup) {
            default -> throw new IncompatibleClassChangeError();
            case ToolbarGroup.FILTER -> -10237992;
            case ToolbarGroup.CAMERA -> palette.accentB();
            case ToolbarGroup.DIAGNOSTIC -> -2053814;
            case ToolbarGroup.BOOKMARK -> -4682787;
        };
        n6 = switch (toolbarGroup) {
            case FILTER -> -803393480;
            case CAMERA -> -802610376;
            case DIAGNOSTIC -> -801821408;
            case BOOKMARK -> -802216392;
        };
        guiGraphics.fill(n3, n4, n3 + n5, n4 + 16, bl ? NetworkMapView.withAlpha(n7, 98) : (bl2 ? NetworkMapView.withAlpha(n7, 56) : n6));
        guiGraphics.renderOutline(n3, n4, n5, 16, bl || bl2 ? n7 : NetworkMapView.withAlpha(n7, 144));
        guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(component.getString(), n5 - 4), n3 + n5 / 2, n4 + 4, bl || bl2 ? -1 : -2762272);
    }

    private static int withAlpha(int n, int n2) {
        return n2 << 24 | n & 0xFFFFFF;
    }

    private boolean clickToolbar(double d, double d2, int n) {
        int n2;
        int n3 = this.viewButtonsTop();
        int n4 = this.left + 7;
        int n5 = this.sceneRight() - this.left - 14;
        int n6 = n4;
        int n7 = (n5 - 12) / 5;
        for (MapFilter mapFilter : MapFilter.values()) {
            if (NetworkMapView.insideButton(d, d2, n6, n3, n7)) {
                this.filter = mapFilter;
                this.selectedIndex = -1;
                this.focusedOnBlock = false;
                this.recomputeFit();
                return true;
            }
            n6 += n7 + 3;
        }
        int n8 = n3 + 16 + 4;
        int n9 = (n5 - 9) / 4;
        for (n2 = 0; n2 < 4; ++n2) {
            int n10 = n4 + n2 * (n9 + 3);
            if (!NetworkMapView.insideButton(d, d2, n10, n8, n9)) continue;
            if (n != 0) {
                return true;
            }
            switch (n2) {
                case 0: {
                    this.fitView();
                    break;
                }
                case 1: {
                    this.setView(225.0f, 30.0f, true);
                    break;
                }
                case 2: {
                    this.setView(180.0f, 85.0f, true);
                    break;
                }
                case 3: {
                    this.setView(180.0f, 5.0f, true);
                    break;
                }
            }
            return true;
        }
        n6 = n4;
        n2 = n8 + 16 + 4;
        int n11 = (n5 - 12) / 5;
        if (NetworkMapView.insideButton(d, d2, n6, n2, n11)) {
            if (n == 0) {
                this.heatmap = !this.heatmap;
            }
            return true;
        }
        if (NetworkMapView.insideButton(d, d2, n6 += n11 + 3, n2, n11)) {
            if (n == 0) {
                this.showEvents = !this.showEvents;
            }
            return true;
        }
        n6 += n11 + 3;
        int n12 = 0;
        while (n12 < 3) {
            if (NetworkMapView.insideButton(d, d2, n6, n2, n11)) {
                if (n == 1) {
                    this.saveBookmark(n12);
                } else if (n == 0) {
                    this.loadBookmark(n12);
                }
                return true;
            }
            ++n12;
            n6 += n11 + 3;
        }
        return false;
    }

    private static boolean insideButton(double d, double d2, int n, int n2, int n3) {
        return d >= (double)n && d < (double)(n + n3) && d2 >= (double)n2 && d2 < (double)(n2 + 16);
    }

    private void drawHelp(GuiGraphics guiGraphics, int n) {
        if (!((Boolean)ClientConfig.VALUES.mapShowControls.get()).booleanValue()) {
            return;
        }
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n2 = this.top + this.height - 30;
        int n3 = this.left + (n - this.left) / 2;
        guiGraphics.fill(this.left + 6, n2, n - 6, this.top + this.height - 5, -1340860382);
        guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.controls.navigation"), n3, n2 + 4, palette.muted());
        guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.controls.selection"), n3, n2 + 15, palette.muted());
        if (this.snapshot != null && this.snapshot.truncated()) {
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.truncated"), this.left + 7, this.top + 7, -14740, false);
        }
    }

    private void drawDetails(GuiGraphics guiGraphics) {
        MonitorNetwork.MapNode mapNode;
        if (!((Boolean)ClientConfig.VALUES.mapShowDetails.get()).booleanValue()) {
            return;
        }
        if (this.showEvents) {
            this.drawEventLog(guiGraphics);
            return;
        }
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n = this.sceneRight() + 12;
        int n2 = this.left + this.width - 8;
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.details"), n, this.top + 9, palette.accentB(), false);
        if (this.snapshot == null) {
            return;
        }
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.blocks", (Object[])new Object[]{this.snapshot.nodes().size()}), n, this.top + 25, palette.muted(), false);
        MonitorNetwork.MapNode mapNode2 = mapNode = this.selectedIndex >= 0 && this.selectedIndex < this.snapshot.nodes().size() ? this.snapshot.nodes().get(this.selectedIndex) : null;
        if (mapNode == null) {
            guiGraphics.drawWordWrap(this.font, (FormattedText)Component.translatable((String)"screen.forever_production_monitor.map.select_hint"), n, this.top + 48, Math.max(40, n2 - n), palette.muted());
            return;
        }
        ItemStack itemStack = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(mapNode.visualId()));
        guiGraphics.renderItem(itemStack, n, this.top + 45);
        guiGraphics.drawWordWrap(this.font, (FormattedText)Component.literal((String)mapNode.name()), n + 22, this.top + 46, Math.max(40, n2 - n - 22), palette.text());
        int n3 = this.top + 76;
        this.drawDetailLine(guiGraphics, n, n2, n3, (Component)Component.translatable((String)"screen.forever_production_monitor.map.position"), mapNode.pos().toShortString(), palette.text());
        this.drawDetailLine(guiGraphics, n, n2, n3 += 17, (Component)Component.translatable((String)"screen.forever_production_monitor.map.state"), Component.translatable((String)("screen.forever_production_monitor.map.state." + mapNode.state().name().toLowerCase(Locale.ROOT))).getString(), NetworkMapView.stateColorInt(mapNode.state()));
        this.drawDetailLine(guiGraphics, n, n2, n3 += 17, (Component)Component.translatable((String)"screen.forever_production_monitor.map.channels"), Integer.toString(mapNode.channels()), -8861464);
        this.drawDetailLine(guiGraphics, n, n2, n3 += 17, (Component)Component.translatable((String)"screen.forever_production_monitor.map.power"), NetworkMapView.formatPower(mapNode.idlePower()), -14740);
        guiGraphics.drawWordWrap(this.font, (FormattedText)Component.literal((String)mapNode.visualId().toString()), n, n3 += 23, Math.max(40, n2 - n), palette.muted());
    }

    private void drawEventLog(GuiGraphics guiGraphics) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n = this.sceneRight() + 12;
        int n2 = this.left + this.width - 8;
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.event_log"), n, this.top + 9, palette.accentB(), false);
        ArrayDeque<MapEvent> arrayDeque = this.currentEvents();
        if (arrayDeque.isEmpty()) {
            guiGraphics.drawWordWrap(this.font, (FormattedText)Component.translatable((String)"screen.forever_production_monitor.map.event_log.empty"), n, this.top + 31, Math.max(40, n2 - n), palette.muted());
            return;
        }
        int n3 = this.top + 29;
        int n4 = this.top + this.height - 8;
        for (MapEvent mapEvent : arrayDeque) {
            if (n3 + 27 > n4) break;
            String string = EVENT_TIME.format(Instant.ofEpochMilli(mapEvent.time()).atZone(ZoneId.systemDefault()));
            guiGraphics.drawString(this.font, string, n, n3, palette.muted(), false);
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(mapEvent.name(), Math.max(30, n2 - n - 48)), n + 46, n3, palette.text(), false);
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)("screen.forever_production_monitor.map.event." + mapEvent.type().name().toLowerCase(Locale.ROOT))), n, n3 + 11, NetworkMapView.eventColor(mapEvent.type()), false);
            n3 += 27;
        }
    }

    private void drawDetailLine(GuiGraphics guiGraphics, int n, int n2, int n3, Component component, String string, int n4) {
        guiGraphics.drawString(this.font, component, n, n3, InterfaceTheme.current().muted(), false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(string, Math.max(20, n2 - n)), n, n3 + 9, n4, false);
    }

    private static String formatPower(double d) {
        return d < 1000.0 ? String.format(Locale.ROOT, "%.2f AE/t", d) : String.format(Locale.ROOT, "%.2fk AE/t", d / 1000.0);
    }

    boolean mouseClicked(double d, double d2, int n) {
        if ((n == 0 || n == 1) && this.clickToolbar(d, d2, n)) {
            return true;
        }
        if (!this.insideScene(d, d2)) {
            return false;
        }
        if (n == 0 || n == 1) {
            this.dragButton = n;
            this.pressX = d;
            this.pressY = d2;
            this.dragged = false;
            return true;
        }
        if (n == 2) {
            this.resetView();
            return true;
        }
        return false;
    }

    boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (n != this.dragButton) {
            return false;
        }
        double d5 = d - this.pressX;
        double d6 = d2 - this.pressY;
        if (d5 * d5 + d6 * d6 >= 12.0) {
            this.dragged = true;
        }
        if (n == 0) {
            double d7 = (Double)ClientConfig.VALUES.mapRotationSensitivity.get();
            float f = (Boolean)ClientConfig.VALUES.mapInvertHorizontal.get() != false ? -1.0f : 1.0f;
            float f2 = (Boolean)ClientConfig.VALUES.mapInvertVertical.get() != false ? -1.0f : 1.0f;
            this.yaw = NetworkMapView.normalizeYaw(this.yaw + (float)(d3 * d7) * f);
            this.pitch = NetworkMapView.clampPitch(this.pitch - (float)(d4 * d7) * f2);
            this.rememberCamera();
            return true;
        }
        if (n == 1) {
            double d8 = (Double)ClientConfig.VALUES.mapPanSensitivity.get();
            this.panX += d3 * d8;
            this.panY += d4 * d8;
            this.rememberCamera();
            return true;
        }
        return false;
    }

    boolean mouseReleased(double d, double d2, int n) {
        if (this.dragButton != n) {
            return false;
        }
        if (n == 0 && !this.dragged) {
            this.updateHover((int)d, (int)d2);
            this.selectedIndex = this.hoveredIndex;
            long l = System.currentTimeMillis();
            if (this.selectedIndex >= 0 && this.selectedIndex == this.lastClickIndex && l - this.lastClickTime <= 350L) {
                this.focusSelected();
            }
            this.lastClickIndex = this.selectedIndex;
            this.lastClickTime = l;
        }
        this.dragButton = -1;
        return true;
    }

    private void focusSelected() {
        if (this.snapshot == null || this.selectedIndex < 0 || this.selectedIndex >= this.snapshot.nodes().size()) {
            return;
        }
        BlockPos blockPos = this.snapshot.nodes().get(this.selectedIndex).pos();
        this.centerX = (double)blockPos.getX() + 0.5;
        this.centerY = (double)blockPos.getY() + 0.5;
        this.centerZ = (double)blockPos.getZ() + 0.5;
        this.panY = 0.0;
        this.panX = 0.0;
        this.zoom = Math.max((Double)ClientConfig.VALUES.mapFocusZoom.get(), this.zoom);
        this.focusedOnBlock = true;
        this.rememberCamera();
    }

    boolean mouseScrolled(double d, double d2, double d3) {
        if (!this.insideScene(d, d2) || d3 == 0.0) {
            return false;
        }
        double d4 = this.scale();
        double d5 = this.panX;
        double d6 = this.panY;
        double d7 = 1.0 + 0.14 * (Double)ClientConfig.VALUES.mapZoomSensitivity.get();
        this.zoom = Math.max(0.18, Math.min(10.0, this.zoom * Math.pow(d7, d3)));
        double d8 = this.scale() / d4;
        double d9 = (double)(this.left + this.sceneRight()) * 0.5;
        double d10 = (double)(this.sceneContentTop() + this.sceneContentBottom()) * 0.5;
        this.panX = d - d9 - (d - d9 - d5) * d8;
        this.panY = d2 - d10 - (d2 - d10 - d6) * d8;
        this.rememberCamera();
        return true;
    }

    boolean keyPressed(int n) {
        if (n == 82 || n == 268) {
            this.resetView();
            return true;
        }
        if (n == 49) {
            this.setView(225.0f, 30.0f, true);
            return true;
        }
        if (n == 50) {
            this.setView(180.0f, 85.0f, true);
            return true;
        }
        if (n == 51) {
            this.setView(180.0f, 0.0f, true);
            return true;
        }
        return false;
    }

    private void updateHover(int n, int n2) {
        this.hoveredIndex = -1;
        if (this.snapshot == null || !this.insideScene(n, n2)) {
            return;
        }
        double d = Double.MAX_VALUE;
        double d2 = Math.max(5.0, Math.min(18.0, this.scale() * 0.7));
        for (int i = 0; i < this.snapshot.nodes().size(); ++i) {
            double d3;
            double[] dArray;
            double d4;
            double d5;
            if (!this.shouldRender(this.snapshot.nodes().get(i)) || !((d5 = (d4 = (double)n - (dArray = this.project(this.snapshot.nodes().get(i)))[0]) * d4 + (d3 = (double)n2 - dArray[1]) * d3) <= d2 * d2) || !(d5 < d)) continue;
            d = d5;
            this.hoveredIndex = i;
        }
    }

    private double[] project(MonitorNetwork.MapNode mapNode) {
        return this.project(mapNode.pos(), mapNode.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.PART ? mapNode.side() : -1);
    }

    private double[] project(BlockPos blockPos, int n) {
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        if (n >= 0) {
            Direction direction = Direction.from3DDataValue((int)n);
            d = (double)direction.getStepX() * 0.38;
            d2 = (double)direction.getStepY() * 0.38;
            d3 = (double)direction.getStepZ() * 0.38;
        }
        double d4 = (double)blockPos.getX() + 0.5 + d - this.centerX;
        double d5 = (double)blockPos.getY() + 0.5 + d2 - this.centerY;
        double d6 = (double)blockPos.getZ() + 0.5 + d3 - this.centerZ;
        double d7 = Math.toRadians(this.yaw);
        double d8 = Math.toRadians(this.pitch);
        double d9 = d4 * Math.cos(d7) + d6 * Math.sin(d7);
        double d10 = -d4 * Math.sin(d7) + d6 * Math.cos(d7);
        double d11 = d5 * Math.cos(d8) - d10 * Math.sin(d8);
        return new double[]{(double)(this.left + this.sceneRight()) * 0.5 + this.panX + d9 * this.scale(), (double)(this.sceneContentTop() + this.sceneContentBottom()) * 0.5 + this.panY - d11 * this.scale()};
    }

    private void recomputeFit() {
        if (this.snapshot == null || this.snapshot.nodes().isEmpty() || this.width <= 184 || this.height <= 0) {
            return;
        }
        double d = this.centerX;
        double d2 = this.centerY;
        double d3 = this.centerZ;
        int n = Integer.MAX_VALUE;
        int n2 = Integer.MAX_VALUE;
        int n3 = Integer.MAX_VALUE;
        int n4 = Integer.MIN_VALUE;
        int n5 = Integer.MIN_VALUE;
        int n6 = Integer.MIN_VALUE;
        for (MonitorNetwork.MapNode mapNode : this.snapshot.nodes()) {
            if (!this.shouldRender(mapNode)) continue;
            BlockPos blockPos = mapNode.pos();
            n = Math.min(n, blockPos.getX());
            n2 = Math.min(n2, blockPos.getY());
            n3 = Math.min(n3, blockPos.getZ());
            n4 = Math.max(n4, blockPos.getX());
            n5 = Math.max(n5, blockPos.getY());
            n6 = Math.max(n6, blockPos.getZ());
        }
        if (n == Integer.MAX_VALUE) {
            return;
        }
        if (this.focusedOnBlock) {
            this.centerX = d;
            this.centerY = d2;
            this.centerZ = d3;
        } else {
            this.centerX = (double)(n + n4 + 1) * 0.5;
            this.centerY = (double)(n2 + n5 + 1) * 0.5;
            this.centerZ = (double)(n3 + n6 + 1) * 0.5;
        }
        this.sceneDepthSpan = Math.max(2.0, (double)(n4 - n) + 1.0 + ((double)(n5 - n2) + 1.0) + ((double)(n6 - n3) + 1.0));
        double d4 = Math.max(2.0, (double)(n4 - n + n6 - n3) + 2.0);
        double d5 = Math.max(2.0, (double)(n5 - n2) + d4 * 0.35 + 2.0);
        double d6 = Math.max(80.0, (double)(this.sceneRight() - this.left) - 28.0);
        double d7 = Math.max(80.0, (double)(this.sceneContentBottom() - this.sceneContentTop()) - 28.0);
        this.fitScale = Math.max(0.2, Math.min(22.0, Math.min(d6 / d4, d7 / d5)));
    }

    private double scale() {
        return this.fitScale * this.zoom;
    }

    private int sceneRight() {
        return (Boolean)ClientConfig.VALUES.mapShowDetails.get() != false ? this.left + Math.max(80, this.width - 184 - 6) : this.left + this.width;
    }

    private boolean insideScene(double d, double d2) {
        return d >= (double)this.left && d < (double)this.sceneRight() && d2 >= (double)this.sceneContentTop() && d2 < (double)this.sceneContentBottom();
    }

    private int viewButtonsTop() {
        return this.top + 5;
    }

    private int sceneContentTop() {
        return this.top + 70;
    }

    private int sceneContentBottom() {
        return this.top + this.height - ((Boolean)ClientConfig.VALUES.mapShowControls.get() != false ? 30 : 2);
    }

    private boolean shouldRender(MonitorNetwork.MapNode mapNode) {
        String string;
        boolean bl;
        boolean bl2 = mapNode.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.PART && NetworkMapView.isCable(mapNode.visualId().getPath());
        boolean bl3 = bl = mapNode.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.PART && !bl2;
        if (!this.search.isEmpty() && !(string = (mapNode.name() + " " + String.valueOf(mapNode.visualId()) + " " + mapNode.pos().toShortString()).toLowerCase(Locale.ROOT)).contains(this.search)) {
            return false;
        }
        return switch (this.filter) {
            default -> throw new IncompatibleClassChangeError();
            case MapFilter.ALL -> true;
            case MapFilter.DEVICES -> {
                if (mapNode.device() || mapNode.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.BLOCK) {
                    yield true;
                }
                yield false;
            }
            case MapFilter.CABLES -> bl2;
            case MapFilter.PARTS -> bl;
            case MapFilter.ERRORS -> mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL || mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.UNPOWERED || mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.BOOTING;
        };
    }

    private int heatColorInt(MonitorNetwork.MapNode mapNode) {
        if (mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL) {
            return 16727625;
        }
        if (mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.UNPOWERED) {
            return 16742989;
        }
        if (mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.BOOTING) {
            return 16762967;
        }
        int n = mapNode.channels();
        int n2 = 1;
        if (NetworkMapView.isCable(mapNode.visualId().getPath())) {
            n = this.estimatedCableLoad(mapNode.pos());
            n2 = mapNode.visualId().getPath().contains("dense") ? 32 : 8;
        }
        double d = Math.min(1.0, (double)n / (double)Math.max(1, n2));
        if (n <= 0) {
            return 6516867;
        }
        if (d < 0.55) {
            return 5230987;
        }
        if (d < 0.85) {
            return 15059290;
        }
        return 16738143;
    }

    private float[] heatColor(MonitorNetwork.MapNode mapNode) {
        int n = this.heatColorInt(mapNode);
        return new float[]{(float)(n >> 16 & 0xFF) / 255.0f, (float)(n >> 8 & 0xFF) / 255.0f, (float)(n & 0xFF) / 255.0f};
    }

    private int estimatedCableLoad(BlockPos blockPos) {
        return this.cableLoads.getOrDefault(blockPos.asLong(), 0);
    }

    private void recomputeCableLoads() {
        this.cableLoads.clear();
        if (this.snapshot == null) {
            return;
        }
        List<MonitorNetwork.MapNode> list = this.snapshot.nodes().stream().filter(mapNode -> mapNode.channels() > 0 && !NetworkMapView.isCable(mapNode.visualId().getPath())).toList();
        for (MonitorNetwork.MapNode mapNode2 : this.snapshot.nodes()) {
            if (!NetworkMapView.isCable(mapNode2.visualId().getPath())) continue;
            int n = 0;
            for (MonitorNetwork.MapNode mapNode3 : list) {
                int n2 = Math.abs(mapNode3.pos().getX() - mapNode2.pos().getX()) + Math.abs(mapNode3.pos().getY() - mapNode2.pos().getY()) + Math.abs(mapNode3.pos().getZ() - mapNode2.pos().getZ());
                if (n2 > 6) continue;
                n += mapNode3.channels();
            }
            this.cableLoads.put(mapNode2.pos().asLong(), n);
        }
    }

    private CameraState[] cameraBookmarks() {
        return CAMERA_BOOKMARKS.computeIfAbsent(this.networkKey(), string -> new CameraState[3]);
    }

    private void saveBookmark(int n) {
        this.cameraBookmarks()[n] = this.cameraState();
    }

    private void loadBookmark(int n) {
        CameraState cameraState = this.cameraBookmarks()[n];
        if (cameraState != null) {
            this.restoreCamera(cameraState);
        }
    }

    private CameraState cameraState() {
        return new CameraState(this.yaw, this.pitch, this.zoom, this.panX, this.panY, this.centerX, this.centerY, this.centerZ, this.focusedOnBlock);
    }

    private String networkKey() {
        return this.snapshot == null ? "none" : String.valueOf(this.snapshot.dimension()) + "@" + this.snapshot.pos().asLong();
    }

    private ArrayDeque<MapEvent> currentEvents() {
        return EVENT_LOGS.computeIfAbsent(this.networkKey(), string -> new ArrayDeque());
    }

    private void recordEvents(MonitorNetwork.NetworkMapPayload networkMapPayload, MonitorNetwork.NetworkMapPayload networkMapPayload2) {
        HashMap<NodeKey, MonitorNetwork.MapNode> hashMap = new HashMap<NodeKey, MonitorNetwork.MapNode>();
        for (MonitorNetwork.MapNode object2 : networkMapPayload.nodes()) {
            hashMap.put(NodeKey.of(object2), object2);
        }
        HashMap<NodeKey, MonitorNetwork.MapNode> hashMap2 = new HashMap<>();
        for (MonitorNetwork.MapNode mapNode : networkMapPayload2.nodes()) {
            hashMap2.put(NodeKey.of(mapNode), mapNode);
        }
        ArrayDeque<MapEvent> arrayDeque = EVENT_LOGS.computeIfAbsent(String.valueOf(networkMapPayload2.dimension()) + "@" + networkMapPayload2.pos().asLong(), string -> new ArrayDeque());
        long l = System.currentTimeMillis();
        for (Map.Entry<NodeKey, MonitorNetwork.MapNode> entry : hashMap2.entrySet()) {
            MonitorNetwork.MapNode mapNode = (MonitorNetwork.MapNode)hashMap.get(entry.getKey());
            MonitorNetwork.MapNode mapNode2 = (MonitorNetwork.MapNode)entry.getValue();
            if (mapNode == null && mapNode2.device()) {
                NetworkMapView.addEvent(arrayDeque, new MapEvent(l, mapNode2.name(), EventType.ADDED));
                continue;
            }
            if (mapNode == null || mapNode.state() == mapNode2.state()) continue;
            EventType eventType = mapNode2.state() == ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL ? EventType.CHANNEL_LOST : (mapNode2.state() == ProductionMonitorBlockEntity.MapNodeState.UNPOWERED ? EventType.POWER_LOST : (mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL ? EventType.CHANNEL_RESTORED : (mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.UNPOWERED ? EventType.POWER_RESTORED : EventType.STATE_CHANGED)));
            NetworkMapView.addEvent(arrayDeque, new MapEvent(l, mapNode2.name(), eventType));
        }
        for (Map.Entry<NodeKey, MonitorNetwork.MapNode> entry : hashMap.entrySet()) {
            if (hashMap2.containsKey(entry.getKey()) || !((MonitorNetwork.MapNode)entry.getValue()).device()) continue;
            NetworkMapView.addEvent(arrayDeque, new MapEvent(l, ((MonitorNetwork.MapNode)entry.getValue()).name(), EventType.REMOVED));
        }
    }

    private static void addEvent(ArrayDeque<MapEvent> arrayDeque, MapEvent mapEvent) {
        arrayDeque.addFirst(mapEvent);
        while (arrayDeque.size() > 80) {
            arrayDeque.removeLast();
        }
    }

    private static int eventColor(EventType eventType) {
        return switch (eventType) {
            default -> throw new IncompatibleClassChangeError();
            case EventType.CHANNEL_LOST, EventType.POWER_LOST, EventType.REMOVED -> -35716;
            case EventType.CHANNEL_RESTORED, EventType.POWER_RESTORED, EventType.ADDED -> -9972847;
            case EventType.STATE_CHANGED -> -14740;
        };
    }

    private void rememberCamera() {
        if (this.snapshot == null || !((Boolean)ClientConfig.VALUES.mapRememberCamera.get()).booleanValue()) {
            return;
        }
        rememberedNetwork = String.valueOf(this.snapshot.dimension()) + "@" + this.snapshot.pos().asLong();
        rememberedCamera = this.cameraState();
    }

    private void restoreCamera(CameraState cameraState) {
        this.yaw = NetworkMapView.normalizeYaw(cameraState.yaw);
        this.pitch = NetworkMapView.clampPitch(cameraState.pitch);
        this.zoom = Math.max(0.18, Math.min(10.0, cameraState.zoom));
        this.panX = cameraState.panX;
        this.panY = cameraState.panY;
        this.centerX = cameraState.centerX;
        this.centerY = cameraState.centerY;
        this.centerZ = cameraState.centerZ;
        this.focusedOnBlock = cameraState.focused;
        this.recomputeFit();
    }

    private static float clampPitch(float f) {
        if (!Float.isFinite(f)) {
            return 30.0f;
        }
        return Math.max(5.0f, Math.min(85.0f, f));
    }

    private static float normalizeYaw(float f) {
        if (!Float.isFinite(f)) {
            return 225.0f;
        }
        if ((f %= 360.0f) > 180.0f) {
            f -= 360.0f;
        }
        if (f <= -180.0f) {
            f += 360.0f;
        }
        return f;
    }

    private static float[] stateColor(ProductionMonitorBlockEntity.MapNodeState mapNodeState) {
        int n = NetworkMapView.stateColorInt(mapNodeState);
        return new float[]{(float)(n >> 16 & 0xFF) / 255.0f, (float)(n >> 8 & 0xFF) / 255.0f, (float)(n & 0xFF) / 255.0f};
    }

    private static int stateColorInt(ProductionMonitorBlockEntity.MapNodeState mapNodeState) {
        return switch (mapNodeState) {
            default -> throw new IncompatibleClassChangeError();
            case ProductionMonitorBlockEntity.MapNodeState.CONNECTED -> -8861464;
            case ProductionMonitorBlockEntity.MapNodeState.ACTIVE -> -9972847;
            case ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL -> -35716;
            case ProductionMonitorBlockEntity.MapNodeState.UNPOWERED -> -29320;
            case ProductionMonitorBlockEntity.MapNodeState.BOOTING -> -14740;
        };
    }

    static {
        CAMERA_BOOKMARKS = new HashMap<String, CameraState[]>();
        EVENT_LOGS = new HashMap<String, ArrayDeque<MapEvent>>();
        EVENT_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    private static enum MapFilter {
        ALL,
        DEVICES,
        CABLES,
        PARTS,
        ERRORS;

    }

    private record CameraState(float yaw, float pitch, double zoom, double panX, double panY, double centerX, double centerY, double centerZ, boolean focused) {
    }

    private static enum ToolbarGroup {
        FILTER,
        CAMERA,
        DIAGNOSTIC,
        BOOKMARK;

    }

    private record MapEvent(long time, String name, EventType type) {
    }

    private static enum EventType {
        ADDED,
        REMOVED,
        CHANNEL_LOST,
        CHANNEL_RESTORED,
        POWER_LOST,
        POWER_RESTORED,
        STATE_CHANGED;

    }

    private record NodeKey(long pos, String visualId, ProductionMonitorBlockEntity.MapRenderKind kind, int side) {
        static NodeKey of(MonitorNetwork.MapNode mapNode) {
            return new NodeKey(mapNode.pos().asLong(), mapNode.visualId().toString(), mapNode.renderKind(), mapNode.side());
        }
    }
}
