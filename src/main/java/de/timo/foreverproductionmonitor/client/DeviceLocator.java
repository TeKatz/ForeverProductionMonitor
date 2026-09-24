/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$LineStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 */
package de.timo.foreverproductionmonitor.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class DeviceLocator {
    private static final long DISPLAY_TIME_MS = 12000L;
    private static final RenderType LOCATOR_LINES = RenderType.create((String)"forever_production_monitor_locator_lines", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_NORMAL, (VertexFormat.Mode)VertexFormat.Mode.LINES, (int)1536, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setDepthTestState(RenderStateShard.NO_DEPTH_TEST).setWriteMaskState(RenderStateShard.COLOR_WRITE).setCullState(RenderStateShard.NO_CULL).createCompositeState(false));
    private static ResourceLocation dimension;
    private static BlockPos position;
    private static long expiresAt;

    private DeviceLocator() {
    }

    public static void accept(MonitorNetwork.LocateDeviceResult locateDeviceResult) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!locateDeviceResult.found()) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage((Component)Component.translatable((String)"message.forever_production_monitor.locator.unavailable", (Object[])new Object[]{locateDeviceResult.name()}), true);
            }
            return;
        }
        dimension = locateDeviceResult.dimension();
        position = locateDeviceResult.pos().immutable();
        expiresAt = System.currentTimeMillis() + 12000L;
        minecraft.setScreen(null);
        if (minecraft.player != null) {
            ResourceLocation resourceLocation;
            ResourceLocation resourceLocation2 = resourceLocation = minecraft.level == null ? null : minecraft.level.dimension().location();
            if (!locateDeviceResult.dimension().equals((Object)resourceLocation)) {
                position = null;
                expiresAt = 0L;
                minecraft.player.displayClientMessage((Component)Component.translatable((String)"message.forever_production_monitor.locator.wrong_dimension", (Object[])new Object[]{locateDeviceResult.name(), locateDeviceResult.dimension(), locateDeviceResult.pos().getX(), locateDeviceResult.pos().getY(), locateDeviceResult.pos().getZ()}), false);
                return;
            }
            minecraft.player.displayClientMessage((Component)Component.translatable((String)"message.forever_production_monitor.locator.found", (Object[])new Object[]{locateDeviceResult.name(), position.getX(), position.getY(), position.getZ(), locateDeviceResult.index() + 1, locateDeviceResult.total()}), true);
        }
    }

    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || position == null || System.currentTimeMillis() >= expiresAt) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().location().equals((Object)dimension)) {
            return;
        }
        double d = 0.04 + (Math.sin((double)System.nanoTime() / 1.6E8) + 1.0) * 0.035;
        AABB aABB = new AABB(position).inflate(d);
        AABB aABB2 = new AABB((double)position.getX() + 0.43, (double)position.getY() + 1.0, (double)position.getZ() + 0.43, (double)position.getX() + 0.57, (double)position.getY() + 7.0, (double)position.getZ() + 0.57);
        Vec3 vec3 = renderLevelStageEvent.getCamera().getPosition();
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-vec3.x, -vec3.y, -vec3.z);
        RenderSystem.enableBlend();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(LOCATOR_LINES);
        LevelRenderer.renderLineBox((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (AABB)aABB, (float)0.78f, (float)0.35f, (float)1.0f, (float)1.0f);
        LevelRenderer.renderLineBox((PoseStack)poseStack, (VertexConsumer)vertexConsumer, (AABB)aABB2, (float)0.85f, (float)0.48f, (float)1.0f, (float)0.75f);
        bufferSource.endBatch(LOCATOR_LINES);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}

