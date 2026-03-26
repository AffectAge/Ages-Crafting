package com.agescrafting.agescrafting.choppingblock;

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

public class ChoppingBlockRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final Ingredient tool;
    private final ItemStack result;
    private final int chopsRequired;
    private final int durabilityPerChop;

    public ChoppingBlockRecipe(ResourceLocation id, Ingredient ingredient, Ingredient tool, ItemStack result, int chopsRequired, int durabilityPerChop) {
        this.id = id;
        this.ingredient = ingredient;
        this.tool = tool;
        this.result = result.copy();
        this.chopsRequired = Math.max(1, chopsRequired);
        this.durabilityPerChop = Math.max(0, durabilityPerChop);
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return ingredient.test(container.getItem(0));
    }

    public boolean matchesInput(ItemStack stack) {
        return ingredient.test(stack);
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
        return ModRecipeSerializers.CHOPPING_BLOCK.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.CHOPPING_BLOCK.get();
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

    public int chopsRequired() {
        return chopsRequired;
    }

    public int durabilityPerChop() {
        return durabilityPerChop;
    }

    public static class Serializer implements RecipeSerializer<ChoppingBlockRecipe> {
        @Override
        public @NotNull ChoppingBlockRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            Ingredient tool = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tool"));
            ItemStack result = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int chopsRequired = GsonHelper.getAsInt(json, "chops_required", 6);
            int durabilityPerChop = GsonHelper.getAsInt(json, "durability_per_chop", 1);
            return new ChoppingBlockRecipe(recipeId, ingredient, tool, result, chopsRequired, durabilityPerChop);
        }

        @Override
        public @Nullable ChoppingBlockRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            Ingredient tool = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int chopsRequired = buffer.readVarInt();
            int durabilityPerChop = buffer.readVarInt();
            return new ChoppingBlockRecipe(recipeId, ingredient, tool, result, chopsRequired, durabilityPerChop);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ChoppingBlockRecipe recipe) {
            recipe.ingredient().toNetwork(buffer);
            recipe.tool().toNetwork(buffer);
            buffer.writeItem(recipe.result());
            buffer.writeVarInt(recipe.chopsRequired());
            buffer.writeVarInt(recipe.durabilityPerChop());
        }
    }
}
