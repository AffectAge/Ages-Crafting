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
        widgets.addFillingArrow(30, 13, Math.max(20, recipe.cookTimeTicks()));
        widgets.addSlot(EmiStack.of(recipe.cookedOutput()), 58, 12).drawBack(true).recipeContext(this);

        widgets.addFillingArrow(80, 13, Math.max(20, recipe.overcookTimeTicks()));
        widgets.addDrawable(80, 13, 24, 17, (guiGraphics, mouseX, mouseY, delta) -> guiGraphics.fill(80, 13, 104, 30, 0x35B34735));
        widgets.addSlot(EmiStack.of(recipe.overcookedOutput()), 108, 12).drawBack(true).recipeContext(this);

        widgets.addText(
                Component.literal(formatClock(recipe.cookTimeTicks())),
                8,
                32,
                0x5E5E5E,
                false
        );
        widgets.addText(
                Component.literal(formatClock(recipe.overcookTimeTicks())),
                74,
                32,
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
