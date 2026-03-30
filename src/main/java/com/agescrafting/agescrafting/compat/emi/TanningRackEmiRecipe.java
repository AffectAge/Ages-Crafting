package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.tanningrack.TanningRackRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TanningRackEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_W = 120;
    private static final int DISPLAY_H = 58;
    private static final int INPUT_X = 6;
    private static final int TOOL_X = 28;
    private static final int RESULT_X = 76;
    private static final int FAILURE_SLOT_X = 98;
    private static final int SLOT_Y = 12;
    private static final int ARROW_X = 49;
    private static final int ARROW_Y = 13;
    private static final int SLOT_SIZE = 18;
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/tanning_rack_recipe.png");

    private final TanningRackRecipe recipe;
    private final EmiRecipeCategory category;

    public TanningRackEmiRecipe(TanningRackRecipe recipe, EmiRecipeCategory category) {
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
        List<EmiStack> outputs = new ArrayList<>();
        outputs.add(EmiStack.of(recipe.result()));
        if (recipe.hasFailureOutput()) {
            outputs.add(EmiStack.of(recipe.failureOutput()));
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
        widgets.addTexture(ATLAS, 0, 0, 0, 0, DISPLAY_W, DISPLAY_H, DISPLAY_W, DISPLAY_H, DISPLAY_W, DISPLAY_H);

        widgets.addSlot(EmiIngredient.of(recipe.ingredient()), INPUT_X, SLOT_Y).drawBack(true);
        widgets.addSlot(EmiIngredient.of(recipe.tool()), TOOL_X, SLOT_Y).drawBack(true);
        widgets.addFillingArrow(ARROW_X, ARROW_Y, 40);
        widgets.addSlot(EmiStack.of(recipe.result()), RESULT_X, SLOT_Y).drawBack(true).recipeContext(this);

        if (recipe.hasFailureOutput()) {
            widgets.addSlot(EmiStack.of(recipe.failureOutput()), FAILURE_SLOT_X, SLOT_Y).drawBack(true).recipeContext(this);
            int chancePercent = Math.round(recipe.failureChance() * 100.0F);
            Component tooltipLine = Component.translatable(
                    "tooltip.agescrafting.tanning_rack.failure_result",
                    Component.literal(chancePercent + "%").withStyle(ChatFormatting.RED)
            ).withStyle(ChatFormatting.YELLOW);
            widgets.addDrawable(0, 0, DISPLAY_W, DISPLAY_H, (guiGraphics, mouseX, mouseY, delta) -> {
                if (mouseX >= FAILURE_SLOT_X && mouseX < FAILURE_SLOT_X + SLOT_SIZE
                        && mouseY >= SLOT_Y && mouseY < SLOT_Y + SLOT_SIZE) {
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, List.of(tooltipLine), Optional.empty(), mouseX, mouseY);
                }
            });
        }

        var font = Minecraft.getInstance().font;
        Component hits = Component.translatable("gui.agescrafting.anvil.hits", recipe.hits());
        int textX = (DISPLAY_W - font.width(hits)) / 2;
        widgets.addText(hits, textX, 40, 0x5E5E5E, false);
    }
}

