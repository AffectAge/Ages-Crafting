package com.agescrafting.agescrafting.compat.kubejs;

import com.agescrafting.agescrafting.AgesCraftingMod;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import net.minecraft.resources.ResourceLocation;

public class AgesCraftingKubeJsPlugin extends KubeJSPlugin {
    private static final ResourceLocation WORKSPACE_CRAFTING_TYPE = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "workspace_crafting");
    private static final ResourceLocation PIT_KILN_TYPE = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "pit_kiln");

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(WORKSPACE_CRAFTING_TYPE, JsonRecipeSchema.SCHEMA);
        event.register(PIT_KILN_TYPE, JsonRecipeSchema.SCHEMA);

        event.namespace(AgesCraftingMod.MODID).register("workspace_crafting", JsonRecipeSchema.SCHEMA);
        event.namespace(AgesCraftingMod.MODID).register("workspace", JsonRecipeSchema.SCHEMA);
        event.namespace(AgesCraftingMod.MODID).register("pit_kiln", JsonRecipeSchema.SCHEMA);

        event.mapRecipe(AgesCraftingMod.MODID + ":workspace_crafting", WORKSPACE_CRAFTING_TYPE);
        event.mapRecipe(AgesCraftingMod.MODID + ":workspace", WORKSPACE_CRAFTING_TYPE);
        event.mapRecipe(AgesCraftingMod.MODID + ":pit_kiln", PIT_KILN_TYPE);
    }
}
