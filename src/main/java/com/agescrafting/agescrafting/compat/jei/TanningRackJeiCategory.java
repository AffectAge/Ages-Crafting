package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.tanningrack.TanningRackRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TanningRackJeiCategory implements IRecipeCategory<TanningRackRecipe> {
    private static final int DISPLAY_W = 120;
    private static final int DISPLAY_H = 58;
    private static final int INPUT_X = 6;
    private static final int TOOL_X = 28;
    private static final int RESULT_X = 76;
    private static final int FAILURE_SLOT_X = 98;
    private static final int SLOT_Y = 12;
    private static final int ARROW_X = 49;
    private static final int ARROW_Y = 13;

    public static final RecipeType<TanningRackRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "tanning_rack",
            TanningRackRecipe.class
    );

    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/tanning_rack_recipe.png");

    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawableAnimated arrow;

    public TanningRackJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.TANNING_RACK.get()));
        this.background = guiHelper.drawableBuilder(ATLAS, 0, 0, DISPLAY_W, DISPLAY_H)
                .setTextureSize(DISPLAY_W, DISPLAY_H)
                .build();
        this.arrow = guiHelper.createAnimatedRecipeArrow(40);
    }

    @Override
    public @NotNull RecipeType<TanningRackRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.agescrafting.tanning_rack");
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return DISPLAY_W;
    }

    @Override
    public int getHeight() {
        return DISPLAY_H;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull TanningRackRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y).addIngredients(recipe.ingredient());
        builder.addSlot(RecipeIngredientRole.CATALYST, TOOL_X, SLOT_Y).addIngredients(recipe.tool());
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, SLOT_Y).addItemStack(recipe.result());

        if (recipe.hasFailureOutput()) {
            int chancePercent = Math.round(recipe.failureChance() * 100.0F);
            IRecipeSlotBuilder failureSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, FAILURE_SLOT_X, SLOT_Y)
                    .addItemStack(recipe.failureOutput());
            failureSlot.addRichTooltipCallback((view, tooltip) -> tooltip.add(
                    Component.translatable(
                            "tooltip.agescrafting.tanning_rack.failure_result",
                            Component.literal(chancePercent + "%").withStyle(ChatFormatting.RED)
                    ).withStyle(ChatFormatting.YELLOW)
            ));
        }
    }

    @Override
    public void draw(@NotNull TanningRackRecipe recipe, @NotNull mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
        var font = Minecraft.getInstance().font;
        arrow.draw(guiGraphics, ARROW_X, ARROW_Y);

        Component hits = Component.translatable("gui.agescrafting.anvil.hits", recipe.hits());
        int textX = (DISPLAY_W - font.width(hits)) / 2;
        guiGraphics.drawString(font, hits, textX, 40, 0x5E5E5E, false);
    }
}

