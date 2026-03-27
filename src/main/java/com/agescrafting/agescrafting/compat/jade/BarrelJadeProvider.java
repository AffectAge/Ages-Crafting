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

import java.util.Locale;

public enum BarrelJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "barrel_progress");
    private static final String TAG_PROGRESS = "RecipeProgress";
    private static final String TAG_TOTAL = "RecipeTotal";
    private static final String TAG_SEALED = "Sealed";
    private static final String TAG_SEASON_MULTIPLIER = "SeasonMultiplier";

    @Override
    public void appendServerData(CompoundTag data, @NotNull BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof BarrelBlockEntity barrel)) {
            return;
        }

        data.putBoolean(TAG_SEALED, barrel.isSealed());
        if (barrel.hasSeasonDurationModifier()) {
            data.putFloat(TAG_SEASON_MULTIPLIER, barrel.getSeasonDurationMultiplierForDisplay());
        }

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

        if (data.contains(TAG_SEASON_MULTIPLIER)) {
            float seasonMultiplier = data.getFloat(TAG_SEASON_MULTIPLIER);
            ChatFormatting color = seasonMultiplier < 1.0F ? ChatFormatting.GREEN : (seasonMultiplier > 1.0F ? ChatFormatting.RED : ChatFormatting.GRAY);
            tooltip.add(Component.translatable("tooltip.agescrafting.barrel.season_modifier", String.format(Locale.ROOT, "x%.2f", seasonMultiplier)).withStyle(color));
        }

        if (!data.contains(TAG_TOTAL)) {
            return;
        }

        int total = Math.max(1, data.getInt(TAG_TOTAL));
        int progress = Mth.clamp(data.getInt(TAG_PROGRESS), 0, total);
        float ratio = progress / (float) total;

        int progressSec = Mth.floor(progress / 20.0F);
        int totalSec = Mth.ceil(total / 20.0F);

        tooltip.add(Component.translatable("tooltip.agescrafting.barrel.progress", progressSec, totalSec).withStyle(ChatFormatting.GRAY));
        tooltip.add(IElementHelper.get().progress(ratio, Component.empty(), IElementHelper.get().progressStyle(), BoxStyle.DEFAULT, true));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}

