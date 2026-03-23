package com.agescrafting.agescrafting.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AgesCraftingConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SERVER = new Server(builder);
        SERVER_SPEC = builder.build();
    }

    private AgesCraftingConfig() {
    }

    public static final class Server {
        public final ForgeConfigSpec.BooleanValue enableVanillaRecipes;
        public final ForgeConfigSpec.IntValue barrelTankCapacityMb;
        public final ForgeConfigSpec.BooleanValue barrelRainFillEnabled;
        public final ForgeConfigSpec.IntValue barrelRainFillAmountMb;
        public final ForgeConfigSpec.IntValue barrelRainFillIntervalTicks;
        public final ForgeConfigSpec.ConfigValue<String> barrelRainFillFluid;
        public final ForgeConfigSpec.DoubleValue barrelSeasonSpringMultiplier;
        public final ForgeConfigSpec.DoubleValue barrelSeasonSummerMultiplier;
        public final ForgeConfigSpec.DoubleValue barrelSeasonAutumnMultiplier;
        public final ForgeConfigSpec.DoubleValue barrelSeasonWinterMultiplier;
        public final ForgeConfigSpec.DoubleValue campfireRainExtinguishChancePerSecond;
        public final ForgeConfigSpec.DoubleValue campfireRainCookTimeMultiplier;
        public final ForgeConfigSpec.DoubleValue campfireAshCookPenaltyPerLevel;
        public final ForgeConfigSpec.IntValue pitKilnMaxStackSize;
        public final ForgeConfigSpec.DoubleValue pitKilnBaseRecipeDurationModifier;
        public final ForgeConfigSpec.DoubleValue pitKilnVariableSpeedModifier;
        public final ForgeConfigSpec.BooleanValue pitKilnExtinguishedByRain;
        public final ForgeConfigSpec.IntValue pitKilnRainExtinguishTicks;

        private Server(ForgeConfigSpec.Builder builder) {
            builder.push("recipes");
            enableVanillaRecipes = builder
                    .comment("Enable vanilla crafting recipes for blocks overridden by Ages Crafting.")
                    .define("enableVanillaRecipes", false);
            builder.pop();

            builder.push("barrel");
            barrelTankCapacityMb = builder
                    .comment("Fluid capacity of the Ages Crafting Barrel in mB.")
                    .defineInRange("tankCapacityMb", 10000, 1000, 100000);
            barrelRainFillEnabled = builder
                    .comment("Allow open and empty barrels to collect rainwater automatically.")
                    .define("rainFillEnabled", true);
            barrelRainFillAmountMb = builder
                    .comment("How much rain fluid (in mB) is added per rain fill tick.")
                    .defineInRange("rainFillAmountMb", 20, 1, 1000);
            barrelRainFillIntervalTicks = builder
                    .comment("How often rain fill runs, in ticks.")
                    .defineInRange("rainFillIntervalTicks", 20, 1, 1200);
            barrelRainFillFluid = builder
                    .comment("Fluid id used for rain fill, e.g. minecraft:water")
                    .define("rainFillFluid", "minecraft:water");

            builder.push("seasonMultipliers");
            barrelSeasonSpringMultiplier = builder
                    .comment("Global fallback barrel recipe duration multiplier for spring.")
                    .defineInRange("spring", 1.0D, 0.05D, 10.0D);
            barrelSeasonSummerMultiplier = builder
                    .comment("Global fallback barrel recipe duration multiplier for summer.")
                    .defineInRange("summer", 0.85D, 0.05D, 10.0D);
            barrelSeasonAutumnMultiplier = builder
                    .comment("Global fallback barrel recipe duration multiplier for autumn.")
                    .defineInRange("autumn", 1.10D, 0.05D, 10.0D);
            barrelSeasonWinterMultiplier = builder
                    .comment("Global fallback barrel recipe duration multiplier for winter.")
                    .defineInRange("winter", 1.35D, 0.05D, 10.0D);
            builder.pop();
            builder.pop();

            builder.push("primitiveCampfire");
            campfireRainExtinguishChancePerSecond = builder
                    .comment("Chance per second that a lit primitive campfire extinguishes while directly exposed to rain (0.0 - 1.0).")
                    .defineInRange("rainExtinguishChancePerSecond", 0.08D, 0.0D, 1.0D);
            campfireRainCookTimeMultiplier = builder
                    .comment("Cooking time multiplier while a lit primitive campfire is exposed to rain.")
                    .defineInRange("rainCookTimeMultiplier", 1.50D, 1.0D, 10.0D);
            campfireAshCookPenaltyPerLevel = builder
                    .comment("Additional cooking time multiplier added per ash level. Final multiplier: 1 + ash * value.")
                    .defineInRange("ashCookPenaltyPerLevel", 0.12D, 0.0D, 2.0D);
            builder.pop();

            builder.push("pitKiln");
            pitKilnMaxStackSize = builder
                    .comment("Maximum stack size that can be placed into pit kiln input.")
                    .defineInRange("maxStackSize", 8, 1, 64);
            pitKilnBaseRecipeDurationModifier = builder
                    .comment("Multiplicative modifier applied to all pit kiln recipe durations.")
                    .defineInRange("baseRecipeDurationModifier", 1.0D, 0.01D, 20.0D);
            pitKilnVariableSpeedModifier = builder
                    .comment("Speed scalar for low item counts. 0.5 means one item takes 50% of full-stack duration.")
                    .defineInRange("variableSpeedModifier", 0.5D, 0.0D, 1.0D);
            pitKilnExtinguishedByRain = builder
                    .comment("If true, active pit kilns are extinguished by rain after a delay.")
                    .define("extinguishedByRain", true);
            pitKilnRainExtinguishTicks = builder
                    .comment("Ticks of direct rain exposure before an active pit kiln is extinguished.")
                    .defineInRange("rainExtinguishTicks", 200, 1, 72000);
            builder.pop();
        }
    }
}
