package com.agescrafting.agescrafting.workspace;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AgesCraftingMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorkspaceClientForgeEvents {
    @SubscribeEvent
    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;

        if (level == null || player == null) {
            return;
        }

        BlockHitResult target = event.getTarget();
        BlockPos pos = target.getBlockPos();
        if (!(level.getBlockState(pos).getBlock() instanceof WorkspaceTableBlock)) {
            return;
        }

        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        int[] slot = WorkspaceTableBlock.getPosition(target, held.isEmpty());
        if (!WorkspaceTableBlock.validPosition(slot)) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        double minX = pos.getX() + slot[0] / 3.0;
        double minY = pos.getY() + slot[1] / 3.0;
        double minZ = pos.getZ() + slot[2] / 3.0;
        AABB aabb = new AABB(minX, minY, minZ, minX + 1 / 3.0, minY + 1 / 3.0, minZ + 1 / 3.0);

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        LevelRenderer.renderLineBox(
                poseStack,
                event.getMultiBufferSource().getBuffer(RenderType.lines()),
                aabb,
                0.15F,
                0.95F,
                0.2F,
                1.0F
        );
        poseStack.popPose();
    }
}
