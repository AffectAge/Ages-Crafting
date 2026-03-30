package com.agescrafting.agescrafting.compat.jei;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.workspace.WorkspaceCraftingRecipe;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorkspaceJeiCategory implements IRecipeCategory<WorkspaceCraftingRecipe> {
    private static final int RECIPE_WIDTH = 190;
    private static final int RECIPE_HEIGHT = 122;
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/workspace_recipe.png");

    private static final int LAYER0_X = 8;
    private static final int LAYER1_X = 68;
    private static final int LAYER2_X = 128;
    private static final int LAYER_Y = 14;
    private static final int SLOT_STEP = 18;
    private static final int LAYER_SIZE = SLOT_STEP * 3;

    public static final RecipeType<WorkspaceCraftingRecipe> TYPE = RecipeType.create(
            AgesCraftingMod.MODID,
            "workspace_crafting",
            WorkspaceCraftingRecipe.class
    );

    private final IDrawable icon;
    private final IDrawable background;

    public WorkspaceJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.WORKSPACE_TABLE.get()));
        this.background = guiHelper.drawableBuilder(ATLAS, 0, 0, RECIPE_WIDTH, RECIPE_HEIGHT)
                .setTextureSize(RECIPE_WIDTH, RECIPE_HEIGHT)
                .build();
    }

    @Override
    public @NotNull RecipeType<WorkspaceCraftingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.agescrafting.workspace_crafting");
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull WorkspaceCraftingRecipe recipe, @NotNull IFocusGroup focuses) {
        addLayer(builder, recipe, 0, LAYER0_X, LAYER_Y);
        addLayer(builder, recipe, 1, LAYER1_X, LAYER_Y);
        addLayer(builder, recipe, 2, LAYER2_X, LAYER_Y);

        builder.addSlot(RecipeIngredientRole.INPUT, 56, 95)
                .addItemStack(new ItemStack(Items.FLINT));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 95)
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level != null
                        ? Minecraft.getInstance().level.registryAccess()
                        : Minecraft.getInstance().getConnection() != null
                        ? Minecraft.getInstance().getConnection().registryAccess()
                        : net.minecraft.core.RegistryAccess.EMPTY));
    }

    private void addLayer(IRecipeLayoutBuilder builder, WorkspaceCraftingRecipe recipe, int layer, int startX, int startY) {
        for (int z = 0; z < 3; z++) {
            for (int x = 0; x < 3; x++) {
                int index = x + z * 3 + layer * 9;
                Optional<net.minecraft.world.item.crafting.Ingredient> ingredient = recipe.getIngredientAt(index);
                var slot = builder.addSlot(RecipeIngredientRole.INPUT, startX + x * 18, startY + z * 18);
                ingredient.ifPresent(slot::addIngredients);
            }
        }
    }

    @Override
    public void draw(@NotNull WorkspaceCraftingRecipe recipe, @NotNull mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
        drawCentered(guiGraphics, "gui.agescrafting.layer.bottom", LAYER0_X + LAYER_SIZE / 2, 3);
        drawCentered(guiGraphics, "gui.agescrafting.layer.middle", LAYER1_X + LAYER_SIZE / 2, 3);
        drawCentered(guiGraphics, "gui.agescrafting.layer.top", LAYER2_X + LAYER_SIZE / 2, 3);        recipe.getRequiredStage().ifPresent(stage ->
                drawCenteredComponent(guiGraphics, Component.translatable("gui.agescrafting.stage_required", stage), 95, 114));
    }

    private void drawCentered(GuiGraphics guiGraphics, String key, int centerX, int y) {
        drawCenteredComponent(guiGraphics, Component.translatable(key), centerX, y);
    }

    private void drawCenteredComponent(GuiGraphics guiGraphics, Component text, int centerX, int y) {
        var font = Minecraft.getInstance().font;
        int x = centerX - font.width(text) / 2;
        guiGraphics.drawString(font, text, x, y, 0x9A9A9A, false);
    }
}


