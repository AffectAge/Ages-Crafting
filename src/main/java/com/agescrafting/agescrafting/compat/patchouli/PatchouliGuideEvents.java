package com.agescrafting.agescrafting.compat.patchouli;

import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public class PatchouliGuideEvents {
    private static final String GIVEN_FLAG = "agescrafting_patchouli_book_given";

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!AgesCraftingConfig.SERVER.patchouliGiveBookOnFirstJoin.get()) {
            return;
        }
        if (!ModList.get().isLoaded(PatchouliCompatUtil.PATCHOULI_MODID)) {
            return;
        }

        CompoundTag persistent = player.getPersistentData();
        if (persistent.getBoolean(GIVEN_FLAG)) {
            return;
        }

        ItemStack stack = PatchouliCompatUtil.createGuideBookStack();
        if (stack.isEmpty()) {
            return;
        }

        boolean added = player.getInventory().add(stack);
        if (!added) {
            player.drop(stack, false);
        }

        persistent.putBoolean(GIVEN_FLAG, true);
    }
}
