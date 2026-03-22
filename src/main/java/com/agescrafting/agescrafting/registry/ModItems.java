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

    public static final RegistryObject<Item> DRYING_RACK_ITEM = registerBlockItem("drying_rack", ModBlocks.DRYING_RACK);
    public static final RegistryObject<Item> PRIMITIVE_CAMPFIRE_ITEM = registerBlockItem("primitive_campfire", ModBlocks.PRIMITIVE_CAMPFIRE);
    public static final RegistryObject<Item> SPRUCE_DRYING_RACK_ITEM = registerBlockItem("spruce_drying_rack", ModBlocks.SPRUCE_DRYING_RACK);
    public static final RegistryObject<Item> BIRCH_DRYING_RACK_ITEM = registerBlockItem("birch_drying_rack", ModBlocks.BIRCH_DRYING_RACK);
    public static final RegistryObject<Item> JUNGLE_DRYING_RACK_ITEM = registerBlockItem("jungle_drying_rack", ModBlocks.JUNGLE_DRYING_RACK);
    public static final RegistryObject<Item> ACACIA_DRYING_RACK_ITEM = registerBlockItem("acacia_drying_rack", ModBlocks.ACACIA_DRYING_RACK);
    public static final RegistryObject<Item> DARK_OAK_DRYING_RACK_ITEM = registerBlockItem("dark_oak_drying_rack", ModBlocks.DARK_OAK_DRYING_RACK);
    public static final RegistryObject<Item> MANGROVE_DRYING_RACK_ITEM = registerBlockItem("mangrove_drying_rack", ModBlocks.MANGROVE_DRYING_RACK);
    public static final RegistryObject<Item> CHERRY_DRYING_RACK_ITEM = registerBlockItem("cherry_drying_rack", ModBlocks.CHERRY_DRYING_RACK);
    public static final RegistryObject<Item> BAMBOO_DRYING_RACK_ITEM = registerBlockItem("bamboo_drying_rack", ModBlocks.BAMBOO_DRYING_RACK);
    public static final RegistryObject<Item> CRIMSON_DRYING_RACK_ITEM = registerBlockItem("crimson_drying_rack", ModBlocks.CRIMSON_DRYING_RACK);
    public static final RegistryObject<Item> WARPED_DRYING_RACK_ITEM = registerBlockItem("warped_drying_rack", ModBlocks.WARPED_DRYING_RACK);

    public static final List<RegistryObject<Item>> DRYING_RACK_ITEMS = List.of(
            DRYING_RACK_ITEM,
            SPRUCE_DRYING_RACK_ITEM,
            BIRCH_DRYING_RACK_ITEM,
            JUNGLE_DRYING_RACK_ITEM,
            ACACIA_DRYING_RACK_ITEM,
            DARK_OAK_DRYING_RACK_ITEM,
            MANGROVE_DRYING_RACK_ITEM,
            CHERRY_DRYING_RACK_ITEM,
            BAMBOO_DRYING_RACK_ITEM,
            CRIMSON_DRYING_RACK_ITEM,
            WARPED_DRYING_RACK_ITEM
    );

    private static RegistryObject<Item> registerBarrelItem(String id, RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Item> registerBlockItem(String id, RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
