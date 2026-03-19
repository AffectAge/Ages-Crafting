package com.agescrafting.agescrafting.barrel;

import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.agescrafting.agescrafting.registry.ModBlockEntities;
import com.agescrafting.agescrafting.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BarrelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CONTAINER_SLOT = 0;
    public static final int ITEM_GRID_START = 1;
    public static final int ITEM_GRID_COUNT = 9;
    public static final int TOTAL_SLOTS = ITEM_GRID_START + ITEM_GRID_COUNT;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_TANK = "tank";
    private static final String TAG_SEALED = "sealed";

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == CONTAINER_SLOT && !suppressContainerSlotHooks) {
                processContainerSlot = true;
            }
            setChanged();
        }
    };

    private final FluidTank tank = new FluidTank(getConfiguredCapacity()) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            markForSync();
        }
    };

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> tank.getFluidAmount();
                case 1 -> tank.getCapacity();
                case 2 -> sealed ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Server authoritative data.
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> tank);
    private boolean sealed;
    private boolean processContainerSlot;
    private boolean suppressContainerSlotHooks;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARREL_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BarrelBlockEntity blockEntity) {
        if (blockEntity.processContainerSlot) {
            blockEntity.processContainerSlot = false;
            blockEntity.tryProcessContainerSlot();
        }
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ContainerData getMenuData() {
        return menuData;
    }

    public FluidStack getFluid() {
        return tank.getFluid().copy();
    }

    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    public int getTankCapacity() {
        return tank.getCapacity();
    }

    public boolean isSealed() {
        return sealed;
    }

    public boolean toggleSealed() {
        sealed = !sealed;
        setChanged();
        markForSync();
        return sealed;
    }

    public ItemStack createSealedDropStack() {
        ItemStack stack = new ItemStack(ModItems.BARREL_ITEM.get());
        CompoundTag blockEntityTag = new CompoundTag();
        saveAdditional(blockEntityTag);
        BlockItem.setBlockEntityData(stack, getType(), blockEntityTag);
        return stack;
    }

    public List<ItemStack> getUnsealedDrops() {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(ModItems.BARREL_ITEM.get()));
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        return drops;
    }

    private void tryProcessContainerSlot() {
        if (sealed) {
            return;
        }

        ItemStack slotStack = itemHandler.getStackInSlot(CONTAINER_SLOT);
        if (slotStack.isEmpty()) {
            return;
        }

        ItemStack singleItem = slotStack.copyWithCount(1);

        var emptyResult = FluidUtil.tryEmptyContainer(singleItem, tank, Integer.MAX_VALUE, null, true);
        if (emptyResult.isSuccess()) {
            applyContainerResult(emptyResult.getResult());
            return;
        }

        var fillResult = FluidUtil.tryFillContainer(singleItem, tank, Integer.MAX_VALUE, null, true);
        if (fillResult.isSuccess()) {
            applyContainerResult(fillResult.getResult());
        }
    }

    private void applyContainerResult(ItemStack resultContainer) {
        suppressContainerSlotHooks = true;
        itemHandler.extractItem(CONTAINER_SLOT, 1, false);
        ItemStack remainder = itemHandler.insertItem(CONTAINER_SLOT, resultContainer, false);
        suppressContainerSlotHooks = false;
        if (!remainder.isEmpty() && level != null) {
            Block.popResource(level, worldPosition, remainder);
        }
        markForSync();
    }

    private void markForSync() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        level.blockEntityChanged(worldPosition);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.agescrafting.barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new BarrelMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ITEMS, itemHandler.serializeNBT());
        tag.put(TAG_TANK, tank.writeToNBT(new CompoundTag()));
        tag.putBoolean(TAG_SEALED, sealed);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound(TAG_ITEMS));
        tank.readFromNBT(tag.getCompound(TAG_TANK));
        sealed = tag.getBoolean(TAG_SEALED);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
        fluidCapability.invalidate();
    }

    private static int getConfiguredCapacity() {
        return Math.max(1000, AgesCraftingConfig.SERVER.barrelTankCapacityMb.get());
    }
}
