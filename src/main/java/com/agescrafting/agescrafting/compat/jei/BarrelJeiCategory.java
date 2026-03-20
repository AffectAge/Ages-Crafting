package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipe;
import com.agescrafting.agescrafting.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class BarrelJeiCategory implements IRecipeCategory<BarrelRecipe> {
    private static final int RECIPE_WIDTH = 176;
    private static final int RECIPE_HEIGHT = 98;

    private static final int TANK_Y = 18;
    private static final int TANK_W = 14;
    private static final int TANK_H = 54;
    private static final int[] INPUT_TANK_X = {10, 28, 46};
    private static final int[] OUTPUT_TANK_X = {120, 138, 156};

    public static final RecipeType<BarrelRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "barrel",
            BarrelRecipe.class
    );

    private final IDrawable icon;

    public BarrelJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BARREL.get()));
    }

    @Override
    public @NotNull RecipeType<BarrelRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.agescrafting.barrel");
    }

    @Override
    public int getWidth() {
        return RECIPE_WIDTH;
    }

    @Override
    public int getHeight() {
        return RECIPE_HEIGHT;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BarrelRecipe recipe, @NotNull IFocusGroup focuses) {
        List<BarrelRecipe.IngredientWithCount> ingredients = recipe.itemIngredients();
        int displayCount = Math.min(9, ingredients.size());

        for (int i = 0; i < displayCount; i++) {
            BarrelRecipe.IngredientWithCount ingredient = ingredients.get(i);
            int row = i / 3;
            int col = i % 3;
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, 62 + col * 18, 18 + row * 18)
                    .addIngredients(ingredient.ingredient());
            if (ingredient.count() > 1) {
                slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("gui.agescrafting.barrel.required_count", ingredient.count())));
            }
        }

        List<FluidStack> inputFluids = recipe.fluidIngredients();
        int inputCount = Math.min(INPUT_TANK_X.length, inputFluids.size());
        for (int i = 0; i < inputCount; i++) {
            FluidStack inFluid = inputFluids.get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, INPUT_TANK_X[i], TANK_Y)
                    .addFluidStack(inFluid.getFluid(), inFluid.getAmount())
                    .setFluidRenderer(Math.max(1000, inFluid.getAmount()), false, TANK_W, TANK_H);
        }

        List<ItemStack> results = recipe.itemResults();
        for (int i = 0; i < results.size(); i++) {
            int row = i / 3;
            int col = i % 3;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 98 + col * 18, 18 + row * 18)
                    .addItemStack(results.get(i));
        }

        List<FluidStack> outputFluids = recipe.fluidResults();
        int outputCount = Math.min(OUTPUT_TANK_X.length, outputFluids.size());
        for (int i = 0; i < outputCount; i++) {
            FluidStack outFluid = outputFluids.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_TANK_X[i], TANK_Y)
                    .addFluidStack(outFluid.getFluid(), outFluid.getAmount())
                    .setFluidRenderer(Math.max(1000, outFluid.getAmount()), false, TANK_W, TANK_H);
        }
    }

    @Override
    public void draw(@NotNull BarrelRecipe recipe, @NotNull mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("gui.agescrafting.barrel.inputs"), 62, 6, 0x9A9A9A, false);
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("gui.agescrafting.barrel.outputs"), 98, 6, 0x9A9A9A, false);

        if (recipe.requiresSealed()) {
            guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                    Component.translatable("gui.agescrafting.barrel.recipe_requires_sealed"), 62, 74, 0xC96A6A, false);
        } else {
            guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                    Component.translatable("gui.agescrafting.barrel.recipe_not_sealed"), 62, 74, 0x7FAF7F, false);
        }

        if (recipe.durationTicks() > 0) {
            guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                    Component.translatable("gui.agescrafting.barrel.recipe_time", String.format(Locale.ROOT, "%.1f", recipe.durationTicks() / 20.0F)), 62, 84, 0x9A9A9A, false);
        }
    }
}
