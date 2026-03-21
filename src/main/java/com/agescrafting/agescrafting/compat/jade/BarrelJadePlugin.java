package com.agescrafting.agescrafting.compat.jade;

import com.agescrafting.agescrafting.barrel.BarrelBlock;
import com.agescrafting.agescrafting.barrel.BarrelBlockEntity;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlock;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlockEntity;
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
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BarrelJadeProvider.INSTANCE, BarrelBlock.class);
        registration.registerBlockComponent(DryingRackJadeProvider.INSTANCE, DryingRackBlock.class);
    }
}
