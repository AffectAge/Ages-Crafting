package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.compat.campfire.PrimitiveCampfireDisplayRecipe;
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

public class PrimitiveCampfireJeiCategory implements IRecipeCategory<PrimitiveCampfireDisplayRecipe> {
    public static final RecipeType<PrimitiveCampfireDisplayRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "primitive_campfire",
            PrimitiveCampfireDisplayRecipe.class
    );

    private final IDrawable icon;
    private final IDrawable background;

    public PrimitiveCampfireJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.PRIMITIVE_CAMPFIRE.get()));
        this.background = guiHelper.createBlankDrawable(140, 44);
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
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, "->", 40, 16, 0x7A7A7A, false);
        guiGraphics.drawString(font, "->", 90, 16, 0x7A7A7A, false);
        guiGraphics.drawString(font,
                Component.translatable("gui.agescrafting.primitive_campfire.cook_time", String.format(Locale.ROOT, "%.1f", recipe.cookTimeTicks() / 20.0F)),
                8,
                32,
                0x5E5E5E,
                false);
        guiGraphics.drawString(font,
                Component.translatable("gui.agescrafting.primitive_campfire.overcook_time", String.format(Locale.ROOT, "%.1f", recipe.overcookTimeTicks() / 20.0F)),
                74,
                32,
                0x5E5E5E,
                false);
    }
}

