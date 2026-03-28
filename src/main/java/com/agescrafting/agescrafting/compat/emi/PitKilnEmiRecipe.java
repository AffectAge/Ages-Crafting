package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.pitkiln.PitKilnRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PitKilnEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_W = 142;
    private static final int DISPLAY_H = 52;
    private static final int ARROW_X = 47;
    private static final int ARROW_W = 24;
    private static final int FAILURE_SLOT_X = 112;
    private static final int FAILURE_SLOT_Y = 12;
    private static final int SLOT_SIZE = 18;
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/pit_kiln_recipe.png");

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

        if (!recipe.failureResult().isEmpty() && recipe.failureChance() > 0.0F) {
            widgets.addSlot(EmiStack.of(recipe.failureResult()), FAILURE_SLOT_X, FAILURE_SLOT_Y).drawBack(true).recipeContext(this);
            int chancePercent = Math.round(recipe.failureChance() * 100.0F);
            Component tooltipLine = Component.translatable("tooltip.agescrafting.pit_kiln.failure_result", Component.literal(chancePercent + "%").withStyle(net.minecraft.ChatFormatting.RED)).withStyle(net.minecraft.ChatFormatting.YELLOW);
            widgets.addDrawable(0, 0, DISPLAY_W, DISPLAY_H, (guiGraphics, mouseX, mouseY, delta) -> {
                if (mouseX >= FAILURE_SLOT_X && mouseX < FAILURE_SLOT_X + SLOT_SIZE
                        && mouseY >= FAILURE_SLOT_Y && mouseY < FAILURE_SLOT_Y + SLOT_SIZE) {
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, List.of(tooltipLine), Optional.empty(), mouseX, mouseY);
                }
            });
        }

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


