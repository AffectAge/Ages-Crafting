package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.BarrelBlock;
import com.agescrafting.agescrafting.campfire.PrimitiveCampfireBlock;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlock;
import com.agescrafting.agescrafting.pitkiln.PitKilnBlock;
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

    public static final RegistryObject<Block> PRIMITIVE_CAMPFIRE = BLOCKS.register(
            "primitive_campfire",
            () -> new PrimitiveCampfireBlock(BlockBehaviour.Properties.of().strength(0.5F).noOcclusion())
    );

    public static final RegistryObject<Block> PIT_KILN = BLOCKS.register(
            "pit_kiln",
            () -> new PitKilnBlock(BlockBehaviour.Properties.of().strength(0.8F).noOcclusion())
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

    private static RegistryObject<Block> registerBarrel(String id) {
        return BLOCKS.register(id, () -> new BarrelBlock(BlockBehaviour.Properties.of().strength(2.5F)));
    }

    private static RegistryObject<Block> registerDryingRack(String id) {
        return BLOCKS.register(id, () -> new DryingRackBlock(BlockBehaviour.Properties.of().strength(1.5F).noOcclusion()));
    }
}
