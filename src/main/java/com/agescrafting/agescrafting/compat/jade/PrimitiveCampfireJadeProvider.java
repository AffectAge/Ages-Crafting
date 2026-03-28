package com.agescrafting.agescrafting.compat.jade;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.campfire.PrimitiveCampfireBlock;
import com.agescrafting.agescrafting.campfire.PrimitiveCampfireBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public enum PrimitiveCampfireJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "primitive_campfire");
    private static final String TAG_ACTIVE = "Active";
    private static final String TAG_FUEL = "Fuel";
    private static final String TAG_ASH = "Ash";
    private static final String TAG_HAS_ITEM = "HasItem";
    private static final String TAG_COOKED = "Cooked";
    private static final String TAG_OVERCOOKED = "Overcooked";
    private static final String TAG_COOK_PROGRESS = "CookProgress";
    private static final String TAG_COOK_TOTAL = "CookTotal";
    private static final String TAG_OVERCOOK_PROGRESS = "OvercookProgress";
    private static final String TAG_OVERCOOK_TOTAL = "OvercookTotal";
    private static final String TAG_BURN_REMAINING = "BurnRemaining";
    private static final String TAG_BURN_TOTAL = "BurnTotal";

    @Override
    public void appendServerData(CompoundTag data, @NotNull BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof PrimitiveCampfireBlockEntity campfire)) {
            return;
        }

        data.putBoolean(TAG_ACTIVE, accessor.getBlockState().getValue(PrimitiveCampfireBlock.VARIANT)
                == PrimitiveCampfireBlockEntity.Variant.LIT);
        data.putInt(TAG_FUEL, campfire.getFuelCount());
        data.putInt(TAG_ASH, campfire.getAshLevel());
        data.putInt(TAG_BURN_REMAINING, campfire.getBurnTimeRemaining());
        data.putInt(TAG_BURN_TOTAL, campfire.getBurnTimePerLogTicks());

        data.putBoolean(TAG_HAS_ITEM, campfire.hasCookItem());
        data.putBoolean(TAG_COOKED, campfire.isCooked());
        data.putBoolean(TAG_OVERCOOKED, campfire.isOvercooked());
        data.putInt(TAG_COOK_PROGRESS, campfire.getCookProgress());
        data.putInt(TAG_COOK_TOTAL, campfire.getCookTotal());
        data.putInt(TAG_OVERCOOK_PROGRESS, campfire.getOvercookProgress());
        data.putInt(TAG_OVERCOOK_TOTAL, 200);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        boolean active = data.getBoolean(TAG_ACTIVE);
        tooltip.add(Component.translatable(active
                        ? "tooltip.agescrafting.primitive_campfire.state.lit"
                        : "tooltip.agescrafting.primitive_campfire.state.unlit")
                .withStyle(active ? ChatFormatting.RED : ChatFormatting.GRAY));

        int fuel = Math.max(0, data.getInt(TAG_FUEL));
        int ash = Math.max(0, data.getInt(TAG_ASH));

        tooltip.add(Component.translatable("tooltip.agescrafting.primitive_campfire.fuel", fuel, PrimitiveCampfireBlockEntity.MAX_FUEL));
        tooltip.add(Component.translatable("tooltip.agescrafting.primitive_campfire.ash", ash, PrimitiveCampfireBlockEntity.MAX_ASH_LEVEL));

        int burnRemaining = Math.max(0, data.getInt(TAG_BURN_REMAINING));
        if (active && burnRemaining > 0) {
            int burnTotal = Math.max(1, data.getInt(TAG_BURN_TOTAL));
            int burnProgress = Mth.clamp(burnTotal - burnRemaining, 0, burnTotal);
            float burnRatio = burnProgress / (float) burnTotal;
            tooltip.add(Component.translatable("tooltip.agescrafting.primitive_campfire.log_burn_remaining", JadeTimeFormat.formatRemainingTicks(burnProgress, burnTotal)).withStyle(ChatFormatting.GRAY));
            tooltip.add(IElementHelper.get().progress(burnRatio, Component.empty(), IElementHelper.get().progressStyle(), BoxStyle.DEFAULT, true));
        }

        if (!data.getBoolean(TAG_HAS_ITEM)) {
            tooltip.add(Component.translatable("tooltip.agescrafting.primitive_campfire.empty"));
            return;
        }

        if (data.getBoolean(TAG_OVERCOOKED)) {
            tooltip.add(Component.translatable("tooltip.agescrafting.primitive_campfire.overcooked").withStyle(ChatFormatting.DARK_RED));
            return;
        }

        if (data.getBoolean(TAG_COOKED)) {
            int total = Math.max(1, data.getInt(TAG_OVERCOOK_TOTAL));
            int progress = Mth.clamp(data.getInt(TAG_OVERCOOK_PROGRESS), 0, total);
            float ratio = progress / (float) total;
            tooltip.add(Component.translatable("tooltip.agescrafting.primitive_campfire.overcook_remaining", JadeTimeFormat.formatRemainingTicks(progress, total)).withStyle(ChatFormatting.GRAY));
            tooltip.add(IElementHelper.get().progress(ratio, Component.empty(), IElementHelper.get().progressStyle(), BoxStyle.DEFAULT, true));
            return;
        }

        int total = Math.max(1, data.getInt(TAG_COOK_TOTAL));
        int progress = Mth.clamp(data.getInt(TAG_COOK_PROGRESS), 0, total);
        float ratio = progress / (float) total;
        tooltip.add(Component.translatable("tooltip.agescrafting.primitive_campfire.cook_remaining", JadeTimeFormat.formatRemainingTicks(progress, total)).withStyle(ChatFormatting.GRAY));
        tooltip.add(IElementHelper.get().progress(ratio, Component.empty(), IElementHelper.get().progressStyle(), BoxStyle.DEFAULT, true));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}