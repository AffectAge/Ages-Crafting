package com.agescrafting.agescrafting.compat.jade;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.tanningrack.TanningRackBlockEntity;
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

public enum TanningRackJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "tanning_rack_progress");
    private static final String TAG_HAS_ITEM = "HasItem";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_TOTAL = "Total";
    private static final String TAG_STAGE = "Stage";

    @Override
    public void appendServerData(CompoundTag data, @NotNull BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof TanningRackBlockEntity rack)) {
            return;
        }

        data.putBoolean(TAG_HAS_ITEM, !rack.isEmpty());
        data.putString(TAG_STAGE, rack.getStageDisplay().text());
        int total = rack.getRequiredHits();
        if (total > 0) {
            data.putInt(TAG_TOTAL, total);
            data.putInt(TAG_PROGRESS, rack.getProgress());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBoolean(TAG_HAS_ITEM)) {
            tooltip.add(Component.translatable("tooltip.agescrafting.tanning_rack.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip.agescrafting.tanning_rack.stage", data.getString(TAG_STAGE)).withStyle(ChatFormatting.GRAY));

        if (!data.contains(TAG_TOTAL)) {
            tooltip.add(Component.translatable("tooltip.agescrafting.tanning_rack.no_recipe").withStyle(ChatFormatting.RED));
            return;
        }

        int total = Math.max(1, data.getInt(TAG_TOTAL));
        int progress = Mth.clamp(data.getInt(TAG_PROGRESS), 0, total);
        float ratio = progress / (float) total;
        int percent = Math.round(ratio * 100.0F);

        tooltip.add(Component.translatable("tooltip.agescrafting.tanning_rack.progress_percent", percent).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.agescrafting.tanning_rack.progress", progress, total).withStyle(ChatFormatting.GRAY));
        tooltip.add(IElementHelper.get().progress(ratio, Component.empty(), IElementHelper.get().progressStyle(), BoxStyle.DEFAULT, true));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
