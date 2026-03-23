package com.agescrafting.agescrafting.compat.jade;

import com.agescrafting.agescrafting.barrel.BarrelBlock;
import com.agescrafting.agescrafting.barrel.BarrelBlockEntity;
import com.agescrafting.agescrafting.campfire.PrimitiveCampfireBlock;
import com.agescrafting.agescrafting.campfire.PrimitiveCampfireBlockEntity;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlock;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlockEntity;
import com.agescrafting.agescrafting.pitkiln.PitKilnBlock;
import com.agescrafting.agescrafting.pitkiln.PitKilnBlockEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class BarrelJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(BarrelJadeProvider.INSTANCE, BarrelBlockEntity.class);
        registration.registerBlockDataProvider(DryingRackJadeProvider.INSTANCE, DryingRackBlockEntity.class);
        registration.registerBlockDataProvider(PrimitiveCampfireJadeProvider.INSTANCE, PrimitiveCampfireBlockEntity.class);
        registration.registerBlockDataProvider(PitKilnJadeProvider.INSTANCE, PitKilnBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BarrelJadeProvider.INSTANCE, BarrelBlock.class);
        registration.registerBlockComponent(DryingRackJadeProvider.INSTANCE, DryingRackBlock.class);
        registration.registerBlockComponent(PrimitiveCampfireJadeProvider.INSTANCE, PrimitiveCampfireBlock.class);
        registration.registerBlockComponent(PitKilnJadeProvider.INSTANCE, PitKilnBlock.class);
        registration.registerBlockComponent(PitKilnJadeProvider.INSTANCE, BaseFireBlock.class);
    }
}
