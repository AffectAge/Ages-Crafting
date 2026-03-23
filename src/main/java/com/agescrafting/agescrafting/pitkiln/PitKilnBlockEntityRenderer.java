package com.agescrafting.agescrafting.pitkiln;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PitKilnBlockEntityRenderer implements BlockEntityRenderer<PitKilnBlockEntity> {

    public PitKilnBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull PitKilnBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderInputInsidePit(blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderLogsOnTop(blockEntity, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderInputInsidePit(PitKilnBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        PitKilnBlock.Variant variant = blockEntity.getBlockState().getValue(PitKilnBlock.VARIANT);
        if (variant == PitKilnBlock.Variant.ACTIVE || variant == PitKilnBlock.Variant.COMPLETE) {
            return;
        }

        ItemStack displayStack = blockEntity.getInputStack();
        if (displayStack.isEmpty()) {
            displayStack = blockEntity.getFirstOutputStack();
        }
        if (displayStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.36D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.60F, 0.60F, 0.60F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                displayStack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                400
        );
        poseStack.popPose();
    }

    private static void renderLogsOnTop(PitKilnBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        PitKilnBlock.Variant variant = blockEntity.getBlockState().getValue(PitKilnBlock.VARIANT);
        if (variant != PitKilnBlock.Variant.THATCH && variant != PitKilnBlock.Variant.WOOD) {
            return;
        }

        for (int i = 0; i < 6; i++) {
            ItemStack log = blockEntity.getLogStack(i);
            if (log.isEmpty()) {
                continue;
            }

            poseStack.pushPose();
            applyTopLogTransform(poseStack, i);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    log,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    blockEntity.getLevel(),
                    410 + i
            );
            poseStack.popPose();
        }
    }

    private static void applyTopLogTransform(PoseStack poseStack, int index) {
        int layer = index / 3;
        int column = index % 3;

        // 3 logs per layer, laid side-by-side from left to right (X axis),
        // each running along the full Z length of the block.
        double xOffset = (column - 1) * 0.33D;
        double y = layer == 0 ? 0.72D : 0.91D;

        poseStack.translate(0.5D, y, 0.5D);
        poseStack.translate(xOffset, 0.0D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.scale(0.38F, 2.00F, 0.66F);
    }
}
