package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.BarrelBlockEntity;
import com.agescrafting.agescrafting.campfire.PrimitiveCampfireBlockEntity;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlockEntity;
import com.agescrafting.agescrafting.workspace.WorkspaceTableBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AgesCraftingMod.MODID);

    public static final RegistryObject<BlockEntityType<WorkspaceTableBlockEntity>> WORKSPACE_TABLE_BE = BLOCK_ENTITIES.register(
            "workspace_table",
            () -> BlockEntityType.Builder.of(WorkspaceTableBlockEntity::new, ModBlocks.WORKSPACE_TABLE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BarrelBlockEntity>> BARREL_BE = BLOCK_ENTITIES.register(
            "barrel",
            () -> BlockEntityType.Builder.of(BarrelBlockEntity::new, ModBlocks.BARREL_BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new)).build(null)
    );

    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BE = BLOCK_ENTITIES.register(
            "drying_rack",
            () -> BlockEntityType.Builder.of(DryingRackBlockEntity::new, ModBlocks.DRYING_RACK_BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new)).build(null)
    );

    public static final RegistryObject<BlockEntityType<PrimitiveCampfireBlockEntity>> PRIMITIVE_CAMPFIRE_BE = BLOCK_ENTITIES.register(
            "primitive_campfire",
            () -> BlockEntityType.Builder.of(PrimitiveCampfireBlockEntity::new, ModBlocks.PRIMITIVE_CAMPFIRE.get()).build(null)
    );
}
