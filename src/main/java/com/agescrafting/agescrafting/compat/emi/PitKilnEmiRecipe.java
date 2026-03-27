package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.pitkiln.PitKilnRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PitKilnEmiRecipe implements EmiRecipe {
    private final PitKilnRecipe recipe;
    private final EmiRecipeCategory category;

    public PitKilnEmiRecipe(PitKilnRecipe recipe, EmiRecipeCategory category) {
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
        return List.of(EmiIngredient.of(recipe.ingredient()));
    }

    @Override
    public List<EmiStack> getOutputs() {
        ArrayList<EmiStack> result = new ArrayList<>();
        result.add(EmiStack.of(recipe.result()));
        if (!recipe.failureResult().isEmpty() && recipe.failureChance() > 0.0F) {
            result.add(EmiStack.of(recipe.failureResult()));
        }
        return result;
    }

    @Override
    public int getDisplayWidth() {
        return 142;
    }

    @Override
    public int getDisplayHeight() {
        return 52;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(recipe.ingredient()), 12, 12).drawBack(true);
        widgets.addFillingArrow(47, 13, Math.max(20, recipe.durationTicks()));
        widgets.addSlot(EmiStack.of(recipe.result()), 88, 12).drawBack(true).recipeContext(this);

        if (!recipe.failureResult().isEmpty() && recipe.failureChance() > 0.0F) {
            widgets.addText(Component.literal("?"), 103, 16, 0x8A4B2A, false);
            widgets.addSlot(EmiStack.of(recipe.failureResult()), 112, 12).drawBack(true).recipeContext(this);
            widgets.addText(Component.literal(String.format(Locale.ROOT, "Fail %.0f%%", recipe.failureChance() * 100.0F)), 12, 32, 0x8A4B2A, false);
        }

        widgets.addText(
                Component.literal(formatClock(recipe.durationTicks())),
                12,
                42,
                0x5E5E5E,
                false
        );
    }
    private static String formatClock(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }
}
