package com.agescrafting.agescrafting.tanningrack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TanningRackBlockEntityRenderer implements BlockEntityRenderer<TanningRackBlockEntity> {
    public TanningRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull TanningRackBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getStoredItem();
        if (stack.isEmpty()) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(TanningRackBlock.FACING);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.64D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(0.0D, -0.02D, -0.18D);
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.72F, 0.72F, 0.72F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }
}
