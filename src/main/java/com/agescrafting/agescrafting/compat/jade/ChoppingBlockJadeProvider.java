package com.agescrafting.agescrafting.compat.jade;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.choppingblock.ChoppingBlockBlock;
import com.agescrafting.agescrafting.choppingblock.ChoppingBlockBlockEntity;
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

public enum ChoppingBlockJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "chopping_block");
    private static final String TAG_HAS_ITEM = "HasItem";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_TOTAL = "Total";
    private static final String TAG_CHIPS = "NearbyChips";

    @Override
    public void appendServerData(CompoundTag data, @NotNull BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof ChoppingBlockBlockEntity chopping)) {
            return;
        }

        data.putBoolean(TAG_HAS_ITEM, !chopping.isEmpty());
        data.putInt(TAG_CHIPS, ChoppingBlockBlock.getNearbyChipLayers(accessor.getLevel(), accessor.getPosition()));

        int total = chopping.getRequiredChops();
        if (total > 0) {
            data.putInt(TAG_TOTAL, total);
            data.putInt(TAG_PROGRESS, chopping.getProgress());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        int chips = data.getInt(TAG_CHIPS);

        if (!data.getBoolean(TAG_HAS_ITEM)) {
            tooltip.add(Component.translatable("tooltip.agescrafting.chopping_block.empty"));
            if (chips > 0) {
                tooltip.add(Component.translatable("tooltip.agescrafting.chopping_block.sawdust", chips));
            }
            return;
        }

        tooltip.add(Component.translatable("tooltip.agescrafting.chopping_block.sawdust", chips));

        if (!data.contains(TAG_TOTAL)) {
            tooltip.add(Component.translatable("tooltip.agescrafting.chopping_block.no_recipe"));
            return;
        }

        int total = Math.max(1, data.getInt(TAG_TOTAL));
        int progress = Mth.clamp(data.getInt(TAG_PROGRESS), 0, total);
        float ratio = progress / (float) total;

        tooltip.add(Component.translatable("tooltip.agescrafting.chopping_block.progress", progress, total));
        tooltip.add(IElementHelper.get().progress(ratio, Component.empty(), IElementHelper.get().progressStyle(), BoxStyle.DEFAULT, true));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
