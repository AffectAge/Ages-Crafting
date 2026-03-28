package com.agescrafting.agescrafting.compat.jade;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.dryingrack.DryingRackBlockEntity;
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


public enum DryingRackJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "drying_rack_progress");
    private static final String TAG_HAS_ITEM = "HasItem";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_TOTAL = "Total";

    @Override
    public void appendServerData(CompoundTag data, @NotNull BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof DryingRackBlockEntity rack)) {
            return;
        }

        data.putBoolean(TAG_HAS_ITEM, !rack.isEmpty());
        if (rack.getTotalTicks() > 0) {
            data.putInt(TAG_PROGRESS, rack.getProgressTicks());
            data.putInt(TAG_TOTAL, rack.getTotalTicks());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBoolean(TAG_HAS_ITEM)) {
            tooltip.add(Component.translatable("tooltip.agescrafting.drying_rack.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (!data.contains(TAG_TOTAL)) {
            tooltip.add(Component.translatable("tooltip.agescrafting.drying_rack.waiting").withStyle(ChatFormatting.RED));
            return;
        }

        int total = Math.max(1, data.getInt(TAG_TOTAL));
        int progress = Mth.clamp(data.getInt(TAG_PROGRESS), 0, total);
        float ratio = progress / (float) total;

        tooltip.add(Component.translatable("tooltip.agescrafting.time_remaining", JadeTimeFormat.formatRemainingTicks(progress, total)).withStyle(ChatFormatting.GRAY));
        tooltip.add(IElementHelper.get().progress(ratio, Component.empty(), IElementHelper.get().progressStyle(), BoxStyle.DEFAULT, true));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}


