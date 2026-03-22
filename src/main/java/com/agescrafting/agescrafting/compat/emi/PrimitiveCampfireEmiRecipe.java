package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.compat.campfire.PrimitiveCampfireDisplayRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class PrimitiveCampfireEmiRecipe implements EmiRecipe {
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
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 44;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(recipe.input()), 8, 12).drawBack(true);
        widgets.addText(Component.literal("->"), 40, 16, 0x808080, false);
        widgets.addSlot(EmiStack.of(recipe.cookedOutput()), 58, 12).drawBack(true).recipeContext(this);
        widgets.addText(Component.literal("->"), 90, 16, 0x808080, false);
        widgets.addSlot(EmiStack.of(recipe.overcookedOutput()), 108, 12).drawBack(true).recipeContext(this);

        widgets.addText(
                Component.translatable("gui.agescrafting.primitive_campfire.cook_time", String.format(Locale.ROOT, "%.1f", recipe.cookTimeTicks() / 20.0F)),
                8,
                32,
                0x404040,
                false
        );
        widgets.addText(
                Component.translatable("gui.agescrafting.primitive_campfire.overcook_time", String.format(Locale.ROOT, "%.1f", recipe.overcookTimeTicks() / 20.0F)),
                74,
                32,
                0x404040,
                false
        );
    }
}
