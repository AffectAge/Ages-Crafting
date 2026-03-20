package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AgesCraftingMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.agescrafting.main"))
            .icon(() -> new ItemStack(ModItems.BARREL_ITEM.get()))
            .displayItems((params, output) -> {
                List<RegistryObject<net.minecraft.world.item.Item>> items = new ArrayList<>(ModItems.ITEMS.getEntries());
                items.sort(Comparator.comparing(item -> {
                    ResourceLocation id = item.getId();
                    return id == null ? "" : id.getPath();
                }));

                for (RegistryObject<net.minecraft.world.item.Item> item : items) {
                    output.accept(item.get());
                }
            })
            .build());

    private ModCreativeTabs() {
    }
}
