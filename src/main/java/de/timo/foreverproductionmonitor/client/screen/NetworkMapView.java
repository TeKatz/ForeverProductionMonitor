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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

final class NetworkMapView {
    private static final int MIN_DETAILS_WIDTH = 108;
    private static final int MAX_DETAILS_WIDTH = 136;
    private static final int COLLAPSED_DETAILS_WIDTH = 18;
    private static final int VIEW_BUTTON_HEIGHT = 14;
    private static final int VIEW_BUTTON_GAP = 2;
    private static final int CAMERA_TOOLBAR_WIDTH = 58;
    private static final int CAMERA_BUTTON_MARGIN = 4;
    private static final int ZONE_GAP = 2;
    private static final int VIEW_TOOLBAR_HEIGHT = 24;
    private static final int CONTROL_BAR_HEIGHT = 28;
    private static final double DRAG_THRESHOLD_SQUARED = 12.0;
    private static final float MIN_CAMERA_PITCH = 5.0f;
    private static final float MAX_CAMERA_PITCH = 85.0f;
    private static final double MAX_SCENE_DEPTH = 4096.0;
    private static final Map<String, CameraState> REMEMBERED_CAMERAS = new HashMap<>();
    private static final Map<String, CameraState[]> CAMERA_BOOKMARKS;
    private static String loadedBookmarksEncoded;
    private static final Map<String, ArrayDeque<MapEvent>> EVENT_LOGS;
    private static final Map<String, MonitorNetwork.NetworkMapPayload> LAST_EVENT_SNAPSHOTS = new HashMap<>();
    private static Object eventConnection;
    private static final DateTimeFormatter EVENT_TIME;
    private final Minecraft minecraft;
    private final Font font;
    private final BiConsumer<ResourceLocation, BlockPos> pathNavigator;
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
    private long selectedAtNanos;
    private int dragButton = -1;
    private double pressX;
    private double pressY;
    private boolean dragged;
    private boolean focusedOnBlock;
    private boolean heatmap;
    private boolean showEvents;
    private boolean detailsCollapsed;
    private NetworkMapPathResolver.Target followTarget;
    private final NetworkMapPerformanceCache performanceCache = new NetworkMapPerformanceCache();
    private final ArrayList<Integer> frameNodeIndices = new ArrayList<>();
    private int followButtonX;
    private int followButtonY;
    private int followButtonWidth;
    private static final int FOLLOW_BUTTON_HEIGHT = 16;
    private MapFilter filter = MapFilter.ALL;
    private String search = "";
    private int searchCursor = -1;
    private long lastClickTime;
    private int lastClickIndex = -1;
    private double centerX;
    private double centerY;
    private double centerZ;
    private double fitScale = 8.0;
    private double sceneDepthSpan = 2.0;

    NetworkMapView(Minecraft minecraft, Font font,
                   BiConsumer<ResourceLocation, BlockPos> pathNavigator) {
        this.minecraft = minecraft;
        this.font = font;
        this.pathNavigator = pathNavigator;
    }

    void setBounds(int n, int n2, int n3, int n4) {
        this.left = n;
        this.top = n2;
        this.width = n3;
        this.height = n4;
        this.recomputeFit();
    }

    void accept(MonitorNetwork.NetworkMapPayload networkMapPayload) {
        boolean contextChanged = this.snapshot == null
                || !this.snapshot.dimension().equals(networkMapPayload.dimension())
                || !this.snapshot.pos().equals(networkMapPayload.pos())
                || !this.snapshot.viewDimension().equals(networkMapPayload.viewDimension());

        String eventKey = this.eventKey(networkMapPayload);
        MonitorNetwork.NetworkMapPayload previousForEvents =
                !contextChanged && this.snapshot != null
                        ? this.snapshot
                        : LAST_EVENT_SNAPSHOTS.get(eventKey);
        if (previousForEvents != null) {
            this.recordEvents(previousForEvents, networkMapPayload);
        }
        LAST_EVENT_SNAPSHOTS.put(eventKey, networkMapPayload);

        this.snapshot = networkMapPayload;
        this.performanceCache.acceptSnapshot(this.snapshot, this::shouldRender);
        if (this.selectedIndex >= networkMapPayload.nodes().size()
                || this.selectedIndex >= 0
                && !this.shouldRender(networkMapPayload.nodes().get(this.selectedIndex))) {
            this.selectedIndex = -1;
        }

        if (contextChanged) {
            String snapshotKey = this.snapshotKey(networkMapPayload);
            CameraState remembered = REMEMBERED_CAMERAS.get(snapshotKey);
            if (ClientConfig.VALUES.mapRememberCamera.get() && remembered != null) {
                this.restoreCamera(remembered);
            } else {
                this.applyDefaultView();
            }
        } else {
            this.recomputeFit();
        }
    }

    void clearSnapshot() {
        this.rememberCamera();
        this.snapshot = null;
        this.hoveredIndex = -1;
        this.selectedIndex = -1;
        this.searchCursor = -1;
        this.followTarget = null;
        this.dragButton = -1;
        this.focusedOnBlock = false;
        this.performanceCache.clear();
        this.frameNodeIndices.clear();
    }

