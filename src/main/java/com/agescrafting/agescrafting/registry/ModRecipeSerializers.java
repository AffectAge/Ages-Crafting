package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipe;
import com.agescrafting.agescrafting.dryingrack.DryingRackRecipe;
import com.agescrafting.agescrafting.pitkiln.PitKilnRecipe;
import com.agescrafting.agescrafting.workspace.WorkspaceCraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AgesCraftingMod.MODID);

    public static final RegistryObject<RecipeSerializer<WorkspaceCraftingRecipe>> WORKSPACE_CRAFTING = RECIPE_SERIALIZERS.register(
            "workspace_crafting",
            WorkspaceCraftingRecipe.Serializer::new
    );

    public static final RegistryObject<RecipeSerializer<BarrelRecipe>> BARREL = RECIPE_SERIALIZERS.register(
            "barrel",
            BarrelRecipe.Serializer::new
    );

    public static final RegistryObject<RecipeSerializer<DryingRackRecipe>> DRYING_RACK = RECIPE_SERIALIZERS.register(
            "drying_rack",
            DryingRackRecipe.Serializer::new
    );

    public static final RegistryObject<RecipeSerializer<PitKilnRecipe>> PIT_KILN = RECIPE_SERIALIZERS.register(
            "pit_kiln",
            PitKilnRecipe.Serializer::new
    );
}
