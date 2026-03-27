package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipe;
import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.agescrafting.agescrafting.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class BarrelJeiCategory implements IRecipeCategory<BarrelRecipe> {
    private static final int RECIPE_WIDTH = 176;
    private static final int RECIPE_HEIGHT = 98;

    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/barrel_recipe.png");

    private static final int ITEM_GRID_X = 48;
    private static final int ITEM_GRID_Y = 18;
    private static final int SLOT_STEP = 18;

    private static final int OUTPUT_ITEM_X = 136;
    private static final int OUTPUT_ITEM_Y = 18;

    private static final int INPUT_TANK_X = 28;
    private static final int OUTPUT_TANK_X = 117;
    private static final int TANK_Y = 18;
    private static final int TANK_W = 14;
    private static final int TANK_H = 52;

    public static final RecipeType<BarrelRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "barrel",
            BarrelRecipe.class
    );

    private final IDrawable icon;
    private final IDrawable background;

    public BarrelJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BARREL.get()));
        this.background = guiHelper.drawableBuilder(ATLAS, 0, 0, RECIPE_WIDTH, RECIPE_HEIGHT)
                .setTextureSize(RECIPE_WIDTH, RECIPE_HEIGHT)
                .build();
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
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, ITEM_GRID_X + col * SLOT_STEP, ITEM_GRID_Y + row * SLOT_STEP)
                    .addIngredients(ingredient.ingredient());
            if (ingredient.count() > 1) {
                slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("gui.agescrafting.barrel.required_count", ingredient.count())));
            }
        }

        if (!recipe.fluidIngredients().isEmpty()) {
            FluidStack inFluid = recipe.fluidIngredients().get(0);
            if (!inFluid.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, INPUT_TANK_X, TANK_Y)
                        .addFluidStack(inFluid.getFluid(), inFluid.getAmount())
                        .setFluidRenderer(getDisplayCapacity(inFluid.getAmount()), false, TANK_W, TANK_H);
            }
        }

        List<ItemStack> results = recipe.itemResults();
        int itemOutCount = Math.min(2, results.size());
        for (int i = 0; i < itemOutCount; i++) {
            int y = OUTPUT_ITEM_Y + i * SLOT_STEP + (i == 1 ? 3 : 0);
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_ITEM_X, y)
                    .addItemStack(results.get(i));
        }

        if (!recipe.fluidResults().isEmpty()) {
            FluidStack outFluid = recipe.fluidResults().get(0);
            if (!outFluid.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_TANK_X, TANK_Y)
                        .addFluidStack(outFluid.getFluid(), outFluid.getAmount())
                        .setFluidRenderer(getDisplayCapacity(outFluid.getAmount()), false, TANK_W, TANK_H);
            }
        }
    }

    private static int getDisplayCapacity(int recipeAmount) {
        int configCapacity = Math.max(1000, AgesCraftingConfig.SERVER.barrelTankCapacityMb.get());
        return Math.max(configCapacity, recipeAmount);
    }

    @Override
    public void draw(@NotNull BarrelRecipe recipe, @NotNull mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);

        var font = Minecraft.getInstance().font;
        if (recipe.requiresSealed()) {
            Component sealed = Component.translatable("gui.agescrafting.barrel.sealed");
            int sealedX = ITEM_GRID_X + (54 - font.width(sealed)) / 2;
            int sealedY = ITEM_GRID_Y + 54 + 3;
            guiGraphics.drawString(font, sealed, sealedX, sealedY, 0xC02020, false);
        }

        if (recipe.durationTicks() > 0) {
            guiGraphics.drawString(font,
                    Component.translatable("gui.agescrafting.barrel.recipe_time", String.format(Locale.ROOT, "%.1f", recipe.durationTicks() / 20.0F)),
                    62, 84, 0x5E5E5E, false);
        }
    }
}




