package com.agescrafting.agescrafting.campfire;

import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.agescrafting.agescrafting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import com.agescrafting.agescrafting.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrimitiveCampfireBlockEntity extends BlockEntity {
    public static final int MAX_FUEL = 8;
    public static final int MAX_ASH_LEVEL = 8;
    private static final float ASH_CHANCE_ON_LOG_BURN = 0.45F;
    private static final int OVERCOOK_TICKS = 200;
    private static final int RAIN_CHECK_INTERVAL_TICKS = 20;

    private static final String TAG_FUEL = "fuel";
    private static final String TAG_BURN_TIME = "burnTime";
    private static final String TAG_ACTIVE = "active";
    private static final String TAG_ASH = "ashLevel";
    private static final String TAG_COOK_STACK = "cookStack";
    private static final String TAG_COOK_PROGRESS = "cookProgress";
    private static final String TAG_COOK_TOTAL = "cookTotal";
    private static final String TAG_OVERCOOK_PROGRESS = "overcookProgress";
    private static final String TAG_COOK_STAGE = "cookStage";

    private final ItemStackHandler fuelStacks = new ItemStackHandler(MAX_FUEL) {
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

    private int burnTime;
    private boolean active;
    private int ashLevel;

    private ItemStack cookStack = ItemStack.EMPTY;
    private int cookProgress;
    private int cookTotal;
    private int overcookProgress;
    private CookStage cookStage = CookStage.RAW;

    public PrimitiveCampfireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRIMITIVE_CAMPFIRE_BE.get(), pos, state);
    }

        public static void serverTick(Level level, BlockPos pos, BlockState state, PrimitiveCampfireBlockEntity be) {
        be.tickServer(level);
    }

    public boolean addFuel(ItemStack heldStack) {
        if (heldStack.isEmpty() || getFuelCount() >= MAX_FUEL) {
            return false;
        }

        int slot = findFirstEmptySlot();
        if (slot < 0) {
            return false;
        }

        ItemStack one = heldStack.copy();
        one.setCount(1);
        fuelStacks.setStackInSlot(slot, one);

        if (!active) {
            setVariant(Variant.NORMAL);
        }

        setChanged();
        sync();
        return true;
    }

    public ItemStack removeTopFuel() {
        int slot = findLastFilledSlot();
        if (slot < 0) {
            return ItemStack.EMPTY;
        }

        ItemStack extracted = fuelStacks.getStackInSlot(slot).copy();
        fuelStacks.setStackInSlot(slot, ItemStack.EMPTY);

        if (getFuelCount() == 0 && active) {
            active = false;
            setVariant(Variant.NORMAL);
        }

        setChanged();
        sync();
        return extracted;
    }

    public boolean tryIgnite() {
        if (active || getFuelCount() <= 0) {
            return false;
        }

        active = true;
        if (burnTime <= 0) {
            burnTime = getBurnTimePerLog();
        }
        setVariant(Variant.LIT);
        setChanged();
        sync();
        return true;
    }

    public void reduceAsh(int amount) {
        ashLevel = Math.max(0, ashLevel - Math.max(1, amount));
        setVariant(active ? Variant.LIT : Variant.NORMAL);
        setChanged();
        sync();
    }

    public int getFuelCount() {
        int count = 0;
        for (int i = 0; i < MAX_FUEL; i++) {
            if (!fuelStacks.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int getAshLevel() {
        return ashLevel;
    }

    public ItemStack getFuelStack(int slot) {
        if (slot < 0 || slot >= MAX_FUEL) {
            return ItemStack.EMPTY;
        }
        return fuelStacks.getStackInSlot(slot);
    }

    public boolean insertCookItem(ItemStack stack) {
        if (level == null || stack.isEmpty() || !cookStack.isEmpty()) {
            return false;
        }

        ItemStack one = stack.copy();
        one.setCount(1);
        if (resolveSmeltingRecipe(one) == null) {
            return false;
        }

        cookStack = one;
        cookStage = CookStage.RAW;
        cookProgress = 0;
        overcookProgress = 0;
        cookTotal = 0;
        setChanged();
        sync();
        return true;
    }

    public ItemStack extractCookItem() {
        if (cookStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack out = cookStack.copy();
        cookStack = ItemStack.EMPTY;
        cookProgress = 0;
        cookTotal = 0;
        overcookProgress = 0;
        cookStage = CookStage.RAW;
        setChanged();
        sync();
        return out;
    }

    public boolean hasCookItem() {
        return !cookStack.isEmpty();
    }

    public boolean isCooked() {
        return cookStage == CookStage.COOKED;
    }

    public boolean isOvercooked() {
        return cookStage == CookStage.OVERCOOKED;
    }

    public int getCookProgress() {
        return cookProgress;
    }

    public int getCookTotal() {
        return cookTotal;
    }

    public int getOvercookProgress() {
        return overcookProgress;
    }

    public ItemStack getCookStack() {
        return cookStack.copy();
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < MAX_FUEL; i++) {
            ItemStack stack = fuelStacks.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack.copy());
                fuelStacks.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        if (!cookStack.isEmpty()) {
            Block.popResource(level, pos, cookStack.copy());
            cookStack = ItemStack.EMPTY;
        }
    }

    private void tickServer(Level level) {
        if (!active) {
            return;
        }

        tryExtinguishFromRain(level);
        if (!active) {
            return;
        }

        tickFuel(level);
        if (active) {
            tickCooking(level);
        }
    }

    private void tryExtinguishFromRain(Level level) {
        if (!isExposedToRain(level) || level.getGameTime() % RAIN_CHECK_INTERVAL_TICKS != 0L) {
            return;
        }

        double chance = AgesCraftingConfig.SERVER.campfireRainExtinguishChancePerSecond.get();
        if (chance <= 0.0D || level.random.nextDouble() >= chance) {
            return;
        }

        active = false;
        burnTime = 0;
        setVariant(Variant.NORMAL);
        setChanged();
        sync();
    }

    private void tickFuel(Level level) {
        if (burnTime > 0) {
            burnTime--;
        }

        if (burnTime > 0) {
            return;
        }

        int slot = findLastFilledSlot();
        if (slot < 0) {
            active = false;
            setVariant(Variant.NORMAL);
            setChanged();
            sync();
            return;
        }

        fuelStacks.setStackInSlot(slot, ItemStack.EMPTY);

        if (level.random.nextFloat() < ASH_CHANCE_ON_LOG_BURN && ashLevel < MAX_ASH_LEVEL) {
            ashLevel++;
        }

        if (getFuelCount() <= 0) {
            active = false;
            setVariant(Variant.NORMAL);
            setChanged();
            sync();
            return;
        }

        burnTime = getBurnTimePerLog();
        setVariant(Variant.LIT);
        setChanged();
        sync();
    }

    private void tickCooking(Level level) {
        if (cookStack.isEmpty()) {
            cookProgress = 0;
            cookTotal = 0;
            overcookProgress = 0;
            return;
        }

        if (cookStage == CookStage.RAW) {
            AbstractCookingRecipe recipe = resolveSmeltingRecipe(cookStack);
            if (recipe == null) {
                cookProgress = 0;
                cookTotal = 0;
                return;
            }

            cookTotal = Math.max(1, (int) Math.ceil(recipe.getCookingTime() * getCookingTimeMultiplier(level)));
            cookProgress++;
            if (cookProgress < cookTotal) {
                return;
            }

            cookStack = recipe.getResultItem(level.registryAccess()).copy();
            cookStage = CookStage.COOKED;
            level.playSound(null, worldPosition, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.65F, 1.15F);
            cookProgress = 0;
            overcookProgress = 0;
            setChanged();
            sync();
            return;
        }

        if (cookStage == CookStage.COOKED) {
            overcookProgress++;
            if (overcookProgress < OVERCOOK_TICKS) {
                return;
            }

            cookStack = new ItemStack(ModItems.ASH.get());
            cookStage = CookStage.OVERCOOKED;
            overcookProgress = OVERCOOK_TICKS;
            setChanged();
            sync();
        }
    }

    private double getCookingTimeMultiplier(Level level) {
        double multiplier = 1.0D + (ashLevel * Math.max(0.0D, AgesCraftingConfig.SERVER.campfireAshCookPenaltyPerLevel.get()));
        if (isExposedToRain(level)) {
            multiplier *= Math.max(1.0D, AgesCraftingConfig.SERVER.campfireRainCookTimeMultiplier.get());
        }
        return multiplier;
    }

    private boolean isExposedToRain(Level level) {
        return level.isRainingAt(worldPosition.above());
    }

    @Nullable
    private AbstractCookingRecipe resolveSmeltingRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack.copy()), level)
                .orElse(null);
    }

    private void setVariant(Variant variant) {
        if (level == null) {
            return;
        }

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof PrimitiveCampfireBlock)) {
            return;
        }

        int clampedAsh = Math.max(0, Math.min(MAX_ASH_LEVEL, ashLevel));
        if (state.getValue(PrimitiveCampfireBlock.VARIANT) == variant
                && state.getValue(PrimitiveCampfireBlock.ASH) == clampedAsh) {
            return;
        }

        level.setBlock(worldPosition,
                state.setValue(PrimitiveCampfireBlock.VARIANT, variant)
                        .setValue(PrimitiveCampfireBlock.ASH, clampedAsh),
                Block.UPDATE_ALL);
    }

    private int findFirstEmptySlot() {
        for (int i = 0; i < MAX_FUEL; i++) {
            if (fuelStacks.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private int findLastFilledSlot() {
        for (int i = MAX_FUEL - 1; i >= 0; i--) {
            if (!fuelStacks.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }


    public int getBurnTimeRemaining() {
        return Math.max(0, burnTime);
    }

    public int getBurnTimePerLogTicks() {
        return getBurnTimePerLog();
    }

    private static int getBurnTimePerLog() {
        return Math.max(1, AgesCraftingConfig.SERVER.campfireLogBurnTimeTicks.get());
    }    private void sync() {
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
        tag.put(TAG_FUEL, fuelStacks.serializeNBT());
        tag.putInt(TAG_BURN_TIME, burnTime);
        tag.putBoolean(TAG_ACTIVE, active);
        tag.putInt(TAG_ASH, ashLevel);
        if (!cookStack.isEmpty()) {
            tag.put(TAG_COOK_STACK, cookStack.save(new CompoundTag()));
        }
        tag.putInt(TAG_COOK_PROGRESS, cookProgress);
        tag.putInt(TAG_COOK_TOTAL, cookTotal);
        tag.putInt(TAG_OVERCOOK_PROGRESS, overcookProgress);
        tag.putInt(TAG_COOK_STAGE, cookStage.ordinal());
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        fuelStacks.deserializeNBT(tag.getCompound(TAG_FUEL));
        burnTime = tag.getInt(TAG_BURN_TIME);
        active = tag.getBoolean(TAG_ACTIVE);
        ashLevel = Math.max(0, Math.min(MAX_ASH_LEVEL, tag.getInt(TAG_ASH)));
        cookStack = tag.contains(TAG_COOK_STACK) ? ItemStack.of(tag.getCompound(TAG_COOK_STACK)) : ItemStack.EMPTY;
        cookProgress = tag.getInt(TAG_COOK_PROGRESS);
        cookTotal = tag.getInt(TAG_COOK_TOTAL);
        overcookProgress = tag.getInt(TAG_OVERCOOK_PROGRESS);
        int stageIndex = tag.getInt(TAG_COOK_STAGE);
        cookStage = stageIndex >= 0 && stageIndex < CookStage.values().length ? CookStage.values()[stageIndex] : CookStage.RAW;
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

    public enum Variant implements StringRepresentable {
        NORMAL("normal"),
        LIT("lit"),
        ASH("ash");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private enum CookStage {
        RAW,
        COOKED,
        OVERCOOKED
    }
}








