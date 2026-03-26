package com.agescrafting.agescrafting.anvil;

import com.agescrafting.agescrafting.registry.ModRecipeSerializers;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnvilRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final Ingredient tool;
    private final ItemStack result;
    private final int hits;

    public AnvilRecipe(ResourceLocation id, Ingredient ingredient, Ingredient tool, ItemStack result, int hits) {
        this.id = id;
        this.ingredient = ingredient;
        this.tool = tool;
        this.result = result.copy();
        this.hits = Math.max(1, hits);
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return ingredient.test(container.getItem(0));
    }

    public boolean matchesTool(ItemStack stack) {
        return tool.test(stack);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull net.minecraft.core.RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > 0;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull net.minecraft.core.RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ANVIL.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.ANVIL.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public Ingredient tool() {
        return tool;
    }

    public ItemStack result() {
        return result.copy();
    }

    public int hits() {
        return hits;
    }

    public static class Serializer implements RecipeSerializer<AnvilRecipe> {
        @Override
        public @NotNull AnvilRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            Ingredient tool = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tool"));
            ItemStack result = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int hits = GsonHelper.getAsInt(json, "hits", 6);
            return new AnvilRecipe(recipeId, ingredient, tool, result, hits);
        }

        @Override
        public @Nullable AnvilRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            Ingredient tool = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int hits = buffer.readVarInt();
            return new AnvilRecipe(recipeId, ingredient, tool, result, hits);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AnvilRecipe recipe) {
            recipe.ingredient().toNetwork(buffer);
            recipe.tool().toNetwork(buffer);
            buffer.writeItem(recipe.result());
            buffer.writeVarInt(recipe.hits());
        }
    }
}
