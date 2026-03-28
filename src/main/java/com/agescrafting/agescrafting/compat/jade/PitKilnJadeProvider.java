package com.agescrafting.agescrafting.compat.jade;

import com.agescrafting.agescrafting.AgesCraftingMod;
import com.agescrafting.agescrafting.pitkiln.PitKilnBlock;
import com.agescrafting.agescrafting.pitkiln.PitKilnBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;


public enum PitKilnJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AgesCraftingMod.MODID, "pit_kiln");
    private static final String TAG_VARIANT = "Variant";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_TOTAL = "Total";
    private static final String TAG_ASH = "Ash";
    private static final String TAG_INPUT = "Input";

    @Override
    public void appendServerData(CompoundTag data, @NotNull BlockAccessor accessor) {
        PitKilnBlockEntity kiln = resolveKiln(accessor);
        if (kiln == null) {
            return;
        }

        data.putString(TAG_VARIANT, kiln.getBlockState().getValue(PitKilnBlock.VARIANT).getSerializedName());
        data.putInt(TAG_ASH, kiln.getAshLevel());

        ItemStack inputStack = kiln.getInputStack();
        if (!inputStack.isEmpty()) {
            data.put(TAG_INPUT, inputStack.save(new CompoundTag()));
        }

        if (kiln.getTotalTicks() > 0) {
            data.putInt(TAG_PROGRESS, kiln.getProgressTicks());
            data.putInt(TAG_TOTAL, kiln.getTotalTicks());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(TAG_VARIANT)) {
            ItemStack input = data.contains(TAG_INPUT) ? ItemStack.of(data.getCompound(TAG_INPUT)) : ItemStack.EMPTY;
            addFromData(tooltip, data.getString(TAG_VARIANT), data.getInt(TAG_PROGRESS), data.getInt(TAG_TOTAL), data.getInt(TAG_ASH), input);
            return;
        }

        // Fallback for edge cases where server data is unavailable on first frame.
        PitKilnBlockEntity kiln = resolveKiln(accessor);
        if (kiln == null) {
            return;
        }

        String variant = kiln.getBlockState().getValue(PitKilnBlock.VARIANT).getSerializedName();
        addFromData(tooltip, variant, kiln.getProgressTicks(), kiln.getTotalTicks(), kiln.getAshLevel(), kiln.getInputStack());
    }

    private static PitKilnBlockEntity resolveKiln(@NotNull BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof PitKilnBlockEntity kiln) {
            return kiln;
        }

        if (accessor.getBlockState().getBlock() instanceof BaseFireBlock
                && accessor.getLevel().getBlockState(accessor.getPosition().below()).getBlock() instanceof PitKilnBlock
                && accessor.getLevel().getBlockEntity(accessor.getPosition().below()) instanceof PitKilnBlockEntity kiln) {
            return kiln;
        }

        return null;
    }

    private static void addFromData(ITooltip tooltip, String variant, int progress, int total, int ash, ItemStack input) {
        if (variant == null || variant.isEmpty()) {
            return;
        }

        tooltip.add(Component.translatable("tooltip.agescrafting.pit_kiln.state." + variant).withStyle(ChatFormatting.GRAY));
        if (ash > 0) {
            tooltip.add(Component.translatable("tooltip.agescrafting.primitive_campfire.ash", ash, PitKilnBlockEntity.MAX_ASH_LEVEL).withStyle(ChatFormatting.GRAY));
        }

        if (!input.isEmpty()) {
            tooltip.add(IElementHelper.get().item(input));
            tooltip.add(Component.translatable("tooltip.agescrafting.pit_kiln.input", input.getCount(), input.getHoverName()).withStyle(ChatFormatting.GRAY));
        }

        if (total <= 0) {
            return;
        }

        int clampedTotal = Math.max(1, total);
        int clampedProgress = Mth.clamp(progress, 0, clampedTotal);
        float ratio = clampedProgress / (float) clampedTotal;
        tooltip.add(Component.translatable("tooltip.agescrafting.time_remaining", JadeTimeFormat.formatRemainingTicks(clampedProgress, clampedTotal)).withStyle(ChatFormatting.GRAY));
        tooltip.add(IElementHelper.get().progress(ratio, Component.empty(), IElementHelper.get().progressStyle(), BoxStyle.DEFAULT, true));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}

