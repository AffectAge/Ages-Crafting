package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.compat.campfire.PrimitiveCampfireDisplayRecipe;
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

public class PrimitiveCampfireJeiCategory implements IRecipeCategory<PrimitiveCampfireDisplayRecipe> {
    private static final int FIRST_ARROW_X = 30;
    private static final int SECOND_ARROW_X = 80;
    private static final int ARROW_Y = 13;
    private static final int ARROW_W = 24;

    public static final RecipeType<PrimitiveCampfireDisplayRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "primitive_campfire",
            PrimitiveCampfireDisplayRecipe.class
    );

    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/primitive_campfire_recipe.png");

    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawableAnimated arrow;

    public PrimitiveCampfireJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.PRIMITIVE_CAMPFIRE.get()));
        this.background = guiHelper.drawableBuilder(ATLAS, 0, 0, 140, 44)
                .setTextureSize(140, 44)
                .build();
        this.arrow = guiHelper.createAnimatedRecipeArrow(40);
    }

    @Override
    public @NotNull RecipeType<PrimitiveCampfireDisplayRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.agescrafting.primitive_campfire");
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 140;
    }

    @Override
    public int getHeight() {
        return 44;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull PrimitiveCampfireDisplayRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 12)
                .addIngredients(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 58, 12)
                .addItemStack(recipe.cookedOutput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 12)
                .addItemStack(recipe.overcookedOutput());
    }

    @Override
    public void draw(@NotNull PrimitiveCampfireDisplayRecipe recipe, @NotNull mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
        var font = Minecraft.getInstance().font;
        arrow.draw(guiGraphics, FIRST_ARROW_X, ARROW_Y);
        arrow.draw(guiGraphics, SECOND_ARROW_X, ARROW_Y);

        Component cookTime = Component.literal(formatClock(recipe.cookTimeTicks()));
        int cookTimeX = FIRST_ARROW_X + (ARROW_W - font.width(cookTime)) / 2;
        guiGraphics.drawString(font, cookTime, cookTimeX, 32, 0x5E5E5E, false);

        Component overcookTime = Component.literal(formatClock(recipe.overcookTimeTicks()));
        int overcookTimeX = SECOND_ARROW_X + (ARROW_W - font.width(overcookTime)) / 2;
        guiGraphics.drawString(font, overcookTime, overcookTimeX, 32, 0x5E5E5E, false);
    }

    private static String formatClock(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }
}
