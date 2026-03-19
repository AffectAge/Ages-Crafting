package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.BarrelBlockEntity;
import com.agescrafting.agescrafting.workspace.WorkspaceTableBlockEntity;
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
            () -> BlockEntityType.Builder.of(BarrelBlockEntity::new, ModBlocks.BARREL.get()).build(null)
    );
}
