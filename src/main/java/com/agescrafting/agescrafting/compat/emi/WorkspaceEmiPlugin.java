package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipe;
import com.agescrafting.agescrafting.compat.campfire.PrimitiveCampfireDisplayRecipe;
import com.agescrafting.agescrafting.dryingrack.DryingRackRecipe;
import com.agescrafting.agescrafting.pitkiln.PitKilnRecipe;
import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import com.agescrafting.agescrafting.workspace.WorkspaceCraftingRecipe;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class WorkspaceEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory WORKSPACE_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "workspace_crafting"),
            EmiStack.of(ModBlocks.WORKSPACE_TABLE.get())
    );

    public static final EmiRecipeCategory BARREL_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "barrel"),
            EmiStack.of(ModBlocks.BARREL.get())
    );

    public static final EmiRecipeCategory DRYING_RACK_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "drying_rack"),
            EmiStack.of(ModBlocks.DRYING_RACK.get())
    );

    public static final EmiRecipeCategory PRIMITIVE_CAMPFIRE_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "primitive_campfire"),
            EmiStack.of(ModBlocks.PRIMITIVE_CAMPFIRE.get())
    );

    public static final EmiRecipeCategory PIT_KILN_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "pit_kiln"),
            EmiStack.of(ModBlocks.PIT_KILN.get())
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(WORKSPACE_CATEGORY);
        registry.addWorkstation(WORKSPACE_CATEGORY, EmiStack.of(ModBlocks.WORKSPACE_TABLE.get()));

        registry.addCategory(BARREL_CATEGORY);
        for (var barrel : ModBlocks.BARREL_BLOCKS) {
            registry.addWorkstation(BARREL_CATEGORY, EmiStack.of(barrel.get()));
        }

        registry.addCategory(DRYING_RACK_CATEGORY);
        for (var dryingRack : ModBlocks.DRYING_RACK_BLOCKS) {
            registry.addWorkstation(DRYING_RACK_CATEGORY, EmiStack.of(dryingRack.get()));
        }

        registry.addCategory(PRIMITIVE_CAMPFIRE_CATEGORY);
        registry.addWorkstation(PRIMITIVE_CAMPFIRE_CATEGORY, EmiStack.of(ModBlocks.PRIMITIVE_CAMPFIRE.get()));

        registry.addCategory(PIT_KILN_CATEGORY);
        registry.addWorkstation(PIT_KILN_CATEGORY, EmiStack.of(ModBlocks.PIT_KILN.get()));

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        for (WorkspaceCraftingRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.WORKSPACE_CRAFTING.get())) {
            registry.addRecipe(new WorkspaceEmiRecipe(recipe, WORKSPACE_CATEGORY));
        }

        for (BarrelRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.BARREL.get())) {
            registry.addRecipe(new BarrelEmiRecipe(recipe, BARREL_CATEGORY));
        }

        for (DryingRackRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DRYING_RACK.get())) {
            registry.addRecipe(new DryingRackEmiRecipe(recipe, DRYING_RACK_CATEGORY));
        }

        for (PitKilnRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.PIT_KILN.get())) {
            registry.addRecipe(new PitKilnEmiRecipe(recipe, PIT_KILN_CATEGORY));
        }

        for (var recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
            if (output.isEmpty()) {
                continue;
            }

            PrimitiveCampfireDisplayRecipe display = new PrimitiveCampfireDisplayRecipe(
                    recipe.getIngredients().get(0),
                    output,
                    new ItemStack(Items.CHARCOAL),
                    recipe.getCookingTime(),
                    200
            );

            registry.addRecipe(new PrimitiveCampfireEmiRecipe(recipe.getId(), display, PRIMITIVE_CAMPFIRE_CATEGORY));
        }
    }
}
