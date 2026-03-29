package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.anvil.AnvilBlock;
import com.agescrafting.agescrafting.barrel.BarrelBlock;
import com.agescrafting.agescrafting.campfire.PrimitiveCampfireBlock;
import com.agescrafting.agescrafting.choppingblock.ChoppingBlockBlock;
import com.agescrafting.agescrafting.choppingblock.WoodChipsPileBlock;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlock;
import com.agescrafting.agescrafting.pitkiln.PitKilnBlock;
import com.agescrafting.agescrafting.tanningrack.TanningRackBlock;
import com.agescrafting.agescrafting.workspace.WorkspaceTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AgesCraftingMod.MODID);

    public static final RegistryObject<Block> WORKSPACE_TABLE = BLOCKS.register(
            "workspace_table",
            () -> new WorkspaceTableBlock(BlockBehaviour.Properties.of().strength(2.0F).noOcclusion())
    );

    public static final RegistryObject<Block> STONE_ANVIL = registerAnvil("stone_anvil");
    public static final RegistryObject<Block> GRANITE_ANVIL = registerAnvil("granite_anvil");
    public static final RegistryObject<Block> DIORITE_ANVIL = registerAnvil("diorite_anvil");
    public static final RegistryObject<Block> ANDESITE_ANVIL = registerAnvil("andesite_anvil");
    public static final RegistryObject<Block> DEEPSLATE_ANVIL = registerAnvil("deepslate_anvil");
    public static final RegistryObject<Block> BASALT_ANVIL = registerAnvil("basalt_anvil");
    public static final RegistryObject<Block> OBSIDIAN_ANVIL = registerAnvil("obsidian_anvil");

    public static final List<RegistryObject<Block>> ANVIL_BLOCKS = List.of(
            STONE_ANVIL,
            GRANITE_ANVIL,
            DIORITE_ANVIL,
            ANDESITE_ANVIL,
            DEEPSLATE_ANVIL,
            BASALT_ANVIL,
            OBSIDIAN_ANVIL
    );

    public static final RegistryObject<Block> BARREL = BLOCKS.register(
            "barrel",
            () -> new BarrelBlock(BlockBehaviour.Properties.of().strength(2.5F))
    );

    public static final RegistryObject<Block> SPRUCE_BARREL = registerBarrel("spruce_barrel");
    public static final RegistryObject<Block> BIRCH_BARREL = registerBarrel("birch_barrel");
    public static final RegistryObject<Block> JUNGLE_BARREL = registerBarrel("jungle_barrel");
    public static final RegistryObject<Block> ACACIA_BARREL = registerBarrel("acacia_barrel");
    public static final RegistryObject<Block> DARK_OAK_BARREL = registerBarrel("dark_oak_barrel");
    public static final RegistryObject<Block> MANGROVE_BARREL = registerBarrel("mangrove_barrel");
    public static final RegistryObject<Block> CHERRY_BARREL = registerBarrel("cherry_barrel");
    public static final RegistryObject<Block> BAMBOO_BARREL = registerBarrel("bamboo_barrel");
    public static final RegistryObject<Block> CRIMSON_BARREL = registerBarrel("crimson_barrel");
    public static final RegistryObject<Block> WARPED_BARREL = registerBarrel("warped_barrel");

    public static final List<RegistryObject<Block>> BARREL_BLOCKS = List.of(
            BARREL,
            SPRUCE_BARREL,
            BIRCH_BARREL,
            JUNGLE_BARREL,
            ACACIA_BARREL,
            DARK_OAK_BARREL,
            MANGROVE_BARREL,
            CHERRY_BARREL,
            BAMBOO_BARREL,
            CRIMSON_BARREL,
            WARPED_BARREL
    );

    public static final RegistryObject<Block> DRYING_RACK = BLOCKS.register(
            "drying_rack",
            () -> new DryingRackBlock(BlockBehaviour.Properties.of().strength(1.5F).noOcclusion())
    );

    public static final RegistryObject<Block> SPRUCE_DRYING_RACK = registerDryingRack("spruce_drying_rack");
    public static final RegistryObject<Block> BIRCH_DRYING_RACK = registerDryingRack("birch_drying_rack");
    public static final RegistryObject<Block> JUNGLE_DRYING_RACK = registerDryingRack("jungle_drying_rack");
    public static final RegistryObject<Block> ACACIA_DRYING_RACK = registerDryingRack("acacia_drying_rack");
    public static final RegistryObject<Block> DARK_OAK_DRYING_RACK = registerDryingRack("dark_oak_drying_rack");
    public static final RegistryObject<Block> MANGROVE_DRYING_RACK = registerDryingRack("mangrove_drying_rack");
    public static final RegistryObject<Block> CHERRY_DRYING_RACK = registerDryingRack("cherry_drying_rack");
    public static final RegistryObject<Block> BAMBOO_DRYING_RACK = registerDryingRack("bamboo_drying_rack");
    public static final RegistryObject<Block> CRIMSON_DRYING_RACK = registerDryingRack("crimson_drying_rack");
    public static final RegistryObject<Block> WARPED_DRYING_RACK = registerDryingRack("warped_drying_rack");

    public static final List<RegistryObject<Block>> DRYING_RACK_BLOCKS = List.of(
            DRYING_RACK,
            SPRUCE_DRYING_RACK,
            BIRCH_DRYING_RACK,
            JUNGLE_DRYING_RACK,
            ACACIA_DRYING_RACK,
            DARK_OAK_DRYING_RACK,
            MANGROVE_DRYING_RACK,
            CHERRY_DRYING_RACK,
            BAMBOO_DRYING_RACK,
            CRIMSON_DRYING_RACK,
            WARPED_DRYING_RACK
    );

    public static final RegistryObject<Block> TANNING_RACK = BLOCKS.register(
            "tanning_rack",
            () -> new TanningRackBlock(BlockBehaviour.Properties.of().strength(1.5F).noOcclusion())
    );

    public static final RegistryObject<Block> SPRUCE_TANNING_RACK = registerTanningRack("spruce_tanning_rack");
    public static final RegistryObject<Block> BIRCH_TANNING_RACK = registerTanningRack("birch_tanning_rack");
    public static final RegistryObject<Block> JUNGLE_TANNING_RACK = registerTanningRack("jungle_tanning_rack");
    public static final RegistryObject<Block> ACACIA_TANNING_RACK = registerTanningRack("acacia_tanning_rack");
    public static final RegistryObject<Block> DARK_OAK_TANNING_RACK = registerTanningRack("dark_oak_tanning_rack");
    public static final RegistryObject<Block> MANGROVE_TANNING_RACK = registerTanningRack("mangrove_tanning_rack");
    public static final RegistryObject<Block> CHERRY_TANNING_RACK = registerTanningRack("cherry_tanning_rack");
    public static final RegistryObject<Block> BAMBOO_TANNING_RACK = registerTanningRack("bamboo_tanning_rack");
    public static final RegistryObject<Block> CRIMSON_TANNING_RACK = registerTanningRack("crimson_tanning_rack");
    public static final RegistryObject<Block> WARPED_TANNING_RACK = registerTanningRack("warped_tanning_rack");

    public static final List<RegistryObject<Block>> TANNING_RACK_BLOCKS = List.of(
            TANNING_RACK,
            SPRUCE_TANNING_RACK,
            BIRCH_TANNING_RACK,
            JUNGLE_TANNING_RACK,
            ACACIA_TANNING_RACK,
            DARK_OAK_TANNING_RACK,
            MANGROVE_TANNING_RACK,
            CHERRY_TANNING_RACK,
            BAMBOO_TANNING_RACK,
            CRIMSON_TANNING_RACK,
            WARPED_TANNING_RACK
    );

    public static final RegistryObject<Block> PRIMITIVE_CAMPFIRE = BLOCKS.register(
            "primitive_campfire",
            () -> new PrimitiveCampfireBlock(BlockBehaviour.Properties.of().strength(0.5F).noOcclusion())
    );

    public static final RegistryObject<Block> PIT_KILN = BLOCKS.register(
            "pit_kiln",
            () -> new PitKilnBlock(BlockBehaviour.Properties.of().strength(0.8F).noOcclusion())
    );

    public static final RegistryObject<Block> CHOPPING_BLOCK = BLOCKS.register(
            "chopping_block",
            () -> new ChoppingBlockBlock(BlockBehaviour.Properties.of().strength(1.8F).noOcclusion())
    );

    public static final RegistryObject<Block> SPRUCE_CHOPPING_BLOCK = registerChoppingBlock("spruce_chopping_block");
    public static final RegistryObject<Block> BIRCH_CHOPPING_BLOCK = registerChoppingBlock("birch_chopping_block");
    public static final RegistryObject<Block> JUNGLE_CHOPPING_BLOCK = registerChoppingBlock("jungle_chopping_block");
    public static final RegistryObject<Block> ACACIA_CHOPPING_BLOCK = registerChoppingBlock("acacia_chopping_block");
    public static final RegistryObject<Block> DARK_OAK_CHOPPING_BLOCK = registerChoppingBlock("dark_oak_chopping_block");
    public static final RegistryObject<Block> MANGROVE_CHOPPING_BLOCK = registerChoppingBlock("mangrove_chopping_block");
    public static final RegistryObject<Block> CHERRY_CHOPPING_BLOCK = registerChoppingBlock("cherry_chopping_block");
    public static final RegistryObject<Block> BAMBOO_CHOPPING_BLOCK = registerChoppingBlock("bamboo_chopping_block");
    public static final RegistryObject<Block> CRIMSON_CHOPPING_BLOCK = registerChoppingBlock("crimson_chopping_block");
    public static final RegistryObject<Block> WARPED_CHOPPING_BLOCK = registerChoppingBlock("warped_chopping_block");

    public static final List<RegistryObject<Block>> CHOPPING_BLOCKS = List.of(
            CHOPPING_BLOCK,
            SPRUCE_CHOPPING_BLOCK,
            BIRCH_CHOPPING_BLOCK,
            JUNGLE_CHOPPING_BLOCK,
            ACACIA_CHOPPING_BLOCK,
            DARK_OAK_CHOPPING_BLOCK,
            MANGROVE_CHOPPING_BLOCK,
            CHERRY_CHOPPING_BLOCK,
            BAMBOO_CHOPPING_BLOCK,
            CRIMSON_CHOPPING_BLOCK,
            WARPED_CHOPPING_BLOCK
    );

    public static final RegistryObject<Block> WOOD_CHIPS_PILE = BLOCKS.register(
            "wood_chips_pile",
            () -> new WoodChipsPileBlock(BlockBehaviour.Properties.of().strength(0.2F).noOcclusion())
    );

    private static RegistryObject<Block> registerBarrel(String id) {
        return BLOCKS.register(id, () -> new BarrelBlock(BlockBehaviour.Properties.of().strength(2.5F)));
    }

    private static RegistryObject<Block> registerDryingRack(String id) {
        return BLOCKS.register(id, () -> new DryingRackBlock(BlockBehaviour.Properties.of().strength(1.5F).noOcclusion()));
    }

    private static RegistryObject<Block> registerTanningRack(String id) {
        return BLOCKS.register(id, () -> new TanningRackBlock(BlockBehaviour.Properties.of().strength(1.5F).noOcclusion()));
    }

    private static RegistryObject<Block> registerChoppingBlock(String id) {
        return BLOCKS.register(id, () -> new ChoppingBlockBlock(BlockBehaviour.Properties.of().strength(1.8F).noOcclusion()));
    }

    private static RegistryObject<Block> registerAnvil(String id) {
        return BLOCKS.register(id, () -> new AnvilBlock(BlockBehaviour.Properties.of().strength(3.0F).noOcclusion()));
    }
}
