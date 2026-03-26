package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.anvil.AnvilRecipe;
import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipe;
import com.agescrafting.agescrafting.choppingblock.ChoppingBlockRecipe;
import com.agescrafting.agescrafting.dryingrack.DryingRackRecipe;
import com.agescrafting.agescrafting.pitkiln.PitKilnRecipe;
import com.agescrafting.agescrafting.workspace.WorkspaceCraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, AgesCraftingMod.MODID);

    public static final RegistryObject<RecipeType<WorkspaceCraftingRecipe>> WORKSPACE_CRAFTING = RECIPE_TYPES.register(
            "workspace_crafting",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return AgesCraftingMod.MODID + ":workspace_crafting";
                }
            }
    );

    public static final RegistryObject<RecipeType<AnvilRecipe>> ANVIL = RECIPE_TYPES.register(
            "anvil",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return AgesCraftingMod.MODID + ":anvil";
                }
            }
    );

    public static final RegistryObject<RecipeType<BarrelRecipe>> BARREL = RECIPE_TYPES.register(
            "barrel",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return AgesCraftingMod.MODID + ":barrel";
                }
            }
    );

    public static final RegistryObject<RecipeType<DryingRackRecipe>> DRYING_RACK = RECIPE_TYPES.register(
            "drying_rack",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return AgesCraftingMod.MODID + ":drying_rack";
                }
            }
    );

    public static final RegistryObject<RecipeType<PitKilnRecipe>> PIT_KILN = RECIPE_TYPES.register(
            "pit_kiln",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return AgesCraftingMod.MODID + ":pit_kiln";
                }
            }
    );

    public static final RegistryObject<RecipeType<ChoppingBlockRecipe>> CHOPPING_BLOCK = RECIPE_TYPES.register(
            "chopping_block",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return AgesCraftingMod.MODID + ":chopping_block";
                }
            }
    );
}
