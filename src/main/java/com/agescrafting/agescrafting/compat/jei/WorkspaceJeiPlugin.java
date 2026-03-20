package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import com.agescrafting.agescrafting.workspace.WorkspaceCraftingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

@JeiPlugin
public class WorkspaceJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new WorkspaceJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new BarrelJeiCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        registration.addRecipes(
                WorkspaceJeiCategory.TYPE,
                new ArrayList<>(level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.WORKSPACE_CRAFTING.get()))
        );

        registration.addRecipes(
                BarrelJeiCategory.TYPE,
                new ArrayList<>(level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.BARREL.get()))
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WORKSPACE_TABLE.get()), WorkspaceJeiCategory.TYPE);
        for (var barrel : ModBlocks.BARREL_BLOCKS) {
            registration.addRecipeCatalyst(new ItemStack(barrel.get()), BarrelJeiCategory.TYPE);
        }
    }
}
