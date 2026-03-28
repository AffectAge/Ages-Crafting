package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.anvil.AnvilRecipe;
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

public class AnvilEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_W = 120;
    private static final int DISPLAY_H = 58;
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/anvil_recipe.png");

    private final AnvilRecipe recipe;
    private final EmiRecipeCategory category;

    public AnvilEmiRecipe(AnvilRecipe recipe, EmiRecipeCategory category) {
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
        return List.of(EmiIngredient.of(recipe.ingredient()), EmiIngredient.of(recipe.tool()));
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

        widgets.addSlot(EmiIngredient.of(recipe.ingredient()), 8, 18).drawBack(true);
        widgets.addSlot(EmiIngredient.of(recipe.tool()), 32, 18).drawBack(true);
        widgets.addSlot(EmiStack.of(recipe.result()), 92, 18).drawBack(true).recipeContext(this);

        var font = Minecraft.getInstance().font;
        Component hits = Component.translatable("gui.agescrafting.anvil.hits", recipe.hits());
        int textX = (DISPLAY_W - font.width(hits)) / 2;
        widgets.addText(hits, textX, 40, 0x5E5E5E, false);
    }
}



