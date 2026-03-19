package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AgesCraftingMod.MODID);

    public static final RegistryObject<Item> WORKSPACE_TABLE_ITEM = ITEMS.register(
            "workspace_table",
            () -> new BlockItem(ModBlocks.WORKSPACE_TABLE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> BARREL_ITEM = ITEMS.register(
            "barrel",
            () -> new BlockItem(ModBlocks.BARREL.get(), new Item.Properties())
    );
}
