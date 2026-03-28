package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.compat.campfire.PrimitiveCampfireDisplayRecipe;
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

public class PrimitiveCampfireEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_W = 140;
    private static final int DISPLAY_H = 44;
    private static final int FIRST_ARROW_X = 30;
    private static final int SECOND_ARROW_X = 80;
    private static final int ARROW_W = 24;
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/primitive_campfire_recipe.png");

    private final ResourceLocation id;
    private final PrimitiveCampfireDisplayRecipe recipe;
    private final EmiRecipeCategory category;

    public PrimitiveCampfireEmiRecipe(ResourceLocation id, PrimitiveCampfireDisplayRecipe recipe, EmiRecipeCategory category) {
        this.id = id;
        this.recipe = recipe;
        this.category = category;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiIngredient.of(recipe.input()));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(recipe.cookedOutput()), EmiStack.of(recipe.overcookedOutput()));
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

        widgets.addSlot(EmiIngredient.of(recipe.input()), 8, 12).drawBack(true);
        widgets.addFillingArrow(FIRST_ARROW_X, 13, Math.max(20, recipe.cookTimeTicks()));
        widgets.addSlot(EmiStack.of(recipe.cookedOutput()), 58, 12).drawBack(true).recipeContext(this);

        widgets.addFillingArrow(SECOND_ARROW_X, 13, Math.max(20, recipe.overcookTimeTicks()));
        widgets.addSlot(EmiStack.of(recipe.overcookedOutput()), 108, 12).drawBack(true).recipeContext(this);

        var font = Minecraft.getInstance().font;
        Component cookTime = Component.literal(formatClock(recipe.cookTimeTicks()));
        int cookTimeX = FIRST_ARROW_X + (ARROW_W - font.width(cookTime)) / 2;
        widgets.addText(cookTime, cookTimeX, 32, 0x5E5E5E, false);

        Component overcookTime = Component.literal(formatClock(recipe.overcookTimeTicks()));
        int overcookTimeX = SECOND_ARROW_X + (ARROW_W - font.width(overcookTime)) / 2;
        widgets.addText(overcookTime, overcookTimeX, 32, 0x5E5E5E, false);
    }

    private static String formatClock(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }
}
