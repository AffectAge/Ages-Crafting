package com.agescrafting.agescrafting.choppingblock;

import com.agescrafting.agescrafting.registry.ModBlockEntities;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChoppingBlockBlockEntity extends BlockEntity {
    private static final String TAG_ITEM = "item";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_ACTIVE_RECIPE = "activeRecipe";

    private ItemStack storedItem = ItemStack.EMPTY;
    private int progress;
    private @Nullable ResourceLocation activeRecipeId;

    public ChoppingBlockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHOPPING_BLOCK_BE.get(), pos, state);
    }

    public ItemStack getStoredItem() {
        return storedItem.copy();
    }

    public boolean isEmpty() {
        return storedItem.isEmpty();
    }

    public int getProgress() {
        return Math.max(0, progress);
    }

    public int getRequiredChops() {
        ChoppingBlockRecipe recipe = getRecipeForStoredItem();
        return recipe == null ? 0 : recipe.chopsRequired();
    }

    public boolean insertOne(ItemStack stack) {
        if (level == null || stack.isEmpty() || !storedItem.isEmpty()) {
            return false;
        }

        if (!hasAnyRecipe(level, stack)) {
            return false;
        }

        ItemStack single = stack.copy();
        single.setCount(1);
        storedItem = single;
        progress = 0;
        activeRecipeId = null;
        setChanged();
        markForSync();
        return true;
    }

    public ItemStack extractStored() {
        if (storedItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack out = storedItem.copy();
        storedItem = ItemStack.EMPTY;
        progress = 0;
        activeRecipeId = null;
        setChanged();
        markForSync();
        return out;
    }

    public @Nullable ChoppingBlockRecipe getRecipeForTool(ItemStack toolStack) {
        ChoppingBlockRecipe recipe = getRecipeForStoredItem();
        if (recipe == null || !recipe.matchesTool(toolStack)) {
            return null;
        }
        return recipe;
    }

    public boolean advanceProgress() {
        ChoppingBlockRecipe recipe = getRecipeForStoredItem();
        if (recipe == null) {
            progress = 0;
            return false;
        }

        progress = Math.min(recipe.chopsRequired(), progress + 1);
        setChanged();
        markForSync();
        return progress >= recipe.chopsRequired();
    }

    public ItemStack consumeForResult() {
        ChoppingBlockRecipe recipe = getRecipeForStoredItem();
        if (recipe == null || storedItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        storedItem = ItemStack.EMPTY;
        progress = 0;
        activeRecipeId = null;
        setChanged();
        markForSync();
        return recipe.result();
    }

    public static boolean hasAnyRecipe(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return false;
        }

        Container container = new SimpleContainer(input.copy());
        for (ChoppingBlockRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CHOPPING_BLOCK.get())) {
            if (recipe.matches(container, level)) {
                return true;
            }
        }
        return false;
    }

    private @Nullable ChoppingBlockRecipe getRecipeForStoredItem() {
        if (level == null || storedItem.isEmpty()) {
            activeRecipeId = null;
            return null;
        }

        Container container = new SimpleContainer(storedItem.copy());

        if (activeRecipeId != null) {
            for (ChoppingBlockRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CHOPPING_BLOCK.get())) {
                if (recipe.getId().equals(activeRecipeId) && recipe.matches(container, level)) {
                    return recipe;
                }
            }
            activeRecipeId = null;
            progress = 0;
        }

        for (ChoppingBlockRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CHOPPING_BLOCK.get())) {
            if (recipe.matches(container, level)) {
                activeRecipeId = recipe.getId();
                return recipe;
            }
        }

        return null;
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
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (!storedItem.isEmpty()) {
            tag.put(TAG_ITEM, storedItem.save(new CompoundTag()));
        }
        tag.putInt(TAG_PROGRESS, progress);
        if (activeRecipeId != null) {
            tag.putString(TAG_ACTIVE_RECIPE, activeRecipeId.toString());
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        storedItem = tag.contains(TAG_ITEM) ? ItemStack.of(tag.getCompound(TAG_ITEM)) : ItemStack.EMPTY;
        progress = tag.getInt(TAG_PROGRESS);
        activeRecipeId = tag.contains(TAG_ACTIVE_RECIPE) ? ResourceLocation.tryParse(tag.getString(TAG_ACTIVE_RECIPE)) : null;
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
}
