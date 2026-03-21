package com.agescrafting.agescrafting.compat.sereneseasons;

import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Locale;

public final class SereneSeasonsCompat {
    public enum SeasonGroup {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER,
        UNKNOWN
    }

    private static final boolean LOADED = ModList.get().isLoaded("sereneseasons");

    private static boolean reflectionInitDone;
    private static @Nullable Method getSeasonStateMethod;
    private static @Nullable Method getSubSeasonMethod;
    private static @Nullable Method getSeasonMethod;

    private SereneSeasonsCompat() {
    }

    public static boolean isLoaded() {
        return LOADED;
    }

    public static SeasonGroup getSeasonGroup(@Nullable Level level) {
        if (!LOADED || level == null) {
            return SeasonGroup.UNKNOWN;
        }

        String seasonName = resolveSeasonName(level);
        if (seasonName == null) {
            return SeasonGroup.UNKNOWN;
        }

        if (seasonName.contains("SUMMER")) {
            return SeasonGroup.SUMMER;
        }
        if (seasonName.contains("AUTUMN") || seasonName.contains("FALL")) {
            return SeasonGroup.AUTUMN;
        }
        if (seasonName.contains("WINTER")) {
            return SeasonGroup.WINTER;
        }
        if (seasonName.contains("SPRING")) {
            return SeasonGroup.SPRING;
        }
        return SeasonGroup.UNKNOWN;
    }

    public static float getBarrelDurationMultiplier(@Nullable Level level) {
        return switch (getSeasonGroup(level)) {
            case SUMMER -> getConfiguredMultiplier(AgesCraftingConfig.SERVER.barrelSeasonSummerMultiplier.get());
            case AUTUMN -> getConfiguredMultiplier(AgesCraftingConfig.SERVER.barrelSeasonAutumnMultiplier.get());
            case WINTER -> getConfiguredMultiplier(AgesCraftingConfig.SERVER.barrelSeasonWinterMultiplier.get());
            case SPRING, UNKNOWN -> getConfiguredMultiplier(AgesCraftingConfig.SERVER.barrelSeasonSpringMultiplier.get());
        };
    }

    private static float getConfiguredMultiplier(double value) {
        if (!Double.isFinite(value)) {
            return 1.0F;
        }
        return (float) Math.max(0.05D, value);
    }

    private static @Nullable String resolveSeasonName(Level level) {
        initReflection();
        if (getSeasonStateMethod == null) {
            return null;
        }

        try {
            Object seasonState = getSeasonStateMethod.invoke(null, level);
            if (seasonState == null) {
                return null;
            }

            if (getSubSeasonMethod != null) {
                Object subSeason = getSubSeasonMethod.invoke(seasonState);
                if (subSeason != null) {
                    return subSeason.toString().toUpperCase(Locale.ROOT);
                }
            }

            if (getSeasonMethod != null) {
                Object season = getSeasonMethod.invoke(seasonState);
                if (season != null) {
                    return season.toString().toUpperCase(Locale.ROOT);
                }
            }
        } catch (Throwable ignored) {
            return null;
        }

        return null;
    }

    private static void initReflection() {
        if (reflectionInitDone) {
            return;
        }
        reflectionInitDone = true;

        if (!LOADED) {
            return;
        }

        try {
            Class<?> seasonHelperClass = Class.forName("sereneseasons.api.season.SeasonHelper");
            getSeasonStateMethod = seasonHelperClass.getMethod("getSeasonState", Level.class);

            Class<?> seasonStateClass = Class.forName("sereneseasons.api.season.Season$SeasonState");
            getSubSeasonMethod = seasonStateClass.getMethod("getSubSeason");
            getSeasonMethod = seasonStateClass.getMethod("getSeason");
        } catch (Throwable ignored) {
            getSeasonStateMethod = null;
            getSubSeasonMethod = null;
            getSeasonMethod = null;
        }
    }
}
