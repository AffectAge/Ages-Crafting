package com.agescrafting.agescrafting.compat.emi;

import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipe;
import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BarrelEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_W = 176;
    private static final int DISPLAY_H = 98;
    private static final int X_OFFSET = 9;

    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/barrel_recipe.png");
    private static final int ATLAS_WIDTH = 176;
    private static final int ATLAS_HEIGHT = 160;

    private static final int ITEM_GRID_X = 48 + X_OFFSET;
    private static final int ITEM_GRID_Y = 18;
    private static final int SLOT_STEP = 18;

    private static final int OUTPUT_ITEM_X = 136 + X_OFFSET;
    private static final int OUTPUT_ITEM_Y = 18;

    private static final int INPUT_TANK_X = 28 + X_OFFSET;
    private static final int OUTPUT_TANK_X = 117 + X_OFFSET;
    private static final int TANK_Y = 18;
    private static final int TANK_W = 14;
    private static final int TANK_H = 52;

    private static final int PROGRESS_X = 115;
    private static final int PROGRESS_Y = 17;
    private static final int PROGRESS_W = 5;
    private static final int PROGRESS_H = 54;
    private static final int PROGRESS_TRACK_U = 0;
    private static final int PROGRESS_TRACK_V = 99;
    private static final int PROGRESS_FILL_U = 8;
    private static final int PROGRESS_FILL_V = 99;

    private final BarrelRecipe recipe;
    private final EmiRecipeCategory category;

    public BarrelEmiRecipe(BarrelRecipe recipe, EmiRecipeCategory category) {
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
        for (BarrelRecipe.IngredientWithCount ingredient : recipe.itemIngredients()) {
            inputs.add(EmiIngredient.of(ingredient.ingredient(), ingredient.count()));
        }

        if (!recipe.fluidIngredients().isEmpty()) {
            FluidStack fluid = recipe.fluidIngredients().get(0);
            if (!fluid.isEmpty()) {
                inputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
            }
        }
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> outputs = new ArrayList<>();
        int itemOutCount = Math.min(2, recipe.itemResults().size());
        for (int i = 0; i < itemOutCount; i++) {
            outputs.add(EmiStack.of(recipe.itemResults().get(i)));
        }

        if (!recipe.fluidResults().isEmpty()) {
            FluidStack fluid = recipe.fluidResults().get(0);
            if (!fluid.isEmpty()) {
                outputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
            }
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

    private static int getDisplayCapacity(int recipeAmount) {
        int configCapacity = Math.max(1000, AgesCraftingConfig.SERVER.barrelTankCapacityMb.get());
        return Math.max(configCapacity, recipeAmount);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(ATLAS, 0, 0, 0, 0, DISPLAY_W, DISPLAY_H, DISPLAY_W, DISPLAY_H, ATLAS_WIDTH, ATLAS_HEIGHT);

        List<BarrelRecipe.IngredientWithCount> ingredients = recipe.itemIngredients();
        int displayCount = Math.min(9, ingredients.size());
        for (int i = 0; i < displayCount; i++) {
            int row = i / 3;
            int col = i % 3;
            BarrelRecipe.IngredientWithCount ingredient = ingredients.get(i);
            widgets.addSlot(EmiIngredient.of(ingredient.ingredient(), ingredient.count()), ITEM_GRID_X + col * SLOT_STEP, ITEM_GRID_Y + row * SLOT_STEP)
                    .drawBack(true);
        }

        if (!recipe.fluidIngredients().isEmpty()) {
            FluidStack inputFluid = recipe.fluidIngredients().get(0);
            if (!inputFluid.isEmpty()) {
                widgets.addTank(EmiStack.of(inputFluid.getFluid(), inputFluid.getAmount()), INPUT_TANK_X, TANK_Y, TANK_W, TANK_H, getDisplayCapacity(inputFluid.getAmount()));
            }
        }

        List<ItemStack> outputs = recipe.itemResults();
        int itemOutCount = Math.min(2, outputs.size());
        for (int i = 0; i < itemOutCount; i++) {
            int y = OUTPUT_ITEM_Y + i * SLOT_STEP + (i == 1 ? 3 : 0);
            widgets.addSlot(EmiStack.of(outputs.get(i)), OUTPUT_ITEM_X, y)
                    .drawBack(true)
                    .recipeContext(this);
        }

        if (!recipe.fluidResults().isEmpty()) {
            FluidStack outputFluid = recipe.fluidResults().get(0);
            if (!outputFluid.isEmpty()) {
                widgets.addTank(EmiStack.of(outputFluid.getFluid(), outputFluid.getAmount()), OUTPUT_TANK_X, TANK_Y, TANK_W, TANK_H, getDisplayCapacity(outputFluid.getAmount()));
            }
        }

        widgets.addDrawable(PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, (guiGraphics, mouseX, mouseY, delta) -> {
            guiGraphics.blit(ATLAS, PROGRESS_X, PROGRESS_Y, PROGRESS_TRACK_U, PROGRESS_TRACK_V, PROGRESS_W, PROGRESS_H, ATLAS_WIDTH, ATLAS_HEIGHT);
            int animatedHeight = getAnimatedProgressHeight();
            if (animatedHeight > 0) {
                int dstY = PROGRESS_Y + (PROGRESS_H - animatedHeight);
                int srcV = PROGRESS_FILL_V + (PROGRESS_H - animatedHeight);
                guiGraphics.blit(ATLAS, PROGRESS_X, dstY, PROGRESS_FILL_U, srcV, PROGRESS_W, animatedHeight, ATLAS_WIDTH, ATLAS_HEIGHT);
            }
        });

        if (recipe.requiresSealed()) {
            var font = Minecraft.getInstance().font;
            Component sealed = Component.translatable("gui.agescrafting.barrel.sealed");
            int sealedX = ITEM_GRID_X + (54 - font.width(sealed)) / 2;
            int sealedY = ITEM_GRID_Y + 54 + 3;
            widgets.addText(sealed, sealedX, sealedY, 0xC02020, false);
        }

        if (recipe.durationTicks() > 0) {
            var font = Minecraft.getInstance().font;
            Component time = Component.literal(formatClock(recipe.durationTicks()));
            int timeX = ITEM_GRID_X + (54 - font.width(time)) / 2;
            widgets.addText(time, timeX, 84, 0x5E5E5E, false);
        }
    }

    private static int getAnimatedProgressHeight() {
        int cycleTicks = 40;
        long cycleMs = cycleTicks * 50L;
        long elapsed = System.currentTimeMillis() % cycleMs;
        return (int) Math.round((elapsed / (double) cycleMs) * PROGRESS_H);
    }

    private static String formatClock(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }
}



