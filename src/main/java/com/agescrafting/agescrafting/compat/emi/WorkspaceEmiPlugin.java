package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import com.agescrafting.agescrafting.workspace.WorkspaceCraftingRecipe;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class WorkspaceEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory WORKSPACE_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "workspace_crafting"),
            EmiStack.of(ModBlocks.WORKSPACE_TABLE.get())
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(WORKSPACE_CATEGORY);
        registry.addWorkstation(WORKSPACE_CATEGORY, EmiStack.of(ModBlocks.WORKSPACE_TABLE.get()));

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        for (WorkspaceCraftingRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.WORKSPACE_CRAFTING.get())) {
            registry.addRecipe(new WorkspaceEmiRecipe(recipe, WORKSPACE_CATEGORY));
        }
    }
}
