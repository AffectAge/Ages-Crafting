package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.choppingblock.ChoppingBlockRecipe;
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

public class ChoppingBlockJeiCategory implements IRecipeCategory<ChoppingBlockRecipe> {
    public static final RecipeType<ChoppingBlockRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "chopping_block",
            ChoppingBlockRecipe.class
    );

    private final IDrawable icon;
    private final IDrawable background;

    public ChoppingBlockJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CHOPPING_BLOCK.get()));
        this.background = guiHelper.createBlankDrawable(120, 58);
    }

    @Override
    public @NotNull RecipeType<ChoppingBlockRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.agescrafting.chopping_block");
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
        return 58;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ChoppingBlockRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 18)
                .addIngredients(recipe.ingredient());

        builder.addSlot(RecipeIngredientRole.CATALYST, 44, 18)
                .addIngredients(recipe.tool());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 18)
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(@NotNull ChoppingBlockRecipe recipe, @NotNull mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, "->", 70, 22, 0x808080, false);
        guiGraphics.drawString(font,
                Component.translatable("gui.agescrafting.chopping_block.chops", recipe.chopsRequired()),
                8,
                42,
                0x404040,
                false);
    }
}
