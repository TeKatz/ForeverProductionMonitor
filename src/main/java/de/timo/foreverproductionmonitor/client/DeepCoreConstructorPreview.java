package de.timo.foreverproductionmonitor.client;

import appeng.api.orientation.IOrientationStrategy;
import appeng.core.definitions.AEBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.timo.foreverproductionmonitor.integration.DeepCoreConstructionBridge;
import de.timo.foreverproductionmonitor.item.MultiblockConstructorItem;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

/** In-world textured ghost preview for the held Multiblock Constructor. */
public final class DeepCoreConstructorPreview {
    private DeepCoreConstructorPreview() {}

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        ItemStack stack = constructorStack(minecraft.player.getMainHandItem(),
                minecraft.player.getOffhandItem());
        if (stack.isEmpty()) return;
        if (MultiblockConstructorItem.getMode(stack)
                == MultiblockConstructorItem.Mode.DISMANTLE) return;

        var target = MultiblockConstructorItem.getTarget(stack).orElse(null);
        if (target == null || !minecraft.level.dimension().location().equals(target.dimension())) return;

        List<DeepCoreConstructionBridge.Placement> placements;
        try {
            placements = DeepCoreConstructionBridge.placements(
                    MultiblockConstructorItem.getTier(stack), target.front());
        } catch (Throwable ignored) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        for (DeepCoreConstructionBridge.Placement placement : placements) {
            Block block = BuiltInRegistries.BLOCK.getOptional(placement.blockId()).orElse(Blocks.AIR);
            if (block == Blocks.AIR) continue;

            BlockPos pos = target.corePos().offset(placement.offset());
            if (!minecraft.level.hasChunkAt(pos)) continue;

            BlockState expected = orientedState(block, target.front());
            BlockState existing = minecraft.level.getBlockState(pos);
            boolean correct = existing.is(block)
                    && (block != AEBlocks.DRIVE.block()
                    || IOrientationStrategy.get(existing).getFacing(existing) == target.front());
            boolean replaceable = existing.isAir() || existing.canBeReplaced();

            pose.pushPose();
            pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

            if (correct) {
                LevelRenderer.renderLineBox(pose, buffers.getBuffer(RenderType.lines()),
                        new AABB(0.01, 0.01, 0.01, 0.99, 0.99, 0.99),
                        0.18f, 1.0f, 0.42f, 0.9f);
            } else {
                boolean invalid = !replaceable && !existing.is(block);
                MultiBufferSource ghost = type -> new GhostVertexConsumer(
                        buffers.getBuffer(RenderType.translucent()), invalid);
                minecraft.getBlockRenderer().renderSingleBlock(
                        expected, pose, ghost, LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.translucent());

                LevelRenderer.renderLineBox(pose, buffers.getBuffer(RenderType.lines()),
                        new AABB(0.005, 0.005, 0.005, 0.995, 0.995, 0.995),
                        invalid ? 1.0f : 0.25f,
                        invalid ? 0.08f : 0.7f,
                        invalid ? 0.08f : 1.0f,
                        0.95f);
            }
            pose.popPose();
        }

        buffers.endBatch(RenderType.translucent());
        buffers.endBatch(RenderType.lines());
    }

    private static BlockState orientedState(Block block, Direction front) {
        BlockState state = block.defaultBlockState();
        if (block == AEBlocks.DRIVE.block()) {
            state = IOrientationStrategy.get(state).setOrientation(state, front, Direction.UP);
        } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, front);
        }
        if (state.hasProperty(BlockStateProperties.LIT)) {
            state = state.setValue(BlockStateProperties.LIT, false);
        }
        return state;
    }

    private static ItemStack constructorStack(ItemStack main, ItemStack offhand) {
        if (main.getItem() instanceof MultiblockConstructorItem) return main;
        if (offhand.getItem() instanceof MultiblockConstructorItem) return offhand;
        return ItemStack.EMPTY;
    }

    /** Preserve the requested block texture while forcing a stable ghost alpha/tint. */
    private static final class GhostVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final boolean invalid;

        private GhostVertexConsumer(VertexConsumer delegate, boolean invalid) {
            this.delegate = delegate;
            this.invalid = invalid;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            if (invalid) {
                delegate.setColor(255, Math.min(green, 35), Math.min(blue, 35), Math.min(alpha, 125));
            } else {
                delegate.setColor(Math.min(red, 110), Math.min(green, 210), 255, Math.min(alpha, 90));
            }
            return this;
        }

        @Override public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }
        @Override public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }
        @Override public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }
        @Override public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}
