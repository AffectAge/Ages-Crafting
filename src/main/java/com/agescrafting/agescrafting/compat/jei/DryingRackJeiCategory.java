package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.dryingrack.DryingRackRecipe;
import com.agescrafting.agescrafting.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DryingRackJeiCategory implements IRecipeCategory<DryingRackRecipe> {
    private static final int ARROW_X = 47;
    private static final int ARROW_Y = 13;
    private static final int ARROW_W = 24;

    public static final RecipeType<DryingRackRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "drying_rack",
            DryingRackRecipe.class
    );

    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/drying_rack_recipe.png");

    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawableAnimated arrow;

    public DryingRackJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DRYING_RACK.get()));
        this.background = guiHelper.drawableBuilder(ATLAS, 0, 0, 120, 44)
                .setTextureSize(120, 44)
                .build();
        this.arrow = guiHelper.createAnimatedRecipeArrow(40);
    }

    @Override
    public @NotNull RecipeType<DryingRackRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.agescrafting.drying_rack");
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 44;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull DryingRackRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 12)
                .addIngredients(recipe.ingredient());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 12)
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(@NotNull DryingRackRecipe recipe, @NotNull mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
        var font = Minecraft.getInstance().font;
        arrow.draw(guiGraphics, ARROW_X, ARROW_Y);

        Component time = Component.literal(formatClock(recipe.durationTicks()));
        int timeX = ARROW_X + (ARROW_W - font.width(time)) / 2;
        guiGraphics.drawString(font, time, timeX, 32, 0x5E5E5E, false);
    }

    private static String formatClock(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }
}
