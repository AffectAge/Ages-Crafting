package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.workspace.WorkspaceCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkspaceEmiRecipe implements EmiRecipe {
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/workspace_recipe.png");
    private static final int DISPLAY_W = 190;
    private static final int DISPLAY_H = 122;

    private static final int LAYER0_X = 8;
    private static final int LAYER1_X = 68;
    private static final int LAYER2_X = 128;
    private static final int LAYER_Y = 14;
    private static final int SLOT_STEP = 18;
    private static final int LAYER_SIZE = SLOT_STEP * 3;

    private final WorkspaceCraftingRecipe recipe;
    private final EmiRecipeCategory category;

    public WorkspaceEmiRecipe(WorkspaceCraftingRecipe recipe, EmiRecipeCategory category) {
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
        for (int i = 0; i < 27; i++) {
            Optional<Ingredient> ingredient = recipe.getIngredientAt(i);
            ingredient.ifPresent(value -> inputs.add(EmiIngredient.of(value)));
        }
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess()
                : net.minecraft.core.RegistryAccess.EMPTY)));
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

        addCenteredText(widgets, "gui.agescrafting.layer.bottom", LAYER0_X + LAYER_SIZE / 2, 3);
        addCenteredText(widgets, "gui.agescrafting.layer.middle", LAYER1_X + LAYER_SIZE / 2, 3);
        addCenteredText(widgets, "gui.agescrafting.layer.top", LAYER2_X + LAYER_SIZE / 2, 3);
        addLayer(widgets, 0, LAYER0_X, LAYER_Y);
        addLayer(widgets, 1, LAYER1_X, LAYER_Y);
        addLayer(widgets, 2, LAYER2_X, LAYER_Y);

        widgets.addSlot(EmiStack.of(Items.FLINT), 56, 95).drawBack(true);

        widgets.addSlot(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess()
                : net.minecraft.core.RegistryAccess.EMPTY)), 116, 95).drawBack(true).recipeContext(this);

        recipe.getRequiredStage().ifPresent(stage ->
                addCenteredText(widgets, Component.translatable("gui.agescrafting.stage_required", stage), 95, 114));
    }

    private void addLayer(WidgetHolder widgets, int layer, int startX, int startY) {
        for (int z = 0; z < 3; z++) {
            for (int x = 0; x < 3; x++) {
                int index = x + z * 3 + layer * 9;
                Optional<Ingredient> ingredient = recipe.getIngredientAt(index);

                if (ingredient.isPresent()) {
                    widgets.addSlot(EmiIngredient.of(ingredient.get()), startX + x * 18, startY + z * 18).drawBack(true);
                } else {
                    widgets.addSlot(EmiStack.EMPTY, startX + x * SLOT_STEP, startY + z * SLOT_STEP).drawBack(true);
                }
            }
        }
    }

    private void addCenteredText(WidgetHolder widgets, String key, int centerX, int y) {
        addCenteredText(widgets, Component.translatable(key), centerX, y);
    }

    private void addCenteredText(WidgetHolder widgets, Component translated, int centerX, int y) {
        var font = Minecraft.getInstance().font;
        int x = centerX - font.width(translated) / 2;
        widgets.addText(translated, x, y, 0x9A9A9A, false);
    }
}



