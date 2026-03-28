package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.pitkiln.PitKilnRecipe;
import com.agescrafting.agescrafting.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
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

public class PitKilnJeiCategory implements IRecipeCategory<PitKilnRecipe> {
    private static final int ARROW_X = 47;
    private static final int ARROW_Y = 13;
    private static final int ARROW_W = 24;
    private static final int FAILURE_SLOT_X = 112;
    private static final int FAILURE_SLOT_Y = 12;

    public static final RecipeType<PitKilnRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "pit_kiln",
            PitKilnRecipe.class
    );

    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/pit_kiln_recipe.png");

    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawableAnimated arrow;

    public PitKilnJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.PIT_KILN.get()));
        this.background = guiHelper.drawableBuilder(ATLAS, 0, 0, 142, 52)
                .setTextureSize(142, 52)
                .build();
        this.arrow = guiHelper.createAnimatedRecipeArrow(40);
    }

    @Override
    public @NotNull RecipeType<PitKilnRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.agescrafting.pit_kiln");
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 142;
    }

    @Override
    public int getHeight() {
        return 52;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull PitKilnRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 12)
                .addIngredients(recipe.ingredient());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 12)
                .addItemStack(recipe.result());

        if (!recipe.failureResult().isEmpty() && recipe.failureChance() > 0.0F) {
            int chancePercent = Math.round(recipe.failureChance() * 100.0F);
            IRecipeSlotBuilder failureSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, FAILURE_SLOT_X, FAILURE_SLOT_Y)
                    .addItemStack(recipe.failureResult());
            failureSlot.addRichTooltipCallback((view, tooltip) -> tooltip.add(
                    Component.translatable("tooltip.agescrafting.pit_kiln.failure_result", Component.literal(chancePercent + "%").withStyle(net.minecraft.ChatFormatting.RED)).withStyle(net.minecraft.ChatFormatting.YELLOW)
            ));
        }
    }

    @Override
    public void draw(@NotNull PitKilnRecipe recipe, @NotNull mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
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


