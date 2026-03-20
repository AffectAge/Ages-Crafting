package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BarrelEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_W = 176;
    private static final int DISPLAY_H = 98;

    private final BarrelRecipe recipe;
    private final EmiRecipeCategory category;

    public BarrelEmiRecipe(BarrelRecipe recipe, EmiRecipeCategory category) {
        this.recipe = recipe;
        this.category = category;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> inputs = new ArrayList<>();
        for (BarrelRecipe.IngredientWithCount ingredient : recipe.itemIngredients()) {
            inputs.add(EmiIngredient.of(ingredient.ingredient(), ingredient.count()));
        }

        for (FluidStack fluid : recipe.fluidIngredients()) {
            if (!fluid.isEmpty()) {
                inputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
            }
        }
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> outputs = new ArrayList<>();
        for (ItemStack stack : recipe.itemResults()) {
            outputs.add(EmiStack.of(stack));
        }

        for (FluidStack fluid : recipe.fluidResults()) {
            if (!fluid.isEmpty()) {
                outputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
            }
        }

        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return DISPLAY_W;
    }

    @Override
    public int getDisplayHeight() {
        return DISPLAY_H;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(Component.translatable("gui.agescrafting.barrel.inputs"), 62, 6, 0x9A9A9A, false);
        widgets.addText(Component.translatable("gui.agescrafting.barrel.outputs"), 98, 6, 0x9A9A9A, false);

        List<BarrelRecipe.IngredientWithCount> ingredients = recipe.itemIngredients();
        int displayCount = Math.min(9, ingredients.size());
        for (int i = 0; i < displayCount; i++) {
            int row = i / 3;
            int col = i % 3;
            BarrelRecipe.IngredientWithCount ingredient = ingredients.get(i);
            widgets.addSlot(EmiIngredient.of(ingredient.ingredient(), ingredient.count()), 62 + col * 18, 18 + row * 18).drawBack(true);
        }

        List<FluidStack> inputFluids = recipe.fluidIngredients();
        int[] inputX = {10, 28, 46};
        int inputCount = Math.min(inputX.length, inputFluids.size());
        for (int i = 0; i < inputCount; i++) {
            FluidStack inputFluid = inputFluids.get(i);
            widgets.addTank(EmiStack.of(inputFluid.getFluid(), inputFluid.getAmount()), inputX[i], 18, 14, 54, Math.max(1000, inputFluid.getAmount()));
        }

        List<ItemStack> outputs = recipe.itemResults();
        for (int i = 0; i < outputs.size(); i++) {
            int row = i / 3;
            int col = i % 3;
            widgets.addSlot(EmiStack.of(outputs.get(i)), 98 + col * 18, 18 + row * 18).drawBack(true).recipeContext(this);
        }

        List<FluidStack> outputFluids = recipe.fluidResults();
        int[] outputX = {120, 138, 156};
        int outputCount = Math.min(outputX.length, outputFluids.size());
        for (int i = 0; i < outputCount; i++) {
            FluidStack outputFluid = outputFluids.get(i);
            widgets.addTank(EmiStack.of(outputFluid.getFluid(), outputFluid.getAmount()), outputX[i], 18, 14, 54, Math.max(1000, outputFluid.getAmount()));
        }

        if (recipe.requiresSealed()) {
            widgets.addText(Component.translatable("gui.agescrafting.barrel.recipe_requires_sealed"), 62, 74, 0xC96A6A, false);
        } else {
            widgets.addText(Component.translatable("gui.agescrafting.barrel.recipe_not_sealed"), 62, 74, 0x7FAF7F, false);
        }

        if (recipe.durationTicks() > 0) {
            widgets.addText(Component.translatable("gui.agescrafting.barrel.recipe_time", String.format(Locale.ROOT, "%.1f", recipe.durationTicks() / 20.0F)), 62, 84, 0x9A9A9A, false);
        }
    }
}
