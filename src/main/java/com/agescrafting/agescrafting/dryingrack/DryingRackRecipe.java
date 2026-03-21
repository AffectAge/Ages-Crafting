package com.agescrafting.agescrafting.dryingrack;

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

public class DryingRackRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int durationTicks;

    public DryingRackRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int durationTicks) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result.copy();
        this.durationTicks = Math.max(1, durationTicks);
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
        return ModRecipeSerializers.DRYING_RACK.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.DRYING_RACK.get();
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

    public static class Serializer implements RecipeSerializer<DryingRackRecipe> {
        @Override
        public @NotNull DryingRackRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack result = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int durationTicks = GsonHelper.getAsInt(json, "duration_ticks", 200);
            return new DryingRackRecipe(recipeId, ingredient, result, durationTicks);
        }

        @Override
        public DryingRackRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int durationTicks = buffer.readVarInt();
            return new DryingRackRecipe(recipeId, ingredient, result, durationTicks);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DryingRackRecipe recipe) {
            recipe.ingredient().toNetwork(buffer);
            buffer.writeItem(recipe.result());
            buffer.writeVarInt(recipe.durationTicks());
        }
    }
}
