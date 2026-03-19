package com.agescrafting.agescrafting.workspace;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class WorkspaceTableRenderer implements BlockEntityRenderer<WorkspaceTableBlockEntity> {
    private static final float SLOT = 1.0f / 3.0f;
    private static final float INSET = 0.004f;
    private static final float RENDER_SIZE = SLOT - INSET * 2.0f;
    private static final double MAX_RENDER_DISTANCE_SQR = 32.0 * 32.0;
    private static final double MIN_VIEW_DOT = -0.15;

    public WorkspaceTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull WorkspaceTableBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Vector3f lookVec = camera.getLookVector();

        for (int i = 0; i < 27; i++) {
            ItemStack stack = blockEntity.getItems().get(i);
            if (stack.isEmpty()) {
                continue;
            }

            int[] pos = WorkspaceTableBlock.getPosition(i);
            if (pos == null) {
                continue;
            }

            float x = pos[0] * SLOT + INSET + RENDER_SIZE * 0.5f;
            float y = pos[1] * SLOT + INSET + RENDER_SIZE * 0.5f;
            float z = pos[2] * SLOT + INSET + RENDER_SIZE * 0.5f;
            Vec3 slotWorldPos = new Vec3(
                    blockEntity.getBlockPos().getX() + x,
                    blockEntity.getBlockPos().getY() + y,
                    blockEntity.getBlockPos().getZ() + z
            );

            if (!shouldRenderSlot(slotWorldPos, cameraPos, lookVec)) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.scale(RENDER_SIZE, RENDER_SIZE, RENDER_SIZE);

            itemRenderer.renderStatic(stack, ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, blockEntity.getLevel(), i);
            poseStack.popPose();
        }
    }

    private boolean shouldRenderSlot(Vec3 slotWorldPos, Vec3 cameraPos, Vector3f lookVec) {
        Vec3 toSlot = slotWorldPos.subtract(cameraPos);
        double distSqr = toSlot.lengthSqr();
        if (distSqr > MAX_RENDER_DISTANCE_SQR) {
            return false;
        }

        double dist = Math.sqrt(distSqr);
        if (dist < 1.0e-6) {
            return true;
        }

        double dot = (toSlot.x / dist) * lookVec.x + (toSlot.y / dist) * lookVec.y + (toSlot.z / dist) * lookVec.z;
        return dot >= MIN_VIEW_DOT;
    }
}