    void focusPosition(BlockPos target) {
        if (this.snapshot == null || target == null) {
            return;
        }
        int bestIndex = -1;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < this.snapshot.nodes().size(); ++i) {
            MonitorNetwork.MapNode node = this.snapshot.nodes().get(i);
            int dx = Math.abs(node.pos().getX() - target.getX());
            int dy = Math.abs(node.pos().getY() - target.getY());
            int dz = Math.abs(node.pos().getZ() - target.getZ());
            int distance = Math.max(dx, Math.max(dy, dz));
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
                if (distance == 0) {
                    break;
                }
            }
        }
        if (bestIndex >= 0 && bestDistance <= 1) {
            this.selectedIndex = bestIndex;
            this.selectedAtNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
            this.focusSelected();
        }
    }

    void setSearch(String value) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        if (Objects.equals(this.search, normalized)) {
            return;
        }
        this.search = normalized;
        this.searchCursor = -1;
        this.rebuildVisibilityCache();
        if (this.selectedIndex >= 0 && this.snapshot != null
                && !this.shouldRender(this.snapshot.nodes().get(this.selectedIndex))) {
            this.selectedIndex = -1;
        }
        this.focusedOnBlock = false;
        this.recomputeFit();
    }

    void focusNextSearchMatch() {
        if (this.snapshot == null || this.performanceCache.visibleNodeIndices().isEmpty()) {
            return;
        }
        List<Integer> visible = this.performanceCache.visibleNodeIndices();
        int nextIndex = visible.get(0);
        for (int index : visible) {
            if (index > this.searchCursor) {
                nextIndex = index;
                break;
            }
        }
        this.searchCursor = nextIndex;
        this.selectedIndex = nextIndex;
        this.selectedAtNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
        this.focusSelected();
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
        this.followTarget = null;
        int n3 = this.sceneRight();
        int detailLeft = this.detailLeft();
        int cameraLeft = this.cameraToolbarLeft();
        guiGraphics.fill(this.left, this.top, this.left + this.width, this.top + this.height, palette.tableOuter());
        guiGraphics.fill(this.left + 1, this.top + 1, n3 - 1, this.top + this.height - 1, -15723491);
        if (this.showViewButtons()) {
            guiGraphics.fill(cameraLeft, this.top + 1, this.cameraToolbarRight(), this.top + this.height - 1, palette.summary());
            guiGraphics.fill(cameraLeft - 1, this.top, cameraLeft, this.top + this.height, palette.accentB());
        }
        if (((Boolean)ClientConfig.VALUES.mapShowDetails.get()).booleanValue()) {
            guiGraphics.fill(detailLeft, this.top + 1, this.left + this.width - 1, this.top + this.height - 1, palette.rowEven());
            guiGraphics.fill(detailLeft - 1, this.top, detailLeft, this.top + this.height, palette.accentA());
        }
        if (this.snapshot == null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.loading"), this.left + (n3 - this.left) / 2, this.top + this.height / 2, -14740);
            this.drawControlBar(guiGraphics, n3);
            return;
        }
        if (this.snapshot.nodes().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.map.empty"), this.left + (n3 - this.left) / 2, this.top + this.height / 2, -7366746);
            this.drawControlBar(guiGraphics, n3);
            this.drawClippedDetails(guiGraphics);
            return;
        }
        this.prepareFrameNodeIndices();
        this.updateHover(n, n2);
        this.renderScene(guiGraphics, n3);
        this.drawToolbar(guiGraphics, n, n2, n3);
        this.drawControlBar(guiGraphics, n3);
        this.drawClippedDetails(guiGraphics);
    }

    private void drawClippedDetails(GuiGraphics guiGraphics) {
        if (!((Boolean)ClientConfig.VALUES.mapShowDetails.get()).booleanValue()) {
            return;
        }
        guiGraphics.enableScissor(this.detailLeft(), this.top + 1, this.left + this.width - 1, this.top + this.height - 1);
        if (this.detailsCollapsed) {
            this.drawCollapsedDetails(guiGraphics);
        } else {
            this.drawDetails(guiGraphics);
        }
        guiGraphics.disableScissor();
    }

    private void renderScene(GuiGraphics guiGraphics, int sceneRight) {
        double scale = this.scale();
        double centerScreenX = (double)(this.left + sceneRight) * 0.5 + this.panX;
        double centerScreenY = (double)(this.sceneContentTop() + this.sceneContentBottom()) * 0.5 + this.panY;

        guiGraphics.flush();
        guiGraphics.enableScissor(this.left + 1, this.sceneContentTop(),
                sceneRight - 1, this.sceneContentBottom());
        RenderSystem.clearDepth(1.0);
        RenderSystem.clear(256, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerScreenX, centerScreenY, 180.0);
        float depthScale = (float)Math.min(scale, 4096.0 / this.sceneDepthSpan);
        poseStack.scale((float)scale, (float)(-scale), depthScale);
        poseStack.mulPose(Axis.XP.rotationDegrees(this.pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(this.yaw));
        poseStack.translate(-this.centerX, -this.centerY, -this.centerZ);

        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        List<MonitorNetwork.MapNode> nodes = this.snapshot.nodes();

        for (int index : this.frameNodeIndices) {
            MonitorNetwork.MapNode node = nodes.get(index);
            if (node.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.BLOCK) {
                this.renderBlock(poseStack, bufferSource, node);
            }
        }

        if (!this.heatmap) {
            for (int index : this.frameNodeIndices) {
                MonitorNetwork.MapNode node = nodes.get(index);
                if (node.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.PART
                        && NetworkMapView.isCable(node.visualId().getPath())) {
                    this.renderCableItemModels(
                            poseStack, bufferSource, node,
                            this.performanceCache.visiblePositions());
                }
            }
        }

        bufferSource.endBatch();
        VertexConsumer faceConsumer = bufferSource.getBuffer(RenderType.debugQuads());
        for (int index : this.frameNodeIndices) {
            MonitorNetwork.MapNode node = nodes.get(index);
            if (node.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.PART) {
                this.renderPartGeometry(
                        poseStack, faceConsumer, node,
                        this.performanceCache.visiblePositions());
            }
        }
        bufferSource.endBatch(RenderType.debugQuads());

        VertexConsumer partLines = bufferSource.getBuffer(RenderType.lines());
        for (int index : this.frameNodeIndices) {
            MonitorNetwork.MapNode node = nodes.get(index);
            if (node.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.PART) {
                this.renderPartOutline(poseStack, partLines, node);
            }
        }
        bufferSource.endBatch(RenderType.lines());

        if (this.heatmap) {
            VertexConsumer heatLines = bufferSource.getBuffer(RenderType.lines());
            for (int index : this.frameNodeIndices) {
                MonitorNetwork.MapNode node = nodes.get(index);
                float[] color = this.heatColor(node);
                LevelRenderer.renderLineBox(
                        poseStack, heatLines,
                        NetworkMapView.boundsFor(node).inflate(0.025),
                        color[0], color[1], color[2], 0.96f);
            }
            bufferSource.endBatch(RenderType.lines());
        }

        int highlightedIndex = this.selectedIndex >= 0 ? this.selectedIndex : this.hoveredIndex;
        if (highlightedIndex >= 0
                && highlightedIndex < nodes.size()
                && this.performanceCache.visibleNodeIndices().contains(highlightedIndex)) {
            MonitorNetwork.MapNode node = nodes.get(highlightedIndex);
            float[] color = NetworkMapView.stateColor(node.state());
            float selectionProgress = GuiMotion.progress(this.selectedAtNanos, 300L);
            double pulse = GuiMotion.enabled()
                    ? (Math.sin((double)GuiMotion.now() / 2.2E8) + 1.0) * 0.006
                    + (1.0 - GuiMotion.easeOut(selectionProgress)) * 0.028
                    : 0.0;
            LevelRenderer.renderLineBox(
                    poseStack,
                    bufferSource.getBuffer(RenderType.lines()),
                    NetworkMapView.boundsFor(node).inflate(0.035 + pulse),
                    color[0], color[1], color[2], 1.0f);
            bufferSource.endBatch(RenderType.lines());
        }

        poseStack.popPose();
        RenderSystem.disableDepthTest();
        guiGraphics.disableScissor();
    }

    private void prepareFrameNodeIndices() {
        this.frameNodeIndices.clear();
        List<Integer> visible = this.performanceCache.visibleNodeIndices();
        if (visible.size() <= 512) {
            this.frameNodeIndices.addAll(visible);
            return;
        }

        double margin = Math.max(64.0, Math.min(512.0, this.scale() * 1.75));
        double minX = this.left - margin;
        double maxX = this.sceneRight() + margin;
        double minY = this.sceneContentTop() - margin;
        double maxY = this.sceneContentBottom() + margin;

        List<MonitorNetwork.MapNode> nodes = this.snapshot.nodes();
        ProjectionContext projection = this.projectionContext();
        double[] projected = new double[2];
        for (int index : visible) {
            this.project(nodes.get(index), projection, projected);
            if (projected[0] >= minX && projected[0] <= maxX
                    && projected[1] >= minY && projected[1] <= maxY) {
                this.frameNodeIndices.add(index);
            }
        }
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

    private void renderCableItemModels(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                       MonitorNetwork.MapNode mapNode, Set<BlockPos> occupied) {
        ItemStack stack = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(mapNode.visualId()));
        if (stack.isEmpty()) {
            return;
        }

        BlockPos pos = mapNode.pos();
        boolean xAxis = occupied.contains(pos.relative(Direction.EAST)) || occupied.contains(pos.relative(Direction.WEST));
        boolean yAxis = occupied.contains(pos.relative(Direction.UP)) || occupied.contains(pos.relative(Direction.DOWN));
        boolean zAxis = occupied.contains(pos.relative(Direction.NORTH)) || occupied.contains(pos.relative(Direction.SOUTH));

        if (!xAxis && !yAxis && !zAxis) {
            zAxis = true;
        }
        if (xAxis) {
            this.renderCableItemAxis(poseStack, bufferSource, stack, pos, Direction.Axis.X);
        }
        if (yAxis) {
            this.renderCableItemAxis(poseStack, bufferSource, stack, pos, Direction.Axis.Y);
        }
        if (zAxis) {
            this.renderCableItemAxis(poseStack, bufferSource, stack, pos, Direction.Axis.Z);
        }
    }

    private void renderCableItemAxis(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                     ItemStack stack, BlockPos pos, Direction.Axis axis) {
        poseStack.pushPose();
        poseStack.translate((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        if (axis == Direction.Axis.X) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        } else if (axis == Direction.Axis.Y) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        }
        poseStack.scale(1.0f, 1.0f, 1.334f);
        this.minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, 0xF000F0,
                OverlayTexture.NO_OVERLAY, poseStack, (MultiBufferSource)bufferSource,
                (Level)this.minecraft.level, pos.hashCode() ^ axis.ordinal());
        poseStack.popPose();
    }

    private void renderPartGeometry(PoseStack poseStack, VertexConsumer faceConsumer,
                                    MonitorNetwork.MapNode mapNode, Set<BlockPos> occupied) {
        String path = mapNode.visualId().getPath();
        int base = this.heatmap ? this.heatColorInt(mapNode) : NetworkMapView.partColor(path, mapNode.state());
        float r = (float)(base >> 16 & 0xFF) / 255.0f;
        float g = (float)(base >> 8 & 0xFF) / 255.0f;
        float b = (float)(base & 0xFF) / 255.0f;

        if (!NetworkMapView.isCable(path)) {
            NetworkMapView.addBox(poseStack, faceConsumer,
                    NetworkMapView.partPlate(mapNode), r, g, b);
            return;
        }

        if (!this.heatmap) {
            return;
        }

        boolean dense = path.contains("dense");
        boolean glass = path.contains("glass");
        boolean covered = path.contains("covered");
        double radius = dense ? 0.25 : (glass ? 0.125 : (covered ? 0.21875 : 0.1875));
        BlockPos pos = mapNode.pos();

        AABB core = new AABB(
                (double)pos.getX() + 0.5 - radius, (double)pos.getY() + 0.5 - radius, (double)pos.getZ() + 0.5 - radius,
                (double)pos.getX() + 0.5 + radius, (double)pos.getY() + 0.5 + radius, (double)pos.getZ() + 0.5 + radius);
        NetworkMapView.addBox(poseStack, faceConsumer, core, r, g, b);
        for (Direction direction : Direction.values()) {
            if (occupied.contains(pos.relative(direction))) {
                NetworkMapView.addBox(poseStack, faceConsumer,
                        NetworkMapView.cableArm(pos, direction, radius), r, g, b);
            }
        }
    }

    private void renderPartOutline(PoseStack poseStack, VertexConsumer lineConsumer,
                                   MonitorNetwork.MapNode mapNode) {
        String path = mapNode.visualId().getPath();
        if (NetworkMapView.isCable(path)) {
            return;
        }
        int base = this.heatmap ? this.heatColorInt(mapNode) : NetworkMapView.partColor(path, mapNode.state());
        float r = (float)(base >> 16 & 0xFF) / 255.0f;
        float g = (float)(base >> 8 & 0xFF) / 255.0f;
        float b = (float)(base & 0xFF) / 255.0f;
        LevelRenderer.renderLineBox(
                poseStack, lineConsumer, NetworkMapView.partPlate(mapNode),
                r * 0.32f, g * 0.32f, b * 0.32f, 0.82f);
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

    private static void addBox(PoseStack poseStack, VertexConsumer vertexConsumer, AABB aABB, float f, float f2, float f3) {
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.DOWN, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.58f), (float)(f2 * 0.58f), (float)(f3 * 0.58f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.UP, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)Math.min(1.0f, f * 1.1f), (float)Math.min(1.0f, f2 * 1.1f), (float)Math.min(1.0f, f3 * 1.1f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.NORTH, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.72f), (float)(f2 * 0.72f), (float)(f3 * 0.72f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.SOUTH, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.9f), (float)(f2 * 0.9f), (float)(f3 * 0.9f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.WEST, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.66f), (float)(f2 * 0.66f), (float)(f3 * 0.66f), (float)1.0f);
        LevelRenderer.renderFace((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (Direction)Direction.EAST, (float)((float)aABB.minX), (float)((float)aABB.minY), (float)((float)aABB.minZ), (float)((float)aABB.maxX), (float)((float)aABB.maxY), (float)((float)aABB.maxZ), (float)(f * 0.82f), (float)(f2 * 0.82f), (float)(f3 * 0.82f), (float)1.0f);
    }

    private static int partColor(String string, ProductionMonitorBlockEntity.MapNodeState mapNodeState) {
        if (mapNodeState == ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL) {
            return 16741500;
        }
        if (mapNodeState == ProductionMonitorBlockEntity.MapNodeState.UNPOWERED) {
            return 16747896;
        }
        if (string.contains("fluix")) {
            return 0x7453A8;
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

    private void drawToolbar(GuiGraphics guiGraphics, int mouseX, int mouseY, int sceneRight) {
        int topY = this.viewButtonsTop();
        int leftX = this.left + 7;
        int available = sceneRight - this.left - 14;
        int buttonX = leftX;
        int buttonWidth = (available - 12) / 5;

        for (MapFilter mapFilter : MapFilter.values()) {
            this.drawToolbarButton(guiGraphics, mouseX, mouseY, buttonX, topY, buttonWidth,
                    Component.translatable("screen.forever_production_monitor.map.filter." + mapFilter.name().toLowerCase(Locale.ROOT)),
                    this.filter == mapFilter, ToolbarGroup.FILTER);
            buttonX += buttonWidth + 3;
        }

        if (!this.showViewButtons()) {
            return;
        }

        int cameraX = this.cameraToolbarLeft() + CAMERA_BUTTON_MARGIN;
        int cameraWidth = CAMERA_TOOLBAR_WIDTH - CAMERA_BUTTON_MARGIN * 2;
        int y = this.top + 5;
        String[] views = new String[]{"fit", "iso", "top", "front"};
        for (String view : views) {
            this.drawToolbarButton(guiGraphics, mouseX, mouseY, cameraX, y, cameraWidth,
                    Component.translatable("screen.forever_production_monitor.map.view." + view), false, ToolbarGroup.CAMERA);
            y += VIEW_BUTTON_HEIGHT + VIEW_BUTTON_GAP;
        }

        y += 4;
        this.drawToolbarButton(guiGraphics, mouseX, mouseY, cameraX, y, cameraWidth,
                Component.translatable("screen.forever_production_monitor.map.heatmap"), this.heatmap, ToolbarGroup.DIAGNOSTIC);
        y += VIEW_BUTTON_HEIGHT + VIEW_BUTTON_GAP;
        this.drawToolbarButton(guiGraphics, mouseX, mouseY, cameraX, y, cameraWidth,
                Component.translatable("screen.forever_production_monitor.map.events"), this.showEvents, ToolbarGroup.DIAGNOSTIC);
        y += VIEW_BUTTON_HEIGHT + 5;
        int bottomBookmarkY = this.top + this.height - VIEW_BUTTON_HEIGHT - 6;
        if (bottomBookmarkY > y) {
            y = bottomBookmarkY;
        }

        int bookmarkGap = 2;
        int bookmarkWidth = Math.max(8, (cameraWidth - bookmarkGap * 2) / 3);
        CameraState[] bookmarks = this.cameraBookmarks();
        for (int i = 0; i < 3; ++i) {
            int bx = cameraX + i * (bookmarkWidth + bookmarkGap);
            boolean filled = bookmarks[i] != null;
            this.drawToolbarButton(guiGraphics, mouseX, mouseY, bx, y, bookmarkWidth,
                    Component.literal(Integer.toString(i + 1)), filled, ToolbarGroup.BOOKMARK);
            if (mouseX >= bx && mouseX < bx + bookmarkWidth && mouseY >= y && mouseY < y + VIEW_BUTTON_HEIGHT) {
                guiGraphics.renderTooltip(this.font,
                        Component.translatable("screen.forever_production_monitor.map.bookmark_hint"), mouseX, mouseY);
            }
        }
    }

    private void drawToolbarButton(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, Component component, boolean bl, ToolbarGroup toolbarGroup) {
        int n6;
        boolean bl2 = n >= n3 && n < n3 + n5 && n2 >= n4 && n2 < n4 + VIEW_BUTTON_HEIGHT;
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
        guiGraphics.fill(n3, n4, n3 + n5, n4 + VIEW_BUTTON_HEIGHT, bl ? NetworkMapView.withAlpha(n7, 98) : (bl2 ? NetworkMapView.withAlpha(n7, 56) : n6));
        guiGraphics.renderOutline(n3, n4, n5, VIEW_BUTTON_HEIGHT, bl || bl2 ? n7 : NetworkMapView.withAlpha(n7, 144));
        guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(component.getString(), n5 - 4), n3 + n5 / 2, n4 + 3, bl || bl2 ? -1 : -2762272);
    }

    private static int withAlpha(int n, int n2) {
        return n2 << 24 | n & 0xFFFFFF;
    }

    private boolean clickToolbar(double mouseX, double mouseY, int button) {
        int topY = this.viewButtonsTop();
        int leftX = this.left + 7;
        int available = this.sceneRight() - this.left - 14;
        int x = leftX;
        int filterWidth = (available - 12) / 5;

        for (MapFilter mapFilter : MapFilter.values()) {
            if (NetworkMapView.insideButton(mouseX, mouseY, x, topY, filterWidth)) {
                this.filter = mapFilter;
                this.selectedIndex = -1;
                this.focusedOnBlock = false;
                this.rebuildVisibilityCache();
                this.recomputeFit();
                return true;
            }
            x += filterWidth + 3;
        }

        if (!this.showViewButtons()) {
            return false;
        }

        int cameraX = this.cameraToolbarLeft() + CAMERA_BUTTON_MARGIN;
        int cameraWidth = CAMERA_TOOLBAR_WIDTH - CAMERA_BUTTON_MARGIN * 2;
        int y = this.top + 5;
        for (int i = 0; i < 4; ++i) {
            if (NetworkMapView.insideButton(mouseX, mouseY, cameraX, y, cameraWidth)) {
                if (button == 0) {
                    switch (i) {
                        case 0 -> this.fitView();
                        case 1 -> this.setView(225.0f, 30.0f, true);
                        case 2 -> this.setView(180.0f, 85.0f, true);
                        case 3 -> this.setView(180.0f, 5.0f, true);
                    }
                }
                return true;
            }
            y += VIEW_BUTTON_HEIGHT + VIEW_BUTTON_GAP;
        }

        y += 4;
        if (NetworkMapView.insideButton(mouseX, mouseY, cameraX, y, cameraWidth)) {
            if (button == 0) {
                this.heatmap = !this.heatmap;
            }
            return true;
        }

        y += VIEW_BUTTON_HEIGHT + VIEW_BUTTON_GAP;
        if (NetworkMapView.insideButton(mouseX, mouseY, cameraX, y, cameraWidth)) {
            if (button == 0) {
                this.showEvents = !this.showEvents;
                if (this.showEvents && this.detailsCollapsed) {
                    this.detailsCollapsed = false;
                    this.recomputeFit();
                }
            }
            return true;
        }

        y += VIEW_BUTTON_HEIGHT + 5;
        int bottomBookmarkY = this.top + this.height - VIEW_BUTTON_HEIGHT - 6;
        if (bottomBookmarkY > y) {
            y = bottomBookmarkY;
        }

        int bookmarkGap = 2;
        int bookmarkWidth = Math.max(8, (cameraWidth - bookmarkGap * 2) / 3);
        for (int i = 0; i < 3; ++i) {
            int bx = cameraX + i * (bookmarkWidth + bookmarkGap);
            if (!NetworkMapView.insideButton(mouseX, mouseY, bx, y, bookmarkWidth)) {
                continue;
            }
            if (button == 1) {
                this.saveBookmark(i);
            } else if (button == 0) {
                this.loadBookmark(i);
            }
            return true;
        }

        return false;
    }

    private static boolean insideButton(double d, double d2, int n, int n2, int n3) {
        return d >= (double)n && d < (double)(n + n3) && d2 >= (double)n2 && d2 < (double)(n2 + VIEW_BUTTON_HEIGHT);
    }

    private void drawControlBar(GuiGraphics guiGraphics, int sceneRight) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int barTop = this.sceneContentBottom();
        int barBottom = this.top + this.height - 1;

        guiGraphics.fill(this.left + 1, barTop, sceneRight - 1, barBottom, palette.summary());
        guiGraphics.fill(this.left + 1, barTop, sceneRight - 1, barTop + 1,
                NetworkMapView.withAlpha(palette.border(), 112));

        int textLeft = this.left + 216;
        int textRight = sceneRight - 8;
        int available = textRight - textLeft;
        boolean truncated = this.snapshot != null && this.snapshot.truncated();

        if (truncated) {
            if (available >= 40) {
                String warning = Component.translatable("screen.forever_production_monitor.map.truncated").getString();
                int center = textLeft + available / 2;
                guiGraphics.drawCenteredString(
                        this.font,
                        this.ellipsize(warning, available),
                        center,
                        barTop + 10,
                        -14740);
            }
        } else if (((Boolean)ClientConfig.VALUES.mapShowControls.get()).booleanValue()) {
            if (available >= 88) {
                String navigation = Component.translatable("screen.forever_production_monitor.map.controls.navigation").getString();
                String selection = Component.translatable("screen.forever_production_monitor.map.controls.selection").getString();
                if (this.font.width(navigation) > available || this.font.width(selection) > available) {
                    navigation = Component.translatable("screen.forever_production_monitor.map.controls.navigation.compact").getString();
                    selection = Component.translatable("screen.forever_production_monitor.map.controls.selection.compact").getString();
                }
                if (this.font.width(navigation) > available || this.font.width(selection) > available) {
                    navigation = Component.translatable("screen.forever_production_monitor.map.controls.navigation.minimal").getString();
                    selection = Component.translatable("screen.forever_production_monitor.map.controls.selection.minimal").getString();
                }

                if (this.font.width(navigation) <= available && this.font.width(selection) <= available) {
                    int center = textLeft + available / 2;
                    guiGraphics.drawCenteredString(this.font, navigation, center, barTop + 4, palette.muted());
                    guiGraphics.drawCenteredString(this.font, selection, center, barTop + 15, palette.muted());
                }
            }
        }
    }

    private void drawCollapsedDetails(GuiGraphics guiGraphics) {
        this.drawDetailsToggle(guiGraphics);
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int x = this.detailLeft() + COLLAPSED_DETAILS_WIDTH / 2;
        guiGraphics.fill(x, this.top + 29, x + 1, this.top + this.height - 8,
                NetworkMapView.withAlpha(palette.accentA(), 36));
    }

    private void drawDetailsToggle(GuiGraphics guiGraphics) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        String glyph = this.detailsCollapsed ? "›" : "‹";
        int x = this.detailLeft() + (this.detailsCollapsed ? 5 : 4);
        guiGraphics.drawString(this.font, glyph, x, this.top + 7, palette.accentB(), false);
    }

    private void drawDetails(GuiGraphics guiGraphics) {
        if (!((Boolean)ClientConfig.VALUES.mapShowDetails.get()).booleanValue()) {
            return;
        }
        if (this.showEvents) {
            this.drawEventLog(guiGraphics);
            return;
        }

        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int left = this.detailLeft() + 8;
        int right = this.left + this.width - 8;
        this.drawDetailsToggle(guiGraphics);

        int titleX = this.detailLeft() + 17;
        Component detailsTitle = Component.translatable("screen.forever_production_monitor.map.details");
        guiGraphics.drawString(this.font,
                this.font.plainSubstrByWidth(detailsTitle.getString(), Math.max(20, right - titleX)),
                titleX, this.top + 9, palette.accentB(), false);

        if (this.snapshot == null) {
            return;
        }

        MonitorNetwork.MapNode selected = this.selectedIndex >= 0 && this.selectedIndex < this.snapshot.nodes().size()
                ? this.snapshot.nodes().get(this.selectedIndex) : null;
        Component blocks = Component.translatable("screen.forever_production_monitor.map.blocks", this.snapshot.nodes().size());

        if (selected == null) {
            guiGraphics.drawString(this.font,
                    this.font.plainSubstrByWidth(blocks.getString(), Math.max(20, right - left)),
                    left, this.top + 29, palette.muted(), false);
            guiGraphics.drawWordWrap(this.font,
                    Component.translatable("screen.forever_production_monitor.map.select_hint"),
                    left, this.top + 49, Math.max(40, right - left), palette.muted());
            return;
        }

        String displayName = this.nodeDisplayName(selected);
        ItemStack stack = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(selected.visualId()));
        guiGraphics.renderItem(stack, left, this.top + 27);

        int nameX = left + 22;
        int nameWidth = Math.max(40, right - nameX);
        List<FormattedCharSequence> nameLines = this.font.split(Component.literal(displayName), nameWidth);
        int nameY = this.top + 28;
        int shownLines = Math.min(3, nameLines.size());
        for (int i = 0; i < shownLines; ++i) {
            guiGraphics.drawString(this.font, nameLines.get(i), nameX, nameY + i * 10, palette.text(), false);
        }
        int blocksY = nameY + shownLines * 10 + 2;
        guiGraphics.drawString(this.font,
                this.font.plainSubstrByWidth(blocks.getString(), Math.max(20, right - left - 22)),
                nameX, blocksY, palette.muted(), false);

        int lineY = Math.max(this.top + 65, blocksY + 23);
        this.drawDetailLine(guiGraphics, left, right, lineY,
                Component.translatable("screen.forever_production_monitor.map.position"),
                selected.pos().toShortString(), palette.text());
        this.drawDetailLine(guiGraphics, left, right, lineY += 17,
                Component.translatable("screen.forever_production_monitor.map.state"),
                Component.translatable("screen.forever_production_monitor.map.state." + selected.state().name().toLowerCase(Locale.ROOT)).getString(),
                NetworkMapView.stateColorInt(selected.state()));
        this.drawDetailLine(guiGraphics, left, right, lineY += 17,
                Component.translatable("screen.forever_production_monitor.map.channels"),
                Integer.toString(selected.channels()), -8861464);
        this.drawDetailLine(guiGraphics, left, right, lineY += 17,
                Component.translatable("screen.forever_production_monitor.map.power"),
                NetworkMapView.formatPower(selected.idlePower()), -14740);

        int registryY = lineY + 24;
        NetworkMapPathResolver.Target pathTarget = NetworkMapPathResolver.resolve(this.snapshot, selected);
        if (pathTarget != null) {
            int pathY = lineY + 25;
            Component linkTitle = Component.translatable(
                    pathTarget.type() == NetworkMapPathResolver.Type.QUANTUM
                            ? "screen.forever_production_monitor.map.quantum_bridge"
                            : "screen.forever_production_monitor.map.wireless_connector");
            guiGraphics.drawString(this.font,
                    this.ellipsize(linkTitle.getString(), Math.max(40, right - left)),
                    left, pathY, palette.accentB(), false);

            String destination = pathTarget.type() == NetworkMapPathResolver.Type.QUANTUM
                    ? NetworkMapView.dimensionName(pathTarget.dimension())
                    : pathTarget.pos().toShortString();
            guiGraphics.drawString(this.font,
                    this.ellipsize(
                            Component.translatable(
                                    "screen.forever_production_monitor.map.destination",
                                    destination).getString(),
                            Math.max(40, right - left)),
                    left, pathY + 11, palette.muted(), false);

            this.followTarget = pathTarget;
            this.followButtonX = left;
            this.followButtonY = pathY + 24;
            this.followButtonWidth = Math.max(40, right - left);
            guiGraphics.fill(this.followButtonX, this.followButtonY,
                    this.followButtonX + this.followButtonWidth,
                    this.followButtonY + FOLLOW_BUTTON_HEIGHT,
                    palette.rowOdd());
            guiGraphics.fill(this.followButtonX, this.followButtonY,
                    this.followButtonX + this.followButtonWidth,
                    this.followButtonY + 1, palette.accentA());
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("screen.forever_production_monitor.map.follow_path"),
                    this.followButtonX + this.followButtonWidth / 2,
                    this.followButtonY + 4, palette.text());
            registryY = this.followButtonY + FOLLOW_BUTTON_HEIGHT + 9;
        }

        guiGraphics.drawWordWrap(this.font, Component.literal(selected.visualId().toString()),
                left, registryY, Math.max(40, right - left), palette.muted());
    }

    static String dimensionName(ResourceLocation dimension) {
        if ("minecraft".equals(dimension.getNamespace())) {
            if ("overworld".equals(dimension.getPath())) return "Overworld";
            if ("the_nether".equals(dimension.getPath())) return "The Nether";
            if ("the_end".equals(dimension.getPath())) return "The End";
        }
        String[] words = dimension.getPath().replace('/', ' ').replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        if (!"minecraft".equals(dimension.getNamespace())) {
            result.append(" (").append(dimension.getNamespace()).append(')');
        }
        return result.toString();
    }

    private String nodeDisplayName(MonitorNetwork.MapNode mapNode) {
        String name = mapNode.name();
        if (name != null && !name.isBlank() && !"ME Network Block".equals(name)) {
            return name;
        }

        ItemStack stack = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(mapNode.visualId()));
        if (!stack.isEmpty()) {
            String itemName = stack.getHoverName().getString();
            if (itemName != null && !itemName.isBlank()) {
                return itemName;
            }
        }

        BlockState state = Block.stateById(mapNode.blockStateId());
        String blockName = state.getBlock().getName().getString();
        if (blockName != null && !blockName.isBlank()) {
            return blockName;
        }
        return mapNode.visualId().getPath();
    }

    private void drawEventLog(GuiGraphics guiGraphics) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int left = this.detailLeft() + 8;
        int right = this.left + this.width - 8;
        this.drawDetailsToggle(guiGraphics);
        int titleX = this.detailLeft() + 17;
        Component eventTitle = Component.translatable("screen.forever_production_monitor.map.event_log");
        guiGraphics.drawString(this.font,
                this.ellipsize(eventTitle.getString(), Math.max(20, right - titleX)),
                titleX, this.top + 9, palette.accentB(), false);
        ArrayDeque<MapEvent> events = this.currentEvents();
        if (events.isEmpty()) {
            guiGraphics.drawWordWrap(this.font,
                    Component.translatable("screen.forever_production_monitor.map.event_log.empty"),
                    left, this.top + 31, Math.max(40, right - left), palette.muted());
            return;
        }
        int y = this.top + 29;
        int bottom = this.top + this.height - 8;
        for (MapEvent event : events) {
            if (y + 27 > bottom) break;
            String time = EVENT_TIME.format(Instant.ofEpochMilli(event.time()).atZone(ZoneId.systemDefault()));
            guiGraphics.drawString(this.font, time, left, y, palette.muted(), false);

            int nameWidth = Math.max(20, right - (left + 46));
            guiGraphics.drawString(this.font,
                    this.ellipsize(event.name(), nameWidth),
                    left + 46, y, palette.text(), false);

            String eventText = Component.translatable(
                    "screen.forever_production_monitor.map.event."
                            + event.type().name().toLowerCase(Locale.ROOT)).getString();
            int eventWidth = Math.max(20, right - left);
            guiGraphics.drawString(this.font,
                    this.ellipsize(eventText, eventWidth),
                    left, y + 11, NetworkMapView.eventColor(event.type()), false);
            y += 27;
        }
    }

    private String ellipsize(String text, int maxWidth) {
        if (text == null || text.isEmpty() || maxWidth <= 0) {
            return "";
        }
        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String suffix = "...";
        if (this.font.width(suffix) > maxWidth) {
            return this.font.plainSubstrByWidth(suffix, maxWidth);
        }

        int bodyWidth = Math.max(0, maxWidth - this.font.width(suffix));
        return this.font.plainSubstrByWidth(text, bodyWidth) + suffix;
    }

    private void drawDetailLine(GuiGraphics guiGraphics, int n, int n2, int n3, Component component, String string, int n4) {
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(component.getString(), Math.max(20, n2 - n)), n, n3, InterfaceTheme.current().muted(), false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(string, Math.max(20, n2 - n)), n, n3 + 9, n4, false);
    }

    private static String formatPower(double d) {
        return d < 1000.0 ? String.format(Locale.ROOT, "%.2f AE/t", d) : String.format(Locale.ROOT, "%.2fk AE/t", d / 1000.0);
    }

    private boolean clickFollowPath(double mouseX, double mouseY) {
        if (this.followTarget == null
                || mouseX < this.followButtonX
                || mouseX >= this.followButtonX + this.followButtonWidth
                || mouseY < this.followButtonY
                || mouseY >= this.followButtonY + FOLLOW_BUTTON_HEIGHT) {
            return false;
        }
        this.pathNavigator.accept(this.followTarget.dimension(), this.followTarget.pos());
        return true;
    }

    private boolean clickDetailsToggle(double mouseX, double mouseY) {
        if (!((Boolean)ClientConfig.VALUES.mapShowDetails.get()).booleanValue()) {
            return false;
        }
        int toggleLeft = this.detailLeft();
        int toggleWidth = this.detailsCollapsed ? COLLAPSED_DETAILS_WIDTH : 17;
        if (mouseX < toggleLeft || mouseX >= toggleLeft + toggleWidth
                || mouseY < this.top + 2 || mouseY >= this.top + 24) {
            return false;
        }
        this.detailsCollapsed = !this.detailsCollapsed;
        this.recomputeFit();
        return true;
    }

    boolean mouseClicked(double d, double d2, int n) {
        if (n == 0 && this.clickFollowPath(d, d2)) {
            return true;
        }
        if (n == 0 && this.clickDetailsToggle(d, d2)) {
            return true;
        }
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
            this.selectedAtNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
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

    private void updateHover(int mouseX, int mouseY) {
        this.hoveredIndex = -1;
        if (this.snapshot == null || !this.insideScene(mouseX, mouseY)) {
            return;
        }

        double bestDistance = Double.MAX_VALUE;
        double hitRadius = Math.max(5.0, Math.min(18.0, this.scale() * 0.7));
        double hitRadiusSquared = hitRadius * hitRadius;
        List<MonitorNetwork.MapNode> nodes = this.snapshot.nodes();
        ProjectionContext projection = this.projectionContext();
        double[] projected = new double[2];

        for (int index : this.frameNodeIndices) {
            this.project(nodes.get(index), projection, projected);
            double dx = (double)mouseX - projected[0];
            double dy = (double)mouseY - projected[1];
            double distance = dx * dx + dy * dy;
            if (distance <= hitRadiusSquared && distance < bestDistance) {
                bestDistance = distance;
                this.hoveredIndex = index;
            }
        }
    }

    private ProjectionContext projectionContext() {
        double yawRadians = Math.toRadians(this.yaw);
        double pitchRadians = Math.toRadians(this.pitch);
        double scale = this.scale();
        return new ProjectionContext(
                Math.cos(yawRadians),
                Math.sin(yawRadians),
                Math.cos(pitchRadians),
                Math.sin(pitchRadians),
                scale,
                (double)(this.left + this.sceneRight()) * 0.5 + this.panX,
                (double)(this.sceneContentTop() + this.sceneContentBottom()) * 0.5 + this.panY);
    }

    private void project(MonitorNetwork.MapNode mapNode,
                         ProjectionContext projection,
                         double[] out) {
        this.project(
                mapNode.pos(),
                mapNode.renderKind() == ProductionMonitorBlockEntity.MapRenderKind.PART
                        ? mapNode.side() : -1,
                projection,
                out);
    }

    private void project(BlockPos blockPos, int side,
                         ProjectionContext projection,
                         double[] out) {
        double sideX = 0.0;
        double sideY = 0.0;
        double sideZ = 0.0;
        if (side >= 0) {
            Direction direction = Direction.from3DDataValue(side);
            sideX = (double)direction.getStepX() * 0.38;
            sideY = (double)direction.getStepY() * 0.38;
            sideZ = (double)direction.getStepZ() * 0.38;
        }

        double localX = (double)blockPos.getX() + 0.5 + sideX - this.centerX;
        double localY = (double)blockPos.getY() + 0.5 + sideY - this.centerY;
        double localZ = (double)blockPos.getZ() + 0.5 + sideZ - this.centerZ;
        double rotatedX = localX * projection.cosYaw()
                + localZ * projection.sinYaw();
        double rotatedZ = -localX * projection.sinYaw()
                + localZ * projection.cosYaw();
        double projectedY = localY * projection.cosPitch()
                - rotatedZ * projection.sinPitch();

        out[0] = projection.centerScreenX() + rotatedX * projection.scale();
        out[1] = projection.centerScreenY() - projectedY * projection.scale();
    }

    private void recomputeFit() {
        if (this.snapshot == null
                || this.performanceCache.visibleNodeIndices().isEmpty()
                || this.width <= this.detailsWidth()
                || this.height <= 0) {
            return;
        }

        double previousCenterX = this.centerX;
        double previousCenterY = this.centerY;
        double previousCenterZ = this.centerZ;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        List<MonitorNetwork.MapNode> nodes = this.snapshot.nodes();
        for (int index : this.performanceCache.visibleNodeIndices()) {
            BlockPos pos = nodes.get(index).pos();
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        if (this.focusedOnBlock) {
            this.centerX = previousCenterX;
            this.centerY = previousCenterY;
            this.centerZ = previousCenterZ;
        } else {
            this.centerX = (double)(minX + maxX + 1) * 0.5;
            this.centerY = (double)(minY + maxY + 1) * 0.5;
            this.centerZ = (double)(minZ + maxZ + 1) * 0.5;
        }

        this.sceneDepthSpan = Math.max(
                2.0,
                (double)(maxX - minX) + 1.0
                        + ((double)(maxY - minY) + 1.0)
                        + ((double)(maxZ - minZ) + 1.0));
        double horizontalSpan = Math.max(2.0, (double)(maxX - minX + maxZ - minZ) + 2.0);
        double verticalSpan = Math.max(2.0, (double)(maxY - minY) + horizontalSpan * 0.35 + 2.0);
        double availableWidth = Math.max(80.0, (double)(this.sceneRight() - this.left) - 28.0);
        double availableHeight = Math.max(
                80.0,
                (double)(this.sceneContentBottom() - this.sceneContentTop()) - 28.0);
        this.fitScale = Math.max(
                0.2,
                Math.min(22.0, Math.min(availableWidth / horizontalSpan, availableHeight / verticalSpan)));
    }

    private double scale() {
        return this.fitScale * this.zoom;
    }

    private int sceneRight() {
        return Math.max(this.left + 1, this.cameraToolbarLeft() - ZONE_GAP);
    }

    private int cameraToolbarLeft() {
        return this.cameraToolbarRight() - (this.showViewButtons() ? CAMERA_TOOLBAR_WIDTH : 0);
    }

    private int cameraToolbarRight() {
        return this.detailLeft() - (this.showViewButtons() ? ZONE_GAP : 0);
    }

    private int detailLeft() {
        return (Boolean)ClientConfig.VALUES.mapShowDetails.get() != false ? this.left + this.width - this.detailsWidth() : this.left + this.width;
    }

    private boolean showViewButtons() {
        int minimumSceneWidth = 120;
        int reservedDetails = (Boolean)ClientConfig.VALUES.mapShowDetails.get() != false ? this.detailsWidth() : 0;
        return (Boolean)ClientConfig.VALUES.mapShowViewButtons.get() && this.width >= minimumSceneWidth + reservedDetails + CAMERA_TOOLBAR_WIDTH + ZONE_GAP * 2;
    }

    private int detailsWidth() {
        if (!((Boolean)ClientConfig.VALUES.mapShowDetails.get()).booleanValue()) {
            return 0;
        }
        if (this.detailsCollapsed) {
            return COLLAPSED_DETAILS_WIDTH;
        }
        if (this.width < 280) {
            return Math.max(84, this.width / 3);
        }
        return Math.max(MIN_DETAILS_WIDTH, Math.min(MAX_DETAILS_WIDTH, this.width / 7));
    }

    private boolean insideScene(double d, double d2) {
        return d >= (double)this.left && d < (double)this.sceneRight() && d2 >= (double)this.sceneContentTop() && d2 < (double)this.sceneContentBottom();
    }

    private int viewButtonsTop() {
        return this.top + 5;
    }

    private int sceneContentTop() {
        return this.top + VIEW_TOOLBAR_HEIGHT;
    }

    private int sceneContentBottom() {
        return this.top + this.height - CONTROL_BAR_HEIGHT;
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
        return this.performanceCache.cableLoad(blockPos);
    }

    private void rebuildVisibilityCache() {
        this.performanceCache.rebuildVisibility(this.snapshot, this::shouldRender);
    }

    private CameraState[] cameraBookmarks() {
        NetworkMapView.loadPersistedBookmarks();
        return CAMERA_BOOKMARKS.computeIfAbsent(this.networkKey(), string -> new CameraState[3]);
    }

    private void saveBookmark(int n) {
        this.cameraBookmarks()[n] = this.cameraState();
        NetworkMapView.persistBookmarks();
    }

    private void loadBookmark(int n) {
        CameraState cameraState = this.cameraBookmarks()[n];
        if (cameraState != null) {
            this.restoreCamera(cameraState);
        }
    }

    private static void loadPersistedBookmarks() {
        String encoded = (String)ClientConfig.VALUES.mapCameraBookmarks.get();
        if (encoded == null) {
            encoded = "";
        }
        if (Objects.equals(encoded, loadedBookmarksEncoded)) {
            return;
        }
        loadedBookmarksEncoded = encoded;
        CAMERA_BOOKMARKS.clear();

        if (encoded.isBlank()) {
            return;
        }

        for (String entry : encoded.split(";")) {
            if (entry.isBlank()) continue;
            String[] parts = entry.split("\\|");
            if (parts.length != 11) continue;
            try {
                String key = parts[0];
                int slot = Integer.parseInt(parts[1]);
                if (slot < 0 || slot >= 3) continue;
                CameraState state = new CameraState(
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3]),
                        Double.parseDouble(parts[4]),
                        Double.parseDouble(parts[5]),
                        Double.parseDouble(parts[6]),
                        Double.parseDouble(parts[7]),
                        Double.parseDouble(parts[8]),
                        Double.parseDouble(parts[9]),
                        Boolean.parseBoolean(parts[10]));
                CAMERA_BOOKMARKS.computeIfAbsent(key, ignored -> new CameraState[3])[slot] = state;
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static void persistBookmarks() {
        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, CameraState[]> entry : CAMERA_BOOKMARKS.entrySet()) {
            CameraState[] states = entry.getValue();
            for (int slot = 0; slot < states.length; ++slot) {
                CameraState state = states[slot];
                if (state == null) continue;
                if (encoded.length() > 0) encoded.append(';');
                encoded.append(entry.getKey()).append('|')
                        .append(slot).append('|')
                        .append(state.yaw()).append('|')
                        .append(state.pitch()).append('|')
                        .append(state.zoom()).append('|')
                        .append(state.panX()).append('|')
                        .append(state.panY()).append('|')
                        .append(state.centerX()).append('|')
                        .append(state.centerY()).append('|')
                        .append(state.centerZ()).append('|')
                        .append(state.focused());
            }
        }
        String serialized = encoded.toString();
        ClientConfig.VALUES.mapCameraBookmarks.set(serialized);
        ClientConfig.VALUES.mapCameraBookmarks.save();
        loadedBookmarksEncoded = serialized;
    }

    private CameraState cameraState() {
        return new CameraState(this.yaw, this.pitch, this.zoom, this.panX, this.panY, this.centerX, this.centerY, this.centerZ, this.focusedOnBlock);
    }

    private String networkKey() {
        return this.snapshot == null ? "none" : this.snapshotKey(this.snapshot);
    }

    private String snapshotKey(MonitorNetwork.NetworkMapPayload payload) {
        return String.valueOf(payload.dimension()) + "@" + payload.pos().asLong()
                + "#" + payload.viewDimension();
    }

    private ArrayDeque<MapEvent> currentEvents() {
        if (this.snapshot == null) {
            return new ArrayDeque<>();
        }
        return EVENT_LOGS.computeIfAbsent(this.eventKey(this.snapshot), string -> new ArrayDeque());
    }

    private String eventKey(MonitorNetwork.NetworkMapPayload payload) {
        Object currentConnection = this.minecraft == null ? null : this.minecraft.getConnection();
        if (currentConnection != eventConnection) {
            EVENT_LOGS.clear();
            LAST_EVENT_SNAPSHOTS.clear();
            REMEMBERED_CAMERAS.clear();
            eventConnection = currentConnection;
        }

        int connectionId = currentConnection == null ? 0 : System.identityHashCode(currentConnection);
        return connectionId + "|" + this.snapshotKey(payload);
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
        ArrayDeque<MapEvent> arrayDeque = EVENT_LOGS.computeIfAbsent(
                this.eventKey(networkMapPayload2), string -> new ArrayDeque());
        long l = System.currentTimeMillis();
        for (Map.Entry<NodeKey, MonitorNetwork.MapNode> entry : hashMap2.entrySet()) {
            MonitorNetwork.MapNode mapNode = (MonitorNetwork.MapNode)hashMap.get(entry.getKey());
            MonitorNetwork.MapNode mapNode2 = (MonitorNetwork.MapNode)entry.getValue();
            if (mapNode == null) {
                NetworkMapView.addEvent(arrayDeque,
                        new MapEvent(l, this.nodeDisplayName(mapNode2), EventType.ADDED));
                continue;
            }

            if (mapNode.state() == mapNode2.state()) {
                continue;
            }
            EventType eventType = mapNode2.state() == ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL
                    ? EventType.CHANNEL_LOST
                    : (mapNode2.state() == ProductionMonitorBlockEntity.MapNodeState.UNPOWERED
                    ? EventType.POWER_LOST
                    : (mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.MISSING_CHANNEL
                    ? EventType.CHANNEL_RESTORED
                    : (mapNode.state() == ProductionMonitorBlockEntity.MapNodeState.UNPOWERED
                    ? EventType.POWER_RESTORED
                    : EventType.STATE_CHANGED)));
            NetworkMapView.addEvent(arrayDeque,
                    new MapEvent(l, this.nodeDisplayName(mapNode2), eventType));
        }

        for (Map.Entry<NodeKey, MonitorNetwork.MapNode> entry : hashMap.entrySet()) {
            if (hashMap2.containsKey(entry.getKey())) {
                continue;
            }
            NetworkMapView.addEvent(arrayDeque,
                    new MapEvent(l, this.nodeDisplayName(entry.getValue()), EventType.REMOVED));
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
        REMEMBERED_CAMERAS.put(this.snapshotKey(this.snapshot), this.cameraState());
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

    private record ProjectionContext(double cosYaw, double sinYaw,
                                     double cosPitch, double sinPitch,
                                     double scale,
                                     double centerScreenX, double centerScreenY) {
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
