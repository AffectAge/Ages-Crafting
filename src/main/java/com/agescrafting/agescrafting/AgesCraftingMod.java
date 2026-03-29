package com.agescrafting.agescrafting;

import com.agescrafting.agescrafting.anvil.AnvilBlockEntityRenderer;
import com.agescrafting.agescrafting.barrel.BarrelBlockEntityRenderer;
import com.agescrafting.agescrafting.barrel.BarrelScreen;
import com.agescrafting.agescrafting.campfire.PrimitiveCampfireBlockEntityRenderer;
import com.agescrafting.agescrafting.choppingblock.ChoppingBlockBlockEntityRenderer;
import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlockEntityRenderer;
import com.agescrafting.agescrafting.pitkiln.PitKilnBlockEntityRenderer;
import com.agescrafting.agescrafting.recipe.condition.EnableVanillaRecipesCondition;
import com.agescrafting.agescrafting.registry.ModBlockEntities;
import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.registry.ModCreativeTabs;
import com.agescrafting.agescrafting.registry.ModItems;
import com.agescrafting.agescrafting.registry.ModMenuTypes;
import com.agescrafting.agescrafting.registry.ModRecipeSerializers;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import com.agescrafting.agescrafting.registry.ModSoundEvents;
import com.agescrafting.agescrafting.tanningrack.TanningRackBlockEntityRenderer;
import com.agescrafting.agescrafting.workspace.WorkspaceTableRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AgesCraftingMod.MODID)
public class AgesCraftingMod {
    public static final String MODID = "agescrafting";

    public AgesCraftingMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(this::onCommonSetup);

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENUS.register(modBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
        ModRecipeTypes.RECIPE_TYPES.register(modBus);
        ModSoundEvents.SOUND_EVENTS.register(modBus);
        context.registerConfig(ModConfig.Type.SERVER, AgesCraftingConfig.SERVER_SPEC);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(EnableVanillaRecipesCondition::register);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> BlockEntityRenderers.register(
                    ModBlockEntities.WORKSPACE_TABLE_BE.get(),
                    WorkspaceTableRenderer::new
            ));
            event.enqueueWork(() -> BlockEntityRenderers.register(
                    ModBlockEntities.ANVIL_BE.get(),
                    AnvilBlockEntityRenderer::new
            ));
            event.enqueueWork(() -> BlockEntityRenderers.register(
                    ModBlockEntities.BARREL_BE.get(),
                    BarrelBlockEntityRenderer::new
            ));
            event.enqueueWork(() -> BlockEntityRenderers.register(
                    ModBlockEntities.DRYING_RACK_BE.get(),
                    DryingRackBlockEntityRenderer::new
            ));
            event.enqueueWork(() -> BlockEntityRenderers.register(
                    ModBlockEntities.TANNING_RACK_BE.get(),
                    TanningRackBlockEntityRenderer::new
            ));
            event.enqueueWork(() -> BlockEntityRenderers.register(
                    ModBlockEntities.PRIMITIVE_CAMPFIRE_BE.get(),
                    PrimitiveCampfireBlockEntityRenderer::new
            ));
            event.enqueueWork(() -> BlockEntityRenderers.register(
                    ModBlockEntities.PIT_KILN_BE.get(),
                    PitKilnBlockEntityRenderer::new
            ));
            event.enqueueWork(() -> BlockEntityRenderers.register(
                    ModBlockEntities.CHOPPING_BLOCK_BE.get(),
                    ChoppingBlockBlockEntityRenderer::new
            ));
            event.enqueueWork(() -> MenuScreens.register(
                    ModMenuTypes.BARREL.get(),
                    BarrelScreen::new
            ));
        }
    }
}
