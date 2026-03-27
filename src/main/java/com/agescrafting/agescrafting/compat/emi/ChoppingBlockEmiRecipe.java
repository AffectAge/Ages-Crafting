package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.choppingblock.ChoppingBlockRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChoppingBlockEmiRecipe implements EmiRecipe {
    private final ChoppingBlockRecipe recipe;
    private final EmiRecipeCategory category;

    public ChoppingBlockEmiRecipe(ChoppingBlockRecipe recipe, EmiRecipeCategory category) {
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
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 58;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(recipe.ingredient()), 8, 18).drawBack(true);
        widgets.addSlot(EmiIngredient.of(recipe.tool()), 44, 18).drawBack(true);
        widgets.addText(Component.literal("->"), 70, 22, 0x7A7A7A, false);
        widgets.addSlot(EmiStack.of(recipe.result()), 92, 18).drawBack(true).recipeContext(this);

        widgets.addText(
                Component.translatable("gui.agescrafting.chopping_block.chops", recipe.chopsRequired()),
                8,
                42,
                0x5E5E5E,
                false
        );
    }
}

