package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.dryingrack.DryingRackRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class DryingRackEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_W = 120;
    private static final int DISPLAY_H = 44;
    private static final int ARROW_X = 47;
    private static final int ARROW_W = 24;
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/drying_rack_recipe.png");

    private final DryingRackRecipe recipe;
    private final EmiRecipeCategory category;

    public DryingRackEmiRecipe(DryingRackRecipe recipe, EmiRecipeCategory category) {
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
        return List.of(EmiStack.of(recipe.result()));
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
        widgets.addTexture(ATLAS, 0, 0, 0, 0, DISPLAY_W, DISPLAY_H, DISPLAY_W, DISPLAY_H, DISPLAY_W, DISPLAY_H);

        widgets.addSlot(EmiIngredient.of(recipe.ingredient()), 12, 12).drawBack(true);
        widgets.addFillingArrow(ARROW_X, 13, Math.max(20, recipe.durationTicks()));
        widgets.addSlot(EmiStack.of(recipe.result()), 88, 12).drawBack(true).recipeContext(this);

        var font = Minecraft.getInstance().font;
        Component time = Component.literal(formatClock(recipe.durationTicks()));
        int timeX = ARROW_X + (ARROW_W - font.width(time)) / 2;
        widgets.addText(time, timeX, 32, 0x5E5E5E, false);
    }

    private static String formatClock(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }
}
