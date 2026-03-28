package com.agescrafting.agescrafting.pitkiln;

import com.agescrafting.agescrafting.config.AgesCraftingConfig;
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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PitKilnBlockEntity extends BlockEntity {
    public static final int MAX_ASH_LEVEL = 8;

    private static final String TAG_INPUT = "input";
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_LOGS = "logs";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_TOTAL = "total";
    private static final String TAG_ACTIVE = "active";
    private static final String TAG_RECIPE = "recipe";
    private static final String TAG_RAIN = "rain";
    private static final String TAG_ASH = "ash";

    private final ItemStackHandler outputStacks = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }
    };

    private final ItemStackHandler logStacks = new ItemStackHandler(6) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }
    };

    private ItemStack inputStack = ItemStack.EMPTY;
    private boolean active;
    private int progressTicks;
    private int totalTicks;
    private int rainTimeRemaining;
    private int ashLevel;
    private @Nullable ResourceLocation activeRecipeId;
    private boolean suppressItemDropOnRemove;

    public PitKilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIT_KILN_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PitKilnBlockEntity blockEntity) {
        blockEntity.tickServer(level);
    }

    public ItemStack getInputStack() {
        return inputStack.copy();
    }

    public int getInputCount() {
        return inputStack.isEmpty() ? 0 : inputStack.getCount();
    }

    public int getLogCount() {
        int count = 0;
        for (int i = 0; i < logStacks.getSlots(); i++) {
            if (!logStacks.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public ItemStack getLogStack(int slot) {
        if (slot < 0 || slot >= logStacks.getSlots()) {
            return ItemStack.EMPTY;
        }
        return logStacks.getStackInSlot(slot);
    }

    public boolean isActive() {
        return active;
    }

    public int getProgressTicks() {
        return Math.max(0, progressTicks);
    }

    public int getTotalTicks() {
        return Math.max(0, totalTicks);
    }

    public int getAshLevel() {
        return ashLevel;
    }

    public void reduceAsh(int amount) {
        ashLevel = Math.max(0, ashLevel - Math.max(1, amount));
        if (!active && ashLevel == 0) {
            setVariant(hasOutput() ? PitKilnBlock.Variant.RESULT : PitKilnBlock.Variant.EMPTY);
        } else {
            setVariant(active ? PitKilnBlock.Variant.ACTIVE : PitKilnBlock.Variant.COMPLETE);
        }
        setChanged();
        sync();
    }

    public boolean hasOutput() {
        for (int i = 0; i < outputStacks.getSlots(); i++) {
            if (!outputStacks.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getFirstOutputStack() {
        for (int i = 0; i < outputStacks.getSlots(); i++) {
            ItemStack stack = outputStacks.getStackInSlot(i);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public void markRemoveWithoutItemDrop() {
        suppressItemDropOnRemove = true;
    }

    public boolean consumeRemoveWithoutItemDrop() {
        boolean value = suppressItemDropOnRemove;
        suppressItemDropOnRemove = false;
        return value;
    }

    public int insertInput(ItemStack heldStack) {
        if (level == null || heldStack.isEmpty() || active) {
            return 0;
        }

        ItemStack single = heldStack.copy();
        single.setCount(1);
        if (resolveRecipe(single) == null) {
            return 0;
        }

        int max = Math.max(1, AgesCraftingConfig.SERVER.pitKilnMaxStackSize.get());
        if (inputStack.isEmpty()) {
            int inserted = Math.min(max, heldStack.getCount());
            inputStack = heldStack.copy();
            inputStack.setCount(inserted);
            setChanged();
            sync();
            return inserted;
        }

        if (!ItemStack.isSameItemSameTags(inputStack, heldStack) || inputStack.getCount() >= max) {
            return 0;
        }

        int canInsert = Math.min(max - inputStack.getCount(), heldStack.getCount());
        if (canInsert <= 0) {
            return 0;
        }

        inputStack.grow(canInsert);
        setChanged();
        sync();
        return canInsert;
    }

    public ItemStack extractInput() {
        if (inputStack.isEmpty() || active) {
            return ItemStack.EMPTY;
        }

        ItemStack out = inputStack.copy();
        inputStack = ItemStack.EMPTY;
        resetProgress();
        setChanged();
        sync();
        return out;
    }

    public boolean addLog(ItemStack heldStack) {
        if (heldStack.isEmpty() || active) {
            return false;
        }

        for (int i = 0; i < logStacks.getSlots(); i++) {
            if (logStacks.getStackInSlot(i).isEmpty()) {
                ItemStack one = heldStack.copy();
                one.setCount(1);
                logStacks.setStackInSlot(i, one);
                setChanged();
                sync();
                return true;
            }
        }

        return false;
    }

    public ItemStack removeLog() {
        if (active) {
            return ItemStack.EMPTY;
        }

        for (int i = logStacks.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = logStacks.getStackInSlot(i);
            if (!stack.isEmpty()) {
                logStacks.setStackInSlot(i, ItemStack.EMPTY);
                setChanged();
                sync();
                return stack.copy();
            }
        }

        return ItemStack.EMPTY;
    }

    public ItemStack extractOutput() {
        for (int i = 0; i < outputStacks.getSlots(); i++) {
            ItemStack stack = outputStacks.getStackInSlot(i);
            if (!stack.isEmpty()) {
                outputStacks.setStackInSlot(i, ItemStack.EMPTY);
                setChanged();
                sync();
                return stack.copy();
            }
        }

        return ItemStack.EMPTY;
    }

    public boolean ignite() {
        if (level == null || active || inputStack.isEmpty()) {
            return false;
        }

        PitKilnRecipe recipe = resolveRecipe(inputStack);
        if (recipe == null) {
            return false;
        }

        active = true;
        progressTicks = 0;
        totalTicks = computeTotalTicks(recipe, inputStack.getCount());
        rainTimeRemaining = Math.max(1, AgesCraftingConfig.SERVER.pitKilnRainExtinguishTicks.get());
        activeRecipeId = recipe.getId();
        ashLevel = 0;

        for (int i = 0; i < logStacks.getSlots(); i++) {
            logStacks.setStackInSlot(i, ItemStack.EMPTY);
        }

        setVariant(PitKilnBlock.Variant.ACTIVE);
        setChanged();
        sync();
        return true;
    }

    public void clearActiveFireFromRain() {
        active = false;
        ashLevel = 0;
        resetProgress();
        setVariant(PitKilnBlock.Variant.THATCH);
        extinguishFireAbove();
        setChanged();
        sync();
    }

    public void dropContents(Level level, BlockPos pos) {
        if (!inputStack.isEmpty()) {
            Block.popResource(level, pos, inputStack.copy());
            inputStack = ItemStack.EMPTY;
        }

        for (int i = 0; i < outputStacks.getSlots(); i++) {
            ItemStack stack = outputStacks.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack.copy());
                outputStacks.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < logStacks.getSlots(); i++) {
            ItemStack stack = logStacks.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack.copy());
                logStacks.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    private void tickServer(Level level) {
        if (!active) {
            return;
        }

        ensureFireAbove(level);

        if (AgesCraftingConfig.SERVER.pitKilnExtinguishedByRain.get() && level.isRainingAt(worldPosition.above())) {
            rainTimeRemaining--;
            if (rainTimeRemaining <= 0) {
                clearActiveFireFromRain();
            }
            return;
        }

        rainTimeRemaining = Math.max(1, AgesCraftingConfig.SERVER.pitKilnRainExtinguishTicks.get());

        PitKilnRecipe recipe = resolveActiveRecipe();
        if (recipe == null) {
            finish(false, null);
            return;
        }

        totalTicks = computeTotalTicks(recipe, inputStack.getCount());
        progressTicks++;
        if (progressTicks % 20 == 0) {
            setChanged();
            sync();
        }
        if (progressTicks < totalTicks) {
            return;
        }

        finish(true, recipe);
    }

    private void finish(boolean success, @Nullable PitKilnRecipe recipe) {
        int count = inputStack.getCount();

        if (success && recipe != null) {
            for (int i = 0; i < count; i++) {
                boolean failed = level != null && level.random.nextFloat() < recipe.failureChance();
                ItemStack produced = failed && !recipe.failureResult().isEmpty() ? recipe.failureResult() : recipe.result();
                insertOutputOrDrop(produced.copy());
            }
        }

        inputStack = ItemStack.EMPTY;
        active = false;
        ashLevel = MAX_ASH_LEVEL;
        resetProgress();
        setVariant(PitKilnBlock.Variant.COMPLETE);
        extinguishFireAbove();
        setChanged();
        sync();
        playRecipeCompleteFx(success);
    }

    private void playRecipeCompleteFx(boolean success) {
        if (level == null) {
            return;
        }

        DeviceRecipeSounds.playFinish(level, worldPosition);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    success ? ParticleTypes.CAMPFIRE_COSY_SMOKE : ParticleTypes.SMOKE,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 1.05D,
                    worldPosition.getZ() + 0.5D,
                    success ? 12 : 8,
                    0.28D,
                    0.12D,
                    0.28D,
                    0.01D
            );
            if (success) {
                serverLevel.sendParticles(
                        ParticleTypes.ASH,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.7D,
                        worldPosition.getZ() + 0.5D,
                        10,
                        0.35D,
                        0.05D,
                        0.35D,
                        0.0D
                );
            }
        }
    }

    private void insertOutputOrDrop(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        for (int i = 0; i < outputStacks.getSlots(); i++) {
            ItemStack slotStack = outputStacks.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                outputStacks.setStackInSlot(i, stack.copy());
                return;
            }
            if (ItemStack.isSameItemSameTags(slotStack, stack) && slotStack.getCount() < slotStack.getMaxStackSize()) {
                int move = Math.min(stack.getCount(), slotStack.getMaxStackSize() - slotStack.getCount());
                if (move > 0) {
                    slotStack.grow(move);
                    stack.shrink(move);
                    outputStacks.setStackInSlot(i, slotStack);
                    if (stack.isEmpty()) {
                        return;
                    }
                }
            }
        }

        if (level != null) {
            Block.popResource(level, worldPosition, stack.copy());
        }
    }

    private void ensureFireAbove(Level level) {
        BlockPos above = worldPosition.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.isAir()) {
            level.setBlock(above, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void extinguishFireAbove() {
        if (level == null) {
            return;
        }
        BlockPos above = worldPosition.above();
        if (level.getBlockState(above).is(Blocks.FIRE)) {
            level.removeBlock(above, false);
        }
    }

    private @Nullable PitKilnRecipe resolveRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }

        SimpleContainer container = new SimpleContainer(stack.copy());
        return level.getRecipeManager().getRecipeFor(ModRecipeTypes.PIT_KILN.get(), container, level).orElse(null);
    }

    private @Nullable PitKilnRecipe resolveActiveRecipe() {
        if (level == null || inputStack.isEmpty()) {
            activeRecipeId = null;
            return null;
        }

        if (activeRecipeId != null) {
            for (PitKilnRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.PIT_KILN.get())) {
                if (recipe.getId().equals(activeRecipeId) && recipe.matches(inputStack)) {
                    return recipe;
                }
            }
            activeRecipeId = null;
        }

        PitKilnRecipe recipe = resolveRecipe(inputStack);
        if (recipe != null) {
            activeRecipeId = recipe.getId();
        }
        return recipe;
    }

    private int computeTotalTicks(PitKilnRecipe recipe, int count) {
        int maxStack = Math.max(1, AgesCraftingConfig.SERVER.pitKilnMaxStackSize.get());
        double speedModifier = AgesCraftingConfig.SERVER.pitKilnVariableSpeedModifier.get();
        double x = count <= 1 ? 0.0D : (count - 1.0D) / Math.max(1.0D, maxStack - 1.0D);
        double scalar = (1.0D - speedModifier) * x + speedModifier;
        double base = Math.max(1, recipe.durationTicks()) * Math.max(0.01D, AgesCraftingConfig.SERVER.pitKilnBaseRecipeDurationModifier.get());
        return Math.max(1, (int) Math.round(base * scalar));
    }

    private void resetProgress() {
        progressTicks = 0;
        totalTicks = 0;
        activeRecipeId = null;
    }

    private void setVariant(PitKilnBlock.Variant variant) {
        if (level == null) {
            return;
        }

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof PitKilnBlock)) {
            return;
        }

        int clampedAsh = Math.max(0, Math.min(MAX_ASH_LEVEL, ashLevel));
        if (state.getValue(PitKilnBlock.VARIANT) == variant && state.getValue(PitKilnBlock.ASH) == clampedAsh) {
            return;
        }

        level.setBlock(worldPosition,
                state.setValue(PitKilnBlock.VARIANT, variant)
                        .setValue(PitKilnBlock.ASH, clampedAsh),
                Block.UPDATE_ALL);
    }

    private void sync() {
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
        if (!inputStack.isEmpty()) {
            tag.put(TAG_INPUT, inputStack.save(new CompoundTag()));
        }
        tag.put(TAG_OUTPUT, outputStacks.serializeNBT());
        tag.put(TAG_LOGS, logStacks.serializeNBT());
        tag.putInt(TAG_PROGRESS, progressTicks);
        tag.putInt(TAG_TOTAL, totalTicks);
        tag.putBoolean(TAG_ACTIVE, active);
        tag.putInt(TAG_RAIN, rainTimeRemaining);
        tag.putInt(TAG_ASH, ashLevel);
        if (activeRecipeId != null) {
            tag.putString(TAG_RECIPE, activeRecipeId.toString());
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        inputStack = tag.contains(TAG_INPUT) ? ItemStack.of(tag.getCompound(TAG_INPUT)) : ItemStack.EMPTY;
        outputStacks.deserializeNBT(tag.getCompound(TAG_OUTPUT));
        logStacks.deserializeNBT(tag.getCompound(TAG_LOGS));
        progressTicks = tag.getInt(TAG_PROGRESS);
        totalTicks = tag.getInt(TAG_TOTAL);
        active = tag.getBoolean(TAG_ACTIVE);
        rainTimeRemaining = tag.getInt(TAG_RAIN);
        ashLevel = Math.max(0, Math.min(MAX_ASH_LEVEL, tag.getInt(TAG_ASH)));
        activeRecipeId = tag.contains(TAG_RECIPE) ? ResourceLocation.tryParse(tag.getString(TAG_RECIPE)) : null;
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









