package com.agescrafting.agescrafting.registry;

import com.agescrafting.agescrafting.AgesCraftingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class ModFluids {
    private static final ResourceLocation TANNIN_STILL = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "block/tannin_still");
    private static final ResourceLocation TANNIN_FLOW = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "block/tannin_flow");

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, AgesCraftingMod.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, AgesCraftingMod.MODID);

    public static final RegistryObject<FluidType> TANNIN_FLUID_TYPE = FLUID_TYPES.register("tannin",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1080)
                    .viscosity(1400)
                    .lightLevel(0)
                    .canExtinguish(true)
                    .canHydrate(true)
            ) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return TANNIN_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return TANNIN_FLOW;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFF7A4C2E;
                        }
                    });
                }
            }
    );

    private static ForgeFlowingFluid.Properties tanninProperties;

    public static final RegistryObject<FlowingFluid> SOURCE_TANNIN = FLUIDS.register("tannin",
            () -> new ForgeFlowingFluid.Source(getTanninProperties()));

    public static final RegistryObject<FlowingFluid> FLOWING_TANNIN = FLUIDS.register("flowing_tannin",
            () -> new ForgeFlowingFluid.Flowing(getTanninProperties()));

    private static ForgeFlowingFluid.Properties getTanninProperties() {
        if (tanninProperties == null) {
            tanninProperties = new ForgeFlowingFluid.Properties(
                    TANNIN_FLUID_TYPE,
                    SOURCE_TANNIN,
                    FLOWING_TANNIN
            )
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(2)
                    .block(ModBlocks.TANNIN_FLUID_BLOCK)
                    .bucket(ModItems.TANNIN_BUCKET);
        }
        return tanninProperties;
    }

    private ModFluids() {
    }
}
