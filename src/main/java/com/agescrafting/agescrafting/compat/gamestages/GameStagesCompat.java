package com.agescrafting.agescrafting.compat.gamestages;

import com.agescrafting.agescrafting.workspace.WorkspaceCraftingRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;

public final class GameStagesCompat {
    private static final String MOD_ID = "gamestages";
    private static final boolean LOADED = ModList.get().isLoaded(MOD_ID);
    private static final Method HAS_STAGE_METHOD = resolveHasStageMethod();

    private GameStagesCompat() {
    }

    public static boolean canCraft(Player player, WorkspaceCraftingRecipe recipe) {
        Optional<String> requiredStage = recipe.getRequiredStage();
        if (requiredStage.isEmpty()) {
            return true;
        }

        // If GameStages is not installed, stage restrictions are ignored by design.
        if (!LOADED || HAS_STAGE_METHOD == null) {
            return true;
        }

        try {
            Object result = HAS_STAGE_METHOD.invoke(null, player, requiredStage.get());
            return result instanceof Boolean value && value;
        } catch (ReflectiveOperationException ignored) {
            // Optional integration should never hard-fail crafting flow.
            return true;
        }
    }

    private static Method resolveHasStageMethod() {
        if (!LOADED) {
            return null;
        }

        try {
            Class<?> helper = Class.forName("net.darkhax.gamestages.GameStageHelper");
            return helper.getMethod("hasStage", Player.class, String.class);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
