package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.dryingrack.DryingRackRecipe;
import com.agescrafting.agescrafting.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DryingRackJeiCategory implements IRecipeCategory<DryingRackRecipe> {
    public static final RecipeType<DryingRackRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "drying_rack",
            DryingRackRecipe.class
    );

    private final IDrawable icon;
    private final IDrawable background;

    public DryingRackJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DRYING_RACK.get()));
        this.background = guiHelper.createBlankDrawable(120, 44);
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
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, "->", 56, 16, 0x808080, false);
        guiGraphics.drawString(font,
                Component.translatable("gui.agescrafting.barrel.recipe_time", String.format(Locale.ROOT, "%.1f", recipe.durationTicks() / 20.0F)),
                12,
                32,
                0x404040,
                false);
    }
}
