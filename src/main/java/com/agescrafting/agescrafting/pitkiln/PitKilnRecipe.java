package com.agescrafting.agescrafting.pitkiln;

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

public class PitKilnRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int durationTicks;
    private final float failureChance;
    private final ItemStack failureResult;

    public PitKilnRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int durationTicks, float failureChance, ItemStack failureResult) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result.copy();
        this.durationTicks = Math.max(1, durationTicks);
        this.failureChance = Math.max(0.0F, Math.min(1.0F, failureChance));
        this.failureResult = failureResult.copy();
    }

    public boolean matches(ItemStack stack) {
        return ingredient.test(stack);
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return ingredient.test(container.getItem(0));
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
        return ModRecipeSerializers.PIT_KILN.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.PIT_KILN.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result.copy();
    }

    public int durationTicks() {
        return durationTicks;
    }

    public float failureChance() {
        return failureChance;
    }

    public ItemStack failureResult() {
        return failureResult.copy();
    }

    public static class Serializer implements RecipeSerializer<PitKilnRecipe> {
        @Override
        public @NotNull PitKilnRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack result = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int durationTicks = GsonHelper.getAsInt(json, "duration_ticks", 1200);
            float failureChance = GsonHelper.getAsFloat(json, "failure_chance", 0.0F);
            ItemStack failureResult = ItemStack.EMPTY;
            if (json.has("failure_result")) {
                failureResult = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "failure_result"));
            }
            return new PitKilnRecipe(recipeId, ingredient, result, durationTicks, failureChance, failureResult);
        }

        @Override
        public @Nullable PitKilnRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int durationTicks = buffer.readVarInt();
            float failureChance = buffer.readFloat();
            ItemStack failureResult = buffer.readItem();
            return new PitKilnRecipe(recipeId, ingredient, result, durationTicks, failureChance, failureResult);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PitKilnRecipe recipe) {
            recipe.ingredient().toNetwork(buffer);
            buffer.writeItem(recipe.result());
            buffer.writeVarInt(recipe.durationTicks());
            buffer.writeFloat(recipe.failureChance());
            buffer.writeItem(recipe.failureResult());
        }
    }
}
