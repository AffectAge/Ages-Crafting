package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.compat.campfire.PrimitiveCampfireDisplayRecipe;
import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.registry.ModItems;
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
import net.minecraft.world.item.crafting.RecipeType;
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
                new AnvilJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new BarrelJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new DryingRackJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new PrimitiveCampfireJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new PitKilnJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new ChoppingBlockJeiCategory(registration.getJeiHelpers().getGuiHelper())
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
                AnvilJeiCategory.TYPE,
                new ArrayList<>(level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ANVIL.get()))
        );

        registration.addRecipes(
                BarrelJeiCategory.TYPE,
                new ArrayList<>(level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.BARREL.get()))
        );

        registration.addRecipes(
                DryingRackJeiCategory.TYPE,
                new ArrayList<>(level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DRYING_RACK.get()))
        );

        registration.addRecipes(
                PitKilnJeiCategory.TYPE,
                new ArrayList<>(level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.PIT_KILN.get()))
        );

        registration.addRecipes(
                ChoppingBlockJeiCategory.TYPE,
                new ArrayList<>(level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CHOPPING_BLOCK.get()))
        );

        ArrayList<PrimitiveCampfireDisplayRecipe> campfireRecipes = new ArrayList<>();
        for (var recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
            if (output.isEmpty() || !output.isEdible()) {
                continue;
            }
            campfireRecipes.add(new PrimitiveCampfireDisplayRecipe(
                    recipe.getIngredients().get(0),
                    output,
                    new ItemStack(ModItems.ASH.get()),
                    recipe.getCookingTime(),
                    200
            ));
        }
        registration.addRecipes(PrimitiveCampfireJeiCategory.TYPE, campfireRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WORKSPACE_TABLE.get()), WorkspaceJeiCategory.TYPE);
        for (var anvil : ModBlocks.ANVIL_BLOCKS) {
            registration.addRecipeCatalyst(new ItemStack(anvil.get()), AnvilJeiCategory.TYPE);
        }
        for (var barrel : ModBlocks.BARREL_BLOCKS) {
            registration.addRecipeCatalyst(new ItemStack(barrel.get()), BarrelJeiCategory.TYPE);
        }
        for (var dryingRack : ModBlocks.DRYING_RACK_BLOCKS) {
            registration.addRecipeCatalyst(new ItemStack(dryingRack.get()), DryingRackJeiCategory.TYPE);
        }
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRIMITIVE_CAMPFIRE.get()), PrimitiveCampfireJeiCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PIT_KILN.get()), PitKilnJeiCategory.TYPE);
        for (var choppingBlock : ModBlocks.CHOPPING_BLOCKS) {
            registration.addRecipeCatalyst(new ItemStack(choppingBlock.get()), ChoppingBlockJeiCategory.TYPE);
        }
    }
}
