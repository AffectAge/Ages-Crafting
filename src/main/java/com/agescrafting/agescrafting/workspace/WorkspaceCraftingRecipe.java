package com.agescrafting.agescrafting.workspace;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.registry.ModRecipeSerializers;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WorkspaceCraftingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Optional<Ingredient>> ingredients;
    private final ItemStack result;
    private final Optional<String> requiredStage;

    public WorkspaceCraftingRecipe(ResourceLocation id, NonNullList<Optional<Ingredient>> ingredients, ItemStack result, Optional<String> requiredStage) {
        this.id = id;
        this.ingredients = ingredients;
        this.result = result;
        this.requiredStage = requiredStage;
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return matchesRotation(container, 0)
                || matchesRotation(container, 1)
                || matchesRotation(container, 2)
                || matchesRotation(container, 3);
    }

    private boolean matchesRotation(Container container, int rotation) {
        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    int patternIndex = x + z * 3 + y * 9;
                    int worldX;
                    int worldZ;

                    switch (rotation) {
                        case 1 -> {
                            // 90 degrees clockwise.
                            worldX = 2 - z;
                            worldZ = x;
                        }
                        case 2 -> {
                            // 180 degrees.
                            worldX = 2 - x;
                            worldZ = 2 - z;
                        }
                        case 3 -> {
                            // 270 degrees clockwise.
                            worldX = z;
                            worldZ = 2 - x;
                        }
                        default -> {
                            // No rotation.
                            worldX = x;
                            worldZ = z;
                        }
                    }

                    int worldIndex = worldX + worldZ * 3 + y * 9;
                    if (!test(ingredients.get(patternIndex), container.getItem(worldIndex))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean test(Optional<Ingredient> ingredient, ItemStack stack) {
        if (ingredient.isEmpty()) {
            return stack.isEmpty();
        }
        return ingredient.get().test(stack);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, net.minecraft.core.RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.WORKSPACE_CRAFTING.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.WORKSPACE_CRAFTING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public List<Optional<Ingredient>> getOptionalIngredients() {
        return ingredients;
    }

    public Optional<Ingredient> getIngredientAt(int index) {
        if (index < 0 || index >= 27) {
            return Optional.empty();
        }
        return ingredients.get(index);
    }

    public Optional<String> getRequiredStage() {
        return requiredStage;
    }

    public static class Serializer implements RecipeSerializer<WorkspaceCraftingRecipe> {
        @Override
        public @NotNull WorkspaceCraftingRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            JsonObject keyObj = GsonHelper.getAsJsonObject(json, "key");
            JsonObject patternObj = GsonHelper.getAsJsonObject(json, "pattern");
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            Map<Character, Ingredient> key = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : keyObj.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new IllegalArgumentException("Workspace key must use single-char symbols");
                }
                char c = entry.getKey().charAt(0);
                if (c == ' ') {
                    throw new IllegalArgumentException("Whitespace cannot be used as key");
                }
                key.put(c, Ingredient.fromJson(entry.getValue()));
            }

            NonNullList<Optional<Ingredient>> ingredients = NonNullList.withSize(27, Optional.empty());

            readLayer(ingredients, key, GsonHelper.getAsJsonArray(patternObj, "bottom"), 0);
            readLayer(ingredients, key, GsonHelper.getAsJsonArray(patternObj, "middle"), 1);
            readLayer(ingredients, key, GsonHelper.getAsJsonArray(patternObj, "top"), 2);

            Optional<String> requiredStage = Optional.empty();
            if (json.has("required_stage")) {
                String raw = GsonHelper.getAsString(json, "required_stage", "").trim();
                if (!raw.isEmpty()) {
                    requiredStage = Optional.of(raw);
                }
            }

            return new WorkspaceCraftingRecipe(recipeId, ingredients, result, requiredStage);
        }

        private static void readLayer(NonNullList<Optional<Ingredient>> ingredients, Map<Character, Ingredient> key, JsonArray rows, int y) {
            if (rows.size() != 3) {
                throw new IllegalArgumentException("Workspace layer must have exactly 3 rows");
            }

            for (int z = 0; z < 3; z++) {
                String row = GsonHelper.convertToString(rows.get(z), "row");
                if (row.length() != 3) {
                    throw new IllegalArgumentException("Workspace row must have exactly 3 chars");
                }

                for (int x = 0; x < 3; x++) {
                    char symbol = row.charAt(x);
                    int index = x + z * 3 + y * 9;

                    if (symbol == ' ') {
                        ingredients.set(index, Optional.empty());
                    } else {
                        Ingredient ingredient = key.get(symbol);
                        if (ingredient == null) {
                            throw new IllegalArgumentException("Undefined key symbol: " + symbol);
                        }
                        ingredients.set(index, Optional.of(ingredient));
                    }
                }
            }
        }

        @Override
        public WorkspaceCraftingRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Optional<Ingredient>> ingredients = NonNullList.withSize(27, Optional.empty());

            for (int i = 0; i < 27; i++) {
                boolean present = buffer.readBoolean();
                if (present) {
                    ingredients.set(i, Optional.of(Ingredient.fromNetwork(buffer)));
                }
            }

            ItemStack result = buffer.readItem();
            Optional<String> requiredStage = buffer.readBoolean() ? Optional.of(buffer.readUtf(128)) : Optional.empty();
            return new WorkspaceCraftingRecipe(recipeId, ingredients, result, requiredStage);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WorkspaceCraftingRecipe recipe) {
            for (int i = 0; i < 27; i++) {
                Optional<Ingredient> ingredient = recipe.ingredients.get(i);
                buffer.writeBoolean(ingredient.isPresent());
                ingredient.ifPresent(value -> value.toNetwork(buffer));
            }
            buffer.writeItem(recipe.result);
            buffer.writeBoolean(recipe.requiredStage.isPresent());
            recipe.requiredStage.ifPresent(stage -> buffer.writeUtf(stage, 128));
        }
    }
}
