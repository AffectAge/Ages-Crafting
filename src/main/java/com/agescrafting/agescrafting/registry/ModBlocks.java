package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.BarrelBlock;
import com.agescrafting.agescrafting.workspace.WorkspaceTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
}
