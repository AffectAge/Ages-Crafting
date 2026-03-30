package com.agescrafting.agescrafting.compat.jade;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.barrel.BarrelBlockEntity;
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

public enum BarrelJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "barrel_progress");
    private static final String TAG_PROGRESS = "RecipeProgress";
    private static final String TAG_TOTAL = "RecipeTotal";
    private static final String TAG_SEALED = "Sealed";

    @Override
    public void appendServerData(CompoundTag data, @NotNull BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof BarrelBlockEntity barrel)) {
            return;
        }

        data.putBoolean(TAG_SEALED, barrel.isSealed());
        int total = barrel.getRecipeTotalTicks();
        if (total <= 0) {
            return;
        }

        data.putInt(TAG_TOTAL, total);
        data.putInt(TAG_PROGRESS, Math.max(0, barrel.getRecipeProgressTicks()));
    }

    @Override
    public void appendTooltip(ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        boolean sealed = data.getBoolean(TAG_SEALED);
        Component stateText = Component.translatable(sealed
                ? "tooltip.agescrafting.barrel.state.sealed"
                : "tooltip.agescrafting.barrel.state.unsealed")
                .withStyle(sealed ? ChatFormatting.RED : ChatFormatting.GREEN);
        tooltip.add(stateText);

        if (!data.contains(TAG_TOTAL)) {
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
