package com.agescrafting.agescrafting.recipe.condition;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import org.jetbrains.annotations.NotNull;

public final class EnableVanillaRecipesCondition implements ICondition {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "enable_vanilla_recipes");
    public static final EnableVanillaRecipesCondition INSTANCE = new EnableVanillaRecipesCondition();
    public static final Serializer SERIALIZER = new Serializer();

    private EnableVanillaRecipesCondition() {
    }

    public static void register() {
        CraftingHelper.register(SERIALIZER);
    }

    @Override
    public @NotNull ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return AgesCraftingConfig.SERVER.enableVanillaRecipes.get();
    }

    public static final class Serializer implements IConditionSerializer<EnableVanillaRecipesCondition> {
        @Override
        public void write(JsonObject json, EnableVanillaRecipesCondition value) {
        }

        @Override
        public @NotNull EnableVanillaRecipesCondition read(@NotNull JsonObject json) {
            return INSTANCE;
        }

        @Override
        public @NotNull ResourceLocation getID() {
            return ID;
        }
    }
}
