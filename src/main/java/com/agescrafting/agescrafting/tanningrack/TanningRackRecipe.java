package com.agescrafting.agescrafting.tanningrack;

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

public class TanningRackRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final Ingredient tool;
    private final ItemStack result;
    private final int hits;
    private final int durabilityPerHit;
    private final ItemStack failureOutput;
    private final float failureChance;

    public TanningRackRecipe(
            ResourceLocation id,
            Ingredient ingredient,
            Ingredient tool,
            ItemStack result,
            int hits,
            int durabilityPerHit,
            ItemStack failureOutput,
            float failureChance
    ) {
        this.id = id;
        this.ingredient = ingredient;
        this.tool = tool;
        this.result = result.copy();
        this.hits = Math.max(1, hits);
        this.durabilityPerHit = Math.max(0, durabilityPerHit);
        this.failureOutput = failureOutput.copy();
        this.failureChance = Math.max(0.0F, Math.min(1.0F, failureChance));
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
        return ModRecipeSerializers.TANNING_RACK.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.TANNING_RACK.get();
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

    public int durabilityPerHit() {
        return durabilityPerHit;
    }

    public ItemStack failureOutput() {
        return failureOutput.copy();
    }

    public float failureChance() {
        return failureChance;
    }

    public boolean hasFailureOutput() {
        return !failureOutput.isEmpty() && failureChance > 0.0F;
    }

    public static class Serializer implements RecipeSerializer<TanningRackRecipe> {
        @Override
        public @NotNull TanningRackRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            Ingredient tool = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tool"));
            ItemStack result = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int hits = GsonHelper.getAsInt(json, "hits", 6);
            int durabilityPerHit = GsonHelper.getAsInt(json, "durability_per_hit", 1);
            ItemStack failureOutput = ItemStack.EMPTY;
            if (json.has("failure_output")) {
                failureOutput = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "failure_output"));
            }
            float failureChance = GsonHelper.getAsFloat(json, "failure_chance", 0.0F);
            return new TanningRackRecipe(recipeId, ingredient, tool, result, hits, durabilityPerHit, failureOutput, failureChance);
        }

        @Override
        public @Nullable TanningRackRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            Ingredient tool = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int hits = buffer.readVarInt();
            int durabilityPerHit = buffer.readVarInt();
            ItemStack failureOutput = buffer.readItem();
            float failureChance = buffer.readFloat();
            return new TanningRackRecipe(recipeId, ingredient, tool, result, hits, durabilityPerHit, failureOutput, failureChance);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TanningRackRecipe recipe) {
            recipe.ingredient().toNetwork(buffer);
            recipe.tool().toNetwork(buffer);
            buffer.writeItem(recipe.result());
            buffer.writeVarInt(recipe.hits());
            buffer.writeVarInt(recipe.durabilityPerHit());
            buffer.writeItem(recipe.failureOutput());
            buffer.writeFloat(recipe.failureChance());
        }
    }
}
