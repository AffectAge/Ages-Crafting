package com.agescrafting.agescrafting.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class DeviceRecipeSounds {
    private DeviceRecipeSounds() {
    }

    public static void playStart(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
    }
    public static void playFinish(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 0.95F + level.random.nextFloat() * 0.1F);
    }
}


