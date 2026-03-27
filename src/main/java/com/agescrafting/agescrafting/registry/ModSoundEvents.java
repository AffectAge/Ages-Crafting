package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AgesCraftingMod.MODID);

    public static final RegistryObject<SoundEvent> BARREL_SEAL = register("barrel.seal");
    public static final RegistryObject<SoundEvent> BARREL_UNSEAL = register("barrel.unseal");

    private ModSoundEvents() {
    }

    private static RegistryObject<SoundEvent> register(String id) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, id);
        return SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(location));
    }
}