package com.agescrafting.agescrafting.workspace;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WorkspaceRecipeInput implements Container {
    private final ItemStack[] items;

    public WorkspaceRecipeInput(List<ItemStack> source) {
        this.items = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            this.items[i] = source.get(i).copy();
        }
    }

    @Override
    public int getContainerSize() {
        return items.length;
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
        return items[slot];
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
        items[slot] = stack;
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
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }
}
