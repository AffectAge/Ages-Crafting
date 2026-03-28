package com.agescrafting.agescrafting.barrel;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BarrelBlockItem extends BlockItem {
    private static final String TAG_ITEMS = "items";
    private static final String TAG_INPUT_TANK = "inputTank";
    private static final String TAG_OUTPUT_TANK = "outputTank";

    public BarrelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag blockEntityTag = BlockItem.getBlockEntityData(stack);
        if (blockEntityTag == null && stack.hasTag() && stack.getTag().contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            blockEntityTag = stack.getTag().getCompound("BlockEntityTag");
        }

        boolean sealed = (stack.hasTag() && stack.getTag().getBoolean(BarrelBlock.SEALED_STACK_FLAG))
                || (blockEntityTag != null && blockEntityTag.getBoolean("sealed"));

        if (!sealed || blockEntityTag == null) {
            return;
        }

        int itemCount = readStoredItemCount(blockEntityTag);
        int fluidMb = readStoredFluidAmount(blockEntityTag);

        tooltip.add(Component.translatable("tooltip.agescrafting.barrel.contains_compact", fluidMb, itemCount)
                .withStyle(ChatFormatting.GRAY));
    }

    private static int readStoredItemCount(CompoundTag blockEntityTag) {
        if (!blockEntityTag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            return 0;
        }

        ItemStackHandler handler = new ItemStackHandler(BarrelBlockEntity.TOTAL_SLOTS);
        handler.deserializeNBT(blockEntityTag.getCompound(TAG_ITEMS));

        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            count += handler.getStackInSlot(i).getCount();
        }
        return count;
    }

    private static int readStoredFluidAmount(CompoundTag blockEntityTag) {
        int amount = 0;

        for (int i = 0; i < 1; i++) {
            String key = TAG_INPUT_TANK + i;
            if (blockEntityTag.contains(key, Tag.TAG_COMPOUND)) {
                amount += FluidStack.loadFluidStackFromNBT(blockEntityTag.getCompound(key)).getAmount();
            }
        }

        for (int i = 0; i < 1; i++) {
            String key = TAG_OUTPUT_TANK + i;
            if (blockEntityTag.contains(key, Tag.TAG_COMPOUND)) {
                amount += FluidStack.loadFluidStackFromNBT(blockEntityTag.getCompound(key)).getAmount();
            }
        }

        if (amount == 0 && blockEntityTag.contains("tank", Tag.TAG_COMPOUND)) {
            amount += FluidStack.loadFluidStackFromNBT(blockEntityTag.getCompound("tank")).getAmount();
        }

        return amount;
    }
}
