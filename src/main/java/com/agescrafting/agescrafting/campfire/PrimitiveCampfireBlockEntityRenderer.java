package com.agescrafting.agescrafting.campfire;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PrimitiveCampfireBlockEntityRenderer implements BlockEntityRenderer<PrimitiveCampfireBlockEntity> {

    public PrimitiveCampfireBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull PrimitiveCampfireBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (int i = 0; i < PrimitiveCampfireBlockEntity.MAX_FUEL; i++) {
            ItemStack stack = blockEntity.getFuelStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            poseStack.pushPose();
            applyLogTransform(poseStack, i);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    blockEntity.getLevel(),
                    i
            );
            poseStack.popPose();
        }

        ItemStack cookStack = blockEntity.getCookStack();
        if (!cookStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.34D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.52F, 0.52F, 0.52F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    cookStack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    blockEntity.getLevel(),
                    99
            );
            poseStack.popPose();
        }
    }

    private static void applyLogTransform(PoseStack poseStack, int i) {
        if (i < 4) {
            poseStack.translate(0.5D, 0.20D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F * (i % 4)));
            poseStack.translate(0.24D, 0.0D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
        } else {
            poseStack.translate(0.5D, 0.125D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F * (i % 4) + 45.0F));
            poseStack.translate(0.27D, 0.0D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }

        poseStack.scale(0.38F, 0.95F, 0.38F);
    }
}


