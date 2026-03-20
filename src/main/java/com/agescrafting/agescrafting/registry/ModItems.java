package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

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

    public static final RegistryObject<Item> SPRUCE_BARREL_ITEM = registerBarrelItem("spruce_barrel", ModBlocks.SPRUCE_BARREL);
    public static final RegistryObject<Item> BIRCH_BARREL_ITEM = registerBarrelItem("birch_barrel", ModBlocks.BIRCH_BARREL);
    public static final RegistryObject<Item> JUNGLE_BARREL_ITEM = registerBarrelItem("jungle_barrel", ModBlocks.JUNGLE_BARREL);
    public static final RegistryObject<Item> ACACIA_BARREL_ITEM = registerBarrelItem("acacia_barrel", ModBlocks.ACACIA_BARREL);
    public static final RegistryObject<Item> DARK_OAK_BARREL_ITEM = registerBarrelItem("dark_oak_barrel", ModBlocks.DARK_OAK_BARREL);
    public static final RegistryObject<Item> MANGROVE_BARREL_ITEM = registerBarrelItem("mangrove_barrel", ModBlocks.MANGROVE_BARREL);
    public static final RegistryObject<Item> CHERRY_BARREL_ITEM = registerBarrelItem("cherry_barrel", ModBlocks.CHERRY_BARREL);
    public static final RegistryObject<Item> BAMBOO_BARREL_ITEM = registerBarrelItem("bamboo_barrel", ModBlocks.BAMBOO_BARREL);
    public static final RegistryObject<Item> CRIMSON_BARREL_ITEM = registerBarrelItem("crimson_barrel", ModBlocks.CRIMSON_BARREL);
    public static final RegistryObject<Item> WARPED_BARREL_ITEM = registerBarrelItem("warped_barrel", ModBlocks.WARPED_BARREL);

    public static final List<RegistryObject<Item>> BARREL_ITEMS = List.of(
            BARREL_ITEM,
            SPRUCE_BARREL_ITEM,
            BIRCH_BARREL_ITEM,
            JUNGLE_BARREL_ITEM,
            ACACIA_BARREL_ITEM,
            DARK_OAK_BARREL_ITEM,
            MANGROVE_BARREL_ITEM,
            CHERRY_BARREL_ITEM,
            BAMBOO_BARREL_ITEM,
            CRIMSON_BARREL_ITEM,
            WARPED_BARREL_ITEM
    );

    private static RegistryObject<Item> registerBarrelItem(String id, RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}