package com.agescrafting.agescrafting.barrel.recipe;

import com.agescrafting.agescrafting.compat.sereneseasons.SereneSeasonsCompat;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BarrelRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final List<IngredientWithCount> itemIngredients;
    private final List<FluidStack> fluidIngredients;
    private final boolean requiresSealed;
    private final int durationTicks;
    private final NonNullList<ItemStack> itemResults;
    private final List<FluidStack> fluidResults;
    private final @Nullable SeasonMultipliers seasonMultipliers;

    public BarrelRecipe(
            ResourceLocation id,
            List<IngredientWithCount> itemIngredients,
            List<FluidStack> fluidIngredients,
            boolean requiresSealed,
            int durationTicks,
            NonNullList<ItemStack> itemResults,
            List<FluidStack> fluidResults,
            @Nullable SeasonMultipliers seasonMultipliers
    ) {
        this.id = id;
        this.itemIngredients = List.copyOf(itemIngredients);
        this.fluidIngredients = copyFluids(fluidIngredients);
        this.requiresSealed = requiresSealed;
        this.durationTicks = Math.max(0, durationTicks);
        this.itemResults = NonNullList.create();
        this.itemResults.addAll(itemResults);
        this.fluidResults = copyFluids(fluidResults);
        this.seasonMultipliers = seasonMultipliers;
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        if (container instanceof BarrelRecipeInput input) {
            return matches(input);
        }
        return false;
    }

    public boolean matches(BarrelRecipeInput input) {
        if (!matchesFluids(input.fluids())) {
            return false;
        }

        int[] available = new int[input.items().size()];
        for (int i = 0; i < input.items().size(); i++) {
            available[i] = input.items().get(i).getCount();
        }

        for (IngredientWithCount ingredient : itemIngredients) {
            int remaining = ingredient.count();
            for (int slot = 0; slot < input.items().size() && remaining > 0; slot++) {
                ItemStack stack = input.items().get(slot);
                if (!stack.isEmpty() && ingredient.ingredient().test(stack)) {
                    int taken = Math.min(remaining, available[slot]);
                    available[slot] -= taken;
                    remaining -= taken;
                }
            }
            if (remaining > 0) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesFluids(List<FluidStack> inputFluids) {
        if (fluidIngredients.isEmpty()) {
            return true;
        }

        int[] available = new int[inputFluids.size()];
        for (int i = 0; i < inputFluids.size(); i++) {
            available[i] = inputFluids.get(i).getAmount();
        }

        for (FluidStack need : fluidIngredients) {
            int remaining = need.getAmount();
            for (int i = 0; i < inputFluids.size() && remaining > 0; i++) {
                FluidStack in = inputFluids.get(i);
                if (in.isEmpty() || !in.isFluidEqual(need)) {
                    continue;
                }
                int taken = Math.min(remaining, available[i]);
                available[i] -= taken;
                remaining -= taken;
            }
            if (remaining > 0) {
                return false;
            }
        }

        return true;
    }

    public boolean requiresSealed() {
        return requiresSealed;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public float getDurationMultiplier(@Nullable Level level) {
        if (seasonMultipliers != null) {
            return seasonMultipliers.forSeason(SereneSeasonsCompat.getSeasonGroup(level));
        }
        return SereneSeasonsCompat.getBarrelDurationMultiplier(level);
    }

    public boolean hasCustomSeasonMultipliers() {
        return seasonMultipliers != null;
    }

    public List<IngredientWithCount> itemIngredients() {
        return itemIngredients;
    }

    public List<FluidStack> fluidIngredients() {
        return copyFluids(fluidIngredients);
    }

    // Backward compatibility helper.
    public FluidStack fluidIngredient() {
        return fluidIngredients.isEmpty() ? FluidStack.EMPTY : fluidIngredients.get(0).copy();
    }

    public List<ItemStack> itemResults() {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack stack : itemResults) {
            copy.add(stack.copy());
        }
        return copy;
    }

    public List<FluidStack> fluidResults() {
        return copyFluids(fluidResults);
    }

    // Backward compatibility helper.
    public FluidStack fluidResult() {
        return fluidResults.isEmpty() ? FluidStack.EMPTY : fluidResults.get(0).copy();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, net.minecraft.core.RegistryAccess registryAccess) {
        return itemResults.isEmpty() ? ItemStack.EMPTY : itemResults.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return itemResults.isEmpty() ? ItemStack.EMPTY : itemResults.get(0).copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.BARREL.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.BARREL.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public record IngredientWithCount(Ingredient ingredient, int count) {
        public IngredientWithCount {
            count = Math.max(1, count);
        }
    }

    public record SeasonMultipliers(float spring, float summer, float autumn, float winter) {
        public SeasonMultipliers {
            spring = sanitizeMultiplier(spring);
            summer = sanitizeMultiplier(summer);
            autumn = sanitizeMultiplier(autumn);
            winter = sanitizeMultiplier(winter);
        }

        public float forSeason(SereneSeasonsCompat.SeasonGroup seasonGroup) {
            return switch (seasonGroup) {
                case SUMMER -> summer;
                case AUTUMN -> autumn;
                case WINTER -> winter;
                case SPRING, UNKNOWN -> spring;
            };
        }

        private static float sanitizeMultiplier(float value) {
            if (!Float.isFinite(value)) {
                return 1.0F;
            }
            return Math.max(0.05F, value);
        }
    }

    public static class Serializer implements RecipeSerializer<BarrelRecipe> {
        @Override
        public @NotNull BarrelRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            List<IngredientWithCount> ingredients = new ArrayList<>();
            JsonArray ingredientArray = GsonHelper.getAsJsonArray(json, "ingredients", new JsonArray());
            for (JsonElement element : ingredientArray) {
                JsonObject ingredientObj = GsonHelper.convertToJsonObject(element, "ingredient");
                Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredientObj, "ingredient"));
                int count = GsonHelper.getAsInt(ingredientObj, "count", 1);
                ingredients.add(new IngredientWithCount(ingredient, count));
            }

            List<FluidStack> fluidIngredients = readFluidListFromJson(json, "fluids", "fluid");
            boolean requiresSealed = GsonHelper.getAsBoolean(json, "requires_sealed", false);
            int durationTicks = GsonHelper.getAsInt(json, "duration_ticks", 0);

            NonNullList<ItemStack> itemResults = NonNullList.create();
            JsonArray itemResultArray = GsonHelper.getAsJsonArray(json, "item_results", new JsonArray());
            for (JsonElement element : itemResultArray) {
                JsonObject resultObj = GsonHelper.convertToJsonObject(element, "item_result");
                itemResults.add(net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(resultObj));
            }

            List<FluidStack> fluidResults = readFluidListFromJson(json, "fluid_results", "fluid_result");
            SeasonMultipliers seasonMultipliers = readSeasonMultipliers(json);

            return new BarrelRecipe(recipeId, ingredients, fluidIngredients, requiresSealed, durationTicks, itemResults, fluidResults, seasonMultipliers);
        }

        @Override
        public BarrelRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int ingredientCount = buffer.readVarInt();
            List<IngredientWithCount> ingredients = new ArrayList<>(ingredientCount);
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(new IngredientWithCount(Ingredient.fromNetwork(buffer), buffer.readVarInt()));
            }

            List<FluidStack> fluidIngredients = readFluidList(buffer);
            boolean requiresSealed = buffer.readBoolean();
            int durationTicks = buffer.readVarInt();

            int itemResultCount = buffer.readVarInt();
            NonNullList<ItemStack> itemResults = NonNullList.withSize(itemResultCount, ItemStack.EMPTY);
            for (int i = 0; i < itemResultCount; i++) {
                itemResults.set(i, buffer.readItem());
            }

            List<FluidStack> fluidResults = readFluidList(buffer);
            SeasonMultipliers seasonMultipliers = null;
            if (buffer.readBoolean()) {
                seasonMultipliers = new SeasonMultipliers(
                        buffer.readFloat(),
                        buffer.readFloat(),
                        buffer.readFloat(),
                        buffer.readFloat()
                );
            }
            return new BarrelRecipe(recipeId, ingredients, fluidIngredients, requiresSealed, durationTicks, itemResults, fluidResults, seasonMultipliers);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BarrelRecipe recipe) {
            buffer.writeVarInt(recipe.itemIngredients.size());
            for (IngredientWithCount ingredient : recipe.itemIngredients) {
                ingredient.ingredient().toNetwork(buffer);
                buffer.writeVarInt(ingredient.count());
            }

            writeFluidList(buffer, recipe.fluidIngredients);
            buffer.writeBoolean(recipe.requiresSealed);
            buffer.writeVarInt(recipe.durationTicks);

            buffer.writeVarInt(recipe.itemResults.size());
            for (ItemStack stack : recipe.itemResults) {
                buffer.writeItem(stack);
            }

            writeFluidList(buffer, recipe.fluidResults);
            buffer.writeBoolean(recipe.seasonMultipliers != null);
            if (recipe.seasonMultipliers != null) {
                buffer.writeFloat(recipe.seasonMultipliers.spring());
                buffer.writeFloat(recipe.seasonMultipliers.summer());
                buffer.writeFloat(recipe.seasonMultipliers.autumn());
                buffer.writeFloat(recipe.seasonMultipliers.winter());
            }
        }

        private static @Nullable SeasonMultipliers readSeasonMultipliers(JsonObject json) {
            if (!json.has("season_multipliers")) {
                return null;
            }

            JsonObject multipliersJson = GsonHelper.getAsJsonObject(json, "season_multipliers");
            float spring = GsonHelper.getAsFloat(multipliersJson, "spring", 1.0F);
            float summer = GsonHelper.getAsFloat(multipliersJson, "summer", 1.0F);
            float autumn = GsonHelper.getAsFloat(multipliersJson, "autumn", 1.0F);
            float winter = GsonHelper.getAsFloat(multipliersJson, "winter", 1.0F);
            return new SeasonMultipliers(spring, summer, autumn, winter);
        }

        private static List<FluidStack> readFluidListFromJson(JsonObject json, String listKey, String singleKey) {
            List<FluidStack> fluids = new ArrayList<>();

            JsonArray array = GsonHelper.getAsJsonArray(json, listKey, new JsonArray());
            for (JsonElement element : array) {
                FluidStack stack = readFluidStack(GsonHelper.convertToJsonObject(element, listKey));
                if (!stack.isEmpty()) {
                    fluids.add(stack);
                }
            }

            if (fluids.isEmpty() && json.has(singleKey)) {
                FluidStack stack = readFluidStack(GsonHelper.getAsJsonObject(json, singleKey));
                if (!stack.isEmpty()) {
                    fluids.add(stack);
                }
            }

            return fluids;
        }

        private static FluidStack readFluidStack(JsonObject json) {
            if (!json.has("fluid")) {
                return FluidStack.EMPTY;
            }

            String fluidId = GsonHelper.getAsString(json, "fluid");
            int amount = GsonHelper.getAsInt(json, "amount", 0);
            var fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidId));
            if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                throw new IllegalArgumentException("Unknown fluid in barrel recipe: " + fluidId);
            }
            return new FluidStack(fluid, Math.max(0, amount));
        }

        private static List<FluidStack> readFluidList(FriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            List<FluidStack> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                FluidStack stack = readFluidStack(buffer);
                if (!stack.isEmpty()) {
                    list.add(stack);
                }
            }
            return list;
        }

        private static void writeFluidList(FriendlyByteBuf buffer, List<FluidStack> list) {
            buffer.writeVarInt(list.size());
            for (FluidStack stack : list) {
                writeFluidStack(buffer, stack);
            }
        }

        private static FluidStack readFluidStack(FriendlyByteBuf buffer) {
            boolean present = buffer.readBoolean();
            if (!present) {
                return FluidStack.EMPTY;
            }
            ResourceLocation id = buffer.readResourceLocation();
            int amount = buffer.readVarInt();
            var fluid = ForgeRegistries.FLUIDS.getValue(id);
            if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                return FluidStack.EMPTY;
            }
            return new FluidStack(fluid, amount);
        }

        private static void writeFluidStack(FriendlyByteBuf buffer, FluidStack stack) {
            if (stack.isEmpty()) {
                buffer.writeBoolean(false);
                return;
            }
            buffer.writeBoolean(true);
            buffer.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(stack.getFluid()));
            buffer.writeVarInt(stack.getAmount());
        }
    }

    private static List<FluidStack> copyFluids(List<FluidStack> source) {
        List<FluidStack> copy = new ArrayList<>(source.size());
        for (FluidStack stack : source) {
            copy.add(stack.copy());
        }
        return copy;
    }
}
