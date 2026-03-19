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
            builder.pop();
        }
    }
}
