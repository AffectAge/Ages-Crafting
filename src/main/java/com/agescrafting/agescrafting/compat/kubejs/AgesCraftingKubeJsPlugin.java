package com.agescrafting.agescrafting.compat.kubejs;

import com.agescrafting.agescrafting.AgesCraftingMod;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import net.minecraft.resources.ResourceLocation;

public class AgesCraftingKubeJsPlugin extends KubeJSPlugin {
    private static final ResourceLocation WORKSPACE_CRAFTING_TYPE = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "workspace_crafting");

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        // Register schema for the custom recipe type so scripts can add/remove it in KubeJS.
        event.register(WORKSPACE_CRAFTING_TYPE, JsonRecipeSchema.SCHEMA);

        // Namespace functions: event.recipes.agescrafting.workspace_crafting(...)
        event.namespace(AgesCraftingMod.MODID).register("workspace_crafting", JsonRecipeSchema.SCHEMA);

        // Short alias: event.recipes.agescrafting.workspace(...)
        event.namespace(AgesCraftingMod.MODID).register("workspace", JsonRecipeSchema.SCHEMA);

        // Explicit map aliases to the real type id.
        event.mapRecipe(AgesCraftingMod.MODID + ":workspace_crafting", WORKSPACE_CRAFTING_TYPE);
        event.mapRecipe(AgesCraftingMod.MODID + ":workspace", WORKSPACE_CRAFTING_TYPE);
    }
}
