package com.agescrafting.agescrafting.barrel.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class BarrelRecipeInput implements Container {
    private final List<ItemStack> items;
    private final List<FluidStack> fluids;

    public BarrelRecipeInput(List<ItemStack> sourceItems, List<FluidStack> sourceFluids) {
        this.items = new ArrayList<>(sourceItems.size());
        for (ItemStack stack : sourceItems) {
            this.items.add(stack.copy());
        }

        this.fluids = new ArrayList<>(sourceFluids.size());
        for (FluidStack fluid : sourceFluids) {
            this.fluids.add(fluid.copy());
        }
    }

    public List<ItemStack> items() {
        return items;
    }

    public List<FluidStack> fluids() {
        List<FluidStack> copy = new ArrayList<>(fluids.size());
        for (FluidStack fluid : fluids) {
            copy.add(fluid.copy());
        }
        return copy;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }
}
