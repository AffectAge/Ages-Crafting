package com.agescrafting.agescrafting.barrel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class BarrelBlockEntityRenderer implements BlockEntityRenderer<BarrelBlockEntity> {
    private static final float MIN_X = 2.0F / 16.0F;
    private static final float MIN_Z = 2.0F / 16.0F;
    private static final float MAX_X = 14.0F / 16.0F;
    private static final float MAX_Z = 14.0F / 16.0F;
    private static final float MIN_Y = 1.05F / 16.0F;
    private static final float MAX_Y = 13.0F / 16.0F;

    public BarrelBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull BarrelBlockEntity barrel, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (barrel.isSealed()) {
            return;
        }

        FluidStack inputFluid = barrel.getInputFluid();
        int inputAmount = barrel.getInputFluidAmount();
        int inputCapacity = barrel.getInputTankCapacity();

        FluidStack outputFluid = barrel.getOutputFluid();
        int outputAmount = barrel.getOutputFluidAmount();
        int outputCapacity = barrel.getOutputTankCapacity();

        FluidStack fluid;
        int amount;
        int capacity;
        if (outputAmount > inputAmount) {
            fluid = outputFluid;
            amount = outputAmount;
            capacity = outputCapacity;
        } else {
            fluid = inputFluid;
            amount = inputAmount;
            capacity = inputCapacity;
        }

        if (fluid.isEmpty() || amount <= 0 || capacity <= 0) {
            return;
        }

        float fillPercent = Math.min(1.0F, amount / (float) capacity);
        float shrink = fillPercent > 0.03F ? 0.0F : (0.03F - fillPercent) * 5.0F;

        float minX = MIN_X + shrink;
        float minZ = MIN_Z + shrink;
        float maxX = MAX_X - shrink;
        float maxZ = MAX_Z - shrink;
        float y = MIN_Y + (MAX_Y - MIN_Y) * fillPercent;

        renderFluidFace(poseStack, fluid, buffer, minX, minZ, maxX, maxZ, y, packedOverlay, packedLight);
    }

    private static void renderFluidFace(PoseStack poseStack, FluidStack fluidStack, MultiBufferSource buffer, float minX, float minZ, float maxX, float maxZ, float y, int packedOverlay, int packedLight) {
        IClientFluidTypeExtensions fluidExt = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation still = fluidExt.getStillTexture(fluidStack);
        if (still == null) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        int tint = fluidExt.getTintColor(fluidStack);

        int a = (tint >>> 24) & 0xFF;
        int r = (tint >>> 16) & 0xFF;
        int g = (tint >>> 8) & 0xFF;
        int b = tint & 0xFF;
        if (a <= 0) {
            a = 255;
        }

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS));
        Matrix4f matrix = poseStack.last().pose();

        builder.vertex(matrix, minX, y, minZ).color(r, g, b, a).uv(sprite.getU(minX * 16.0F), sprite.getV(minZ * 16.0F)).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, minX, y, maxZ).color(r, g, b, a).uv(sprite.getU(minX * 16.0F), sprite.getV(maxZ * 16.0F)).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, maxX, y, maxZ).color(r, g, b, a).uv(sprite.getU(maxX * 16.0F), sprite.getV(maxZ * 16.0F)).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, maxX, y, minZ).color(r, g, b, a).uv(sprite.getU(maxX * 16.0F), sprite.getV(minZ * 16.0F)).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
    }
}


