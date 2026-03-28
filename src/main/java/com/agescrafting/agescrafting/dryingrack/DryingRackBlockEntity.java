package com.agescrafting.agescrafting.dryingrack;

import com.agescrafting.agescrafting.registry.ModBlockEntities;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import com.agescrafting.agescrafting.sound.DeviceRecipeSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DryingRackBlockEntity extends BlockEntity {
    private static final String TAG_ITEM = "item";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_TOTAL = "total";
    private static final String TAG_ACTIVE_RECIPE = "activeRecipe";

    private ItemStack storedItem = ItemStack.EMPTY;
    private int progressTicks;
    private int totalTicks;
    private @Nullable ResourceLocation activeRecipeId;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity blockEntity) {
        blockEntity.tickServer();
    }

    public ItemStack getStoredItem() {
        return storedItem.copy();
    }

    public boolean isEmpty() {
        return storedItem.isEmpty();
    }

    public int getProgressTicks() {
        return Math.max(0, progressTicks);
    }

    public int getTotalTicks() {
        return Math.max(0, totalTicks);
    }

    public boolean hasActiveRecipe() {
        return totalTicks > 0;
    }

    public boolean insertOne(ItemStack stack) {
        if (stack.isEmpty() || !storedItem.isEmpty()) {
            return false;
        }

        ItemStack single = stack.copy();
        single.setCount(1);
        storedItem = single;
        resetProgress();
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
        resetProgress();
        setChanged();
        markForSync();
        return out;
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (storedItem.isEmpty()) {
            if (progressTicks != 0 || totalTicks != 0 || activeRecipeId != null) {
                resetProgress();
            }
            return;
        }

        DryingRackRecipe recipe = resolveRecipe();
        if (recipe == null) {
            progressTicks = 0;
            totalTicks = 0;
            return;
        }

        totalTicks = Math.max(1, recipe.durationTicks());
        progressTicks++;

        if (progressTicks < totalTicks) {
            return;
        }

        storedItem = recipe.result();
        resetProgress();
        setChanged();
        markForSync();
        playRecipeCompleteFx();
    }

    private void playRecipeCompleteFx() {
        if (level == null) {
            return;
        }

        DeviceRecipeSounds.playFinish(level, worldPosition);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.8D,
                    worldPosition.getZ() + 0.5D,
                    6,
                    0.22D,
                    0.1D,
                    0.22D,
                    0.0D);
        }
    }

    private @Nullable DryingRackRecipe resolveRecipe() {
        if (level == null || storedItem.isEmpty()) {
            activeRecipeId = null;
            return null;
        }

        Container input = new SimpleContainer(storedItem.copy());

        if (activeRecipeId != null) {
            for (DryingRackRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DRYING_RACK.get())) {
                if (recipe.getId().equals(activeRecipeId) && recipe.matches(input, level)) {
                    return recipe;
                }
            }
            activeRecipeId = null;
            progressTicks = 0;
        }

        for (DryingRackRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DRYING_RACK.get())) {
            if (recipe.matches(input, level)) {
                activeRecipeId = recipe.getId();
                return recipe;
            }
        }

        return null;
    }

    private void resetProgress() {
        progressTicks = 0;
        totalTicks = 0;
        activeRecipeId = null;
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
        tag.putInt(TAG_PROGRESS, progressTicks);
        tag.putInt(TAG_TOTAL, totalTicks);
        if (activeRecipeId != null) {
            tag.putString(TAG_ACTIVE_RECIPE, activeRecipeId.toString());
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        storedItem = tag.contains(TAG_ITEM) ? ItemStack.of(tag.getCompound(TAG_ITEM)) : ItemStack.EMPTY;
        progressTicks = tag.getInt(TAG_PROGRESS);
        totalTicks = tag.getInt(TAG_TOTAL);
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


