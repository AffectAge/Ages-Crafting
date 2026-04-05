package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.BarrelBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AgesCraftingMod.MODID);

    public static final RegistryObject<Item> WORKSPACE_TABLE_ITEM = ITEMS.register(
            "workspace_table",
            () -> new BlockItem(ModBlocks.WORKSPACE_TABLE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> STONE_ANVIL_ITEM = registerBlockItem("stone_anvil", ModBlocks.STONE_ANVIL);
    public static final RegistryObject<Item> GRANITE_ANVIL_ITEM = registerBlockItem("granite_anvil", ModBlocks.GRANITE_ANVIL);
    public static final RegistryObject<Item> DIORITE_ANVIL_ITEM = registerBlockItem("diorite_anvil", ModBlocks.DIORITE_ANVIL);
    public static final RegistryObject<Item> ANDESITE_ANVIL_ITEM = registerBlockItem("andesite_anvil", ModBlocks.ANDESITE_ANVIL);
    public static final RegistryObject<Item> DEEPSLATE_ANVIL_ITEM = registerBlockItem("deepslate_anvil", ModBlocks.DEEPSLATE_ANVIL);
    public static final RegistryObject<Item> BASALT_ANVIL_ITEM = registerBlockItem("basalt_anvil", ModBlocks.BASALT_ANVIL);
    public static final RegistryObject<Item> OBSIDIAN_ANVIL_ITEM = registerBlockItem("obsidian_anvil", ModBlocks.OBSIDIAN_ANVIL);

    public static final List<RegistryObject<Item>> ANVIL_ITEMS = List.of(
            STONE_ANVIL_ITEM,
            GRANITE_ANVIL_ITEM,
            DIORITE_ANVIL_ITEM,
            ANDESITE_ANVIL_ITEM,
            DEEPSLATE_ANVIL_ITEM,
            BASALT_ANVIL_ITEM,
            OBSIDIAN_ANVIL_ITEM
    );

    public static final RegistryObject<Item> BARREL_ITEM = ITEMS.register(
            "barrel",
            () -> new BarrelBlockItem(ModBlocks.BARREL.get(), new Item.Properties())
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

    public static final RegistryObject<Item> TANNING_RACK_ITEM = registerBlockItem("tanning_rack", ModBlocks.TANNING_RACK);
    public static final RegistryObject<Item> SPRUCE_TANNING_RACK_ITEM = registerBlockItem("spruce_tanning_rack", ModBlocks.SPRUCE_TANNING_RACK);
    public static final RegistryObject<Item> BIRCH_TANNING_RACK_ITEM = registerBlockItem("birch_tanning_rack", ModBlocks.BIRCH_TANNING_RACK);
    public static final RegistryObject<Item> JUNGLE_TANNING_RACK_ITEM = registerBlockItem("jungle_tanning_rack", ModBlocks.JUNGLE_TANNING_RACK);
    public static final RegistryObject<Item> ACACIA_TANNING_RACK_ITEM = registerBlockItem("acacia_tanning_rack", ModBlocks.ACACIA_TANNING_RACK);
    public static final RegistryObject<Item> DARK_OAK_TANNING_RACK_ITEM = registerBlockItem("dark_oak_tanning_rack", ModBlocks.DARK_OAK_TANNING_RACK);
    public static final RegistryObject<Item> MANGROVE_TANNING_RACK_ITEM = registerBlockItem("mangrove_tanning_rack", ModBlocks.MANGROVE_TANNING_RACK);
    public static final RegistryObject<Item> CHERRY_TANNING_RACK_ITEM = registerBlockItem("cherry_tanning_rack", ModBlocks.CHERRY_TANNING_RACK);
    public static final RegistryObject<Item> BAMBOO_TANNING_RACK_ITEM = registerBlockItem("bamboo_tanning_rack", ModBlocks.BAMBOO_TANNING_RACK);
    public static final RegistryObject<Item> CRIMSON_TANNING_RACK_ITEM = registerBlockItem("crimson_tanning_rack", ModBlocks.CRIMSON_TANNING_RACK);
    public static final RegistryObject<Item> WARPED_TANNING_RACK_ITEM = registerBlockItem("warped_tanning_rack", ModBlocks.WARPED_TANNING_RACK);

    public static final List<RegistryObject<Item>> TANNING_RACK_ITEMS = List.of(
            TANNING_RACK_ITEM,
            SPRUCE_TANNING_RACK_ITEM,
            BIRCH_TANNING_RACK_ITEM,
            JUNGLE_TANNING_RACK_ITEM,
            ACACIA_TANNING_RACK_ITEM,
            DARK_OAK_TANNING_RACK_ITEM,
            MANGROVE_TANNING_RACK_ITEM,
            CHERRY_TANNING_RACK_ITEM,
            BAMBOO_TANNING_RACK_ITEM,
            CRIMSON_TANNING_RACK_ITEM,
            WARPED_TANNING_RACK_ITEM
    );

    public static final RegistryObject<Item> PRIMITIVE_CAMPFIRE_ITEM = registerBlockItem("primitive_campfire", ModBlocks.PRIMITIVE_CAMPFIRE);
    public static final RegistryObject<Item> PIT_KILN_ITEM = registerBlockItem("pit_kiln", ModBlocks.PIT_KILN);
    public static final RegistryObject<Item> CHOPPING_BLOCK_ITEM = registerBlockItem("chopping_block", ModBlocks.CHOPPING_BLOCK);

    public static final RegistryObject<Item> SPRUCE_CHOPPING_BLOCK_ITEM = registerBlockItem("spruce_chopping_block", ModBlocks.SPRUCE_CHOPPING_BLOCK);
    public static final RegistryObject<Item> BIRCH_CHOPPING_BLOCK_ITEM = registerBlockItem("birch_chopping_block", ModBlocks.BIRCH_CHOPPING_BLOCK);
    public static final RegistryObject<Item> JUNGLE_CHOPPING_BLOCK_ITEM = registerBlockItem("jungle_chopping_block", ModBlocks.JUNGLE_CHOPPING_BLOCK);
    public static final RegistryObject<Item> ACACIA_CHOPPING_BLOCK_ITEM = registerBlockItem("acacia_chopping_block", ModBlocks.ACACIA_CHOPPING_BLOCK);
    public static final RegistryObject<Item> DARK_OAK_CHOPPING_BLOCK_ITEM = registerBlockItem("dark_oak_chopping_block", ModBlocks.DARK_OAK_CHOPPING_BLOCK);
    public static final RegistryObject<Item> MANGROVE_CHOPPING_BLOCK_ITEM = registerBlockItem("mangrove_chopping_block", ModBlocks.MANGROVE_CHOPPING_BLOCK);
    public static final RegistryObject<Item> CHERRY_CHOPPING_BLOCK_ITEM = registerBlockItem("cherry_chopping_block", ModBlocks.CHERRY_CHOPPING_BLOCK);
    public static final RegistryObject<Item> BAMBOO_CHOPPING_BLOCK_ITEM = registerBlockItem("bamboo_chopping_block", ModBlocks.BAMBOO_CHOPPING_BLOCK);
    public static final RegistryObject<Item> CRIMSON_CHOPPING_BLOCK_ITEM = registerBlockItem("crimson_chopping_block", ModBlocks.CRIMSON_CHOPPING_BLOCK);
    public static final RegistryObject<Item> WARPED_CHOPPING_BLOCK_ITEM = registerBlockItem("warped_chopping_block", ModBlocks.WARPED_CHOPPING_BLOCK);

    public static final List<RegistryObject<Item>> CHOPPING_BLOCK_ITEMS = List.of(
            CHOPPING_BLOCK_ITEM,
            SPRUCE_CHOPPING_BLOCK_ITEM,
            BIRCH_CHOPPING_BLOCK_ITEM,
            JUNGLE_CHOPPING_BLOCK_ITEM,
            ACACIA_CHOPPING_BLOCK_ITEM,
            DARK_OAK_CHOPPING_BLOCK_ITEM,
            MANGROVE_CHOPPING_BLOCK_ITEM,
            CHERRY_CHOPPING_BLOCK_ITEM,
            BAMBOO_CHOPPING_BLOCK_ITEM,
            CRIMSON_CHOPPING_BLOCK_ITEM,
            WARPED_CHOPPING_BLOCK_ITEM
    );
    public static final RegistryObject<Item> TANNIN_BUCKET = ITEMS.register("tannin_bucket",
            () -> new BucketItem(ModFluids.SOURCE_TANNIN, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET))
    );

    public static final RegistryObject<Item> RAW_HIDE = ITEMS.register("raw_hide", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCRAPED_HIDE = ITEMS.register("scraped_hide", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TANNED_LEATHER = ITEMS.register("tanned_leather", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ASH = ITEMS.register("ash", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WOOD_CHIPS = ITEMS.register("wood_chips", () -> new Item(new Item.Properties()) {
        @Override
        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
            return 200;
        }
    });

    private static RegistryObject<Item> registerBarrelItem(String id, RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(id, () -> new BarrelBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Item> registerBlockItem(String id, RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}

