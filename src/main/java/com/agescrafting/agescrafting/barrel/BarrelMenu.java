package com.agescrafting.agescrafting.barrel;

import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BarrelMenu extends AbstractContainerMenu {
    private static final int BARREL_SLOT_COUNT = BarrelBlockEntity.TOTAL_SLOTS;
    private static final int PLAYER_SLOT_COUNT = 36;
    private static final int PLAYER_HOTBAR_COUNT = 9;
    private static final int PLAYER_FIRST_SLOT = BARREL_SLOT_COUNT;

    private final BarrelBlockEntity blockEntity;
    private final ContainerData data;

    public BarrelMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, buffer.readBlockPos()));
    }

    public BarrelMenu(int containerId, Inventory playerInventory, BarrelBlockEntity blockEntity) {
        super(ModMenuTypes.BARREL.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getMenuData();

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), BarrelBlockEntity.CONTAINER_SLOT, 15, 18) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return !isSealed() && stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
            }

            @Override
            public boolean mayPickup(Player player) {
                return !isSealed();
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int slotIndex = BarrelBlockEntity.ITEM_GRID_START + row * 3 + column;
                int x = 57 + column * 18;
                int y = 18 + row * 18;
                addSlot(new SlotItemHandler(blockEntity.getItemHandler(), slotIndex, x, y) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return !isSealed();
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return !isSealed();
                    }
                });
            }
        }

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), BarrelBlockEntity.OUTPUT_ITEM_START, 145, 18) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !isSealed();
            }
        });

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), BarrelBlockEntity.OUTPUT_ITEM_START + 1, 145, 39) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !isSealed();
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 96 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            addSlot(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 154));
        }

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return true;
        }

        BlockPos pos = blockEntity.getBlockPos();
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity)) {
            return false;
        }

        boolean isBarrelBlock = ModBlocks.BARREL_BLOCKS.stream().anyMatch(b -> level.getBlockState(pos).is(b.get()));
        if (!isBarrelBlock) {
            return false;
        }

        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack quickMovedCopy = ItemStack.EMPTY;
        Slot quickMovedSlot = slots.get(index);

        if (quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedCopy = rawStack.copy();

            if (index < PLAYER_FIRST_SLOT) {
                if (!moveItemStackTo(rawStack, PLAYER_FIRST_SLOT, PLAYER_FIRST_SLOT + PLAYER_SLOT_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean isFluidContainer = rawStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
                if (isFluidContainer) {
                    if (!moveItemStackTo(rawStack, BarrelBlockEntity.CONTAINER_SLOT, BarrelBlockEntity.CONTAINER_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(rawStack, BarrelBlockEntity.ITEM_GRID_START, BarrelBlockEntity.ITEM_GRID_START + BarrelBlockEntity.ITEM_GRID_COUNT, false)) {
                    if (index < PLAYER_FIRST_SLOT + (PLAYER_SLOT_COUNT - PLAYER_HOTBAR_COUNT)) {
                        if (!moveItemStackTo(rawStack, PLAYER_FIRST_SLOT + (PLAYER_SLOT_COUNT - PLAYER_HOTBAR_COUNT), PLAYER_FIRST_SLOT + PLAYER_SLOT_COUNT, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(rawStack, PLAYER_FIRST_SLOT, PLAYER_FIRST_SLOT + (PLAYER_SLOT_COUNT - PLAYER_HOTBAR_COUNT), false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (rawStack.isEmpty()) {
                quickMovedSlot.set(ItemStack.EMPTY);
            } else {
                quickMovedSlot.setChanged();
            }
        }

        return quickMovedCopy;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (id == 0) {
            blockEntity.toggleSealed();
            return true;
        }
        if (id == 1) {
            return !isSealed() && blockEntity.clearInputFluids();
        }
        if (id == 2) {
            return !isSealed() && blockEntity.clearOutputFluids();
        }
        return super.clickMenuButton(player, id);
    }

    public boolean isSealed() {
        return data.get(4) == 1;
    }

    public int getInputFluidAmount() {
        return data.get(0);
    }

    public int getInputTankCapacity() {
        return Math.max(1, data.get(1));
    }

    public int getOutputFluidAmount() {
        return data.get(2);
    }

    public int getOutputTankCapacity() {
        return Math.max(1, data.get(3));
    }

    public FluidStack getInputFluid() {
        return blockEntity.getInputFluid();
    }

    public FluidStack getOutputFluid() {
        return blockEntity.getOutputFluid();
    }

    private static BarrelBlockEntity getBlockEntity(Inventory playerInventory, BlockPos blockPos) {
        if (playerInventory.player.level().getBlockEntity(blockPos) instanceof BarrelBlockEntity barrelBlockEntity) {
            return barrelBlockEntity;
        }
        throw new IllegalStateException("Barrel block entity is missing at " + blockPos);
    }
}

