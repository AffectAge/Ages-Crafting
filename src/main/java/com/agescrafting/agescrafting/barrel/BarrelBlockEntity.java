package com.agescrafting.agescrafting.barrel;

import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipe;
import com.agescrafting.agescrafting.barrel.recipe.BarrelRecipeInput;
import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.agescrafting.agescrafting.registry.ModBlockEntities;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.material.Fluids;
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

    private static final int INPUT_TANK_COUNT = 1;
    private static final int OUTPUT_TANK_COUNT = 1;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_TANK = "tank";
    private static final String TAG_INPUT_TANK = "inputTank";
    private static final String TAG_OUTPUT_TANK = "outputTank";
    private static final String TAG_SEALED = "sealed";
    private static final String TAG_RECIPE_PROGRESS = "recipeProgress";
    private static final String TAG_ACTIVE_RECIPE = "activeRecipe";

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == CONTAINER_SLOT && !suppressContainerSlotHooks) {
                processContainerSlot = true;
            }
            recipeStateDirty = true;
            setChanged();
        }
    };

    private final FluidTank[] inputTanks = createInputTanks();
    private final FluidTank[] outputTanks = createOutputTanks();

    private final IFluidHandler fluidAccess = new IFluidHandler() {
        @Override
        public int getTanks() {
            return INPUT_TANK_COUNT + OUTPUT_TANK_COUNT;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (tank < INPUT_TANK_COUNT) {
                return inputTanks[tank].getFluid();
            }
            int outputIndex = tank - INPUT_TANK_COUNT;
            if (outputIndex >= 0 && outputIndex < OUTPUT_TANK_COUNT) {
                return outputTanks[outputIndex].getFluid();
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            if (tank < INPUT_TANK_COUNT) {
                return inputTanks[tank].getCapacity();
            }
            int outputIndex = tank - INPUT_TANK_COUNT;
            if (outputIndex >= 0 && outputIndex < OUTPUT_TANK_COUNT) {
                return outputTanks[outputIndex].getCapacity();
            }
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank >= 0 && tank < INPUT_TANK_COUNT;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return fillIntoTanks(inputTanks, resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }

            FluidStack drained = drainFromTanks(outputTanks, resource, action);
            if (!drained.isEmpty()) {
                return drained;
            }
            return drainFromTanks(inputTanks, resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) {
                return FluidStack.EMPTY;
            }

            FluidStack drained = drainFromTanks(outputTanks, maxDrain, action);
            if (!drained.isEmpty()) {
                return drained;
            }
            return drainFromTanks(inputTanks, maxDrain, action);
        }
    };

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getTotalInputAmount();
                case 1 -> getTotalInputCapacity();
                case 2 -> getTotalOutputAmount();
                case 3 -> getTotalOutputCapacity();
                case 4 -> sealed ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Server authoritative data.
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> fluidAccess);

    private boolean sealed;
    private boolean processContainerSlot;
    private boolean suppressContainerSlotHooks;

    private int recipeProgress;
    private @Nullable ResourceLocation activeRecipeId;
    private boolean recipeStateDirty = true;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARREL_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BarrelBlockEntity blockEntity) {
        blockEntity.syncBlockSealState();
        if (blockEntity.processContainerSlot) {
            blockEntity.processContainerSlot = false;
            blockEntity.tryProcessContainerSlot();
        }

        blockEntity.tickRecipe();
        blockEntity.tickRainFill();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ContainerData getMenuData() {
        return menuData;
    }

    public FluidStack getInputFluid() {
        return getFirstNonEmpty(inputTanks);
    }

    public FluidStack getOutputFluid() {
        return getFirstNonEmpty(outputTanks);
    }

    public int getInputFluidAmount() {
        return getTotalInputAmount();
    }

    public int getInputTankCapacity() {
        return getTotalInputCapacity();
    }

    public int getOutputFluidAmount() {
        return getTotalOutputAmount();
    }

    public int getOutputTankCapacity() {
        return getTotalOutputCapacity();
    }

    public boolean isSealed() {
        return sealed;
    }

    public int getRecipeProgressTicks() {
        return Math.max(0, recipeProgress);
    }

    public int getRecipeTotalTicks() {
        if (activeRecipeId == null || level == null) {
            return 0;
        }
        BarrelRecipe recipe = findRecipeById(activeRecipeId);
        if (recipe == null || !recipe.requiresSealed()) {
            return 0;
        }
        return Math.max(1, recipe.durationTicks());
    }

    public boolean toggleSealed() {
        sealed = !sealed;

        if (!sealed) {
            resetRecipeProgress();
        }

        recipeStateDirty = true;
        setChanged();
        syncBlockSealState();
        markForSync();
        return sealed;
    }

    public ItemStack createSealedDropStack() {
        ItemStack stack = new ItemStack(getBlockState().getBlock().asItem());
        CompoundTag blockEntityTag = new CompoundTag();
        saveAdditional(blockEntityTag);
        BlockItem.setBlockEntityData(stack, getType(), blockEntityTag);

        return stack;
    }

    public List<ItemStack> getUnsealedDrops() {
        List<ItemStack> drops = new ArrayList<>();

        ItemStack barrelItem = new ItemStack(getBlockState().getBlock().asItem());

        drops.add(barrelItem);

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
        FluidStack contained = FluidUtil.getFluidContained(singleItem).orElse(FluidStack.EMPTY);

        if (!contained.isEmpty()) {
            var emptyResult = FluidUtil.tryEmptyContainer(singleItem, createInputFillHandler(), Integer.MAX_VALUE, null, true);
            if (emptyResult.isSuccess()) {
                applyContainerResult(emptyResult.getResult());
            }
            return;
        }

        if (tryFillFromTanks(singleItem, outputTanks)) {
            return;
        }
        tryFillFromTanks(singleItem, inputTanks);
    }

    private IFluidHandler createInputFillHandler() {
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return INPUT_TANK_COUNT;
            }

            @Override
            public @NotNull FluidStack getFluidInTank(int tank) {
                return tank >= 0 && tank < INPUT_TANK_COUNT ? inputTanks[tank].getFluid() : FluidStack.EMPTY;
            }

            @Override
            public int getTankCapacity(int tank) {
                return tank >= 0 && tank < INPUT_TANK_COUNT ? inputTanks[tank].getCapacity() : 0;
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                return tank >= 0 && tank < INPUT_TANK_COUNT;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return fillIntoTanks(inputTanks, resource, action);
            }

            @Override
            public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
                return FluidStack.EMPTY;
            }

            @Override
            public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
                return FluidStack.EMPTY;
            }
        };
    }

    private boolean tryFillFromTanks(ItemStack singleItem, FluidTank[] tanks) {
        var fillResult = FluidUtil.tryFillContainer(singleItem, createDrainHandler(tanks), Integer.MAX_VALUE, null, true);
        if (!fillResult.isSuccess()) {
            return false;
        }
        applyContainerResult(fillResult.getResult());
        return true;
    }

    private IFluidHandler createDrainHandler(FluidTank[] tanks) {
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return tanks.length;
            }

            @Override
            public @NotNull FluidStack getFluidInTank(int tank) {
                return tank >= 0 && tank < tanks.length ? tanks[tank].getFluid() : FluidStack.EMPTY;
            }

            @Override
            public int getTankCapacity(int tank) {
                return tank >= 0 && tank < tanks.length ? tanks[tank].getCapacity() : 0;
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                return false;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return 0;
            }

            @Override
            public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
                return drainFromTanks(tanks, resource, action);
            }

            @Override
            public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
                return drainFromTanks(tanks, maxDrain, action);
            }
        };
    }

    private void applyContainerResult(ItemStack resultContainer) {
        suppressContainerSlotHooks = true;
        itemHandler.extractItem(CONTAINER_SLOT, 1, false);
        ItemStack remainder = itemHandler.insertItem(CONTAINER_SLOT, resultContainer, false);
        suppressContainerSlotHooks = false;

        if (!remainder.isEmpty() && level != null) {
            Block.popResource(level, worldPosition, remainder);
        }

        recipeStateDirty = true;
        markForSync();
    }

    
    private void tickRainFill() {
        if (level == null || level.isClientSide || sealed) {
            return;
        }

        if (!AgesCraftingConfig.SERVER.barrelRainFillEnabled.get()) {
            return;
        }

        int interval = Math.max(1, AgesCraftingConfig.SERVER.barrelRainFillIntervalTicks.get());
        if (level.getGameTime() % interval != 0L) {
            return;
        }

        if (getTotalInputAmount() > 0 || getTotalOutputAmount() > 0) {
            return;
        }

        if (!level.isRainingAt(worldPosition.above())) {
            return;
        }

        int amount = Math.max(1, AgesCraftingConfig.SERVER.barrelRainFillAmountMb.get());
        int filled = fillIntoTanks(inputTanks, new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0) {
            recipeStateDirty = true;
            setChanged();
            markForSync();
        }
    }

    private void tickRecipe() {
        if (level == null || level.isClientSide) {
            return;
        }

        BarrelRecipe recipe = resolveActiveRecipe();
        if (recipe == null) {
            return;
        }

        if (recipe.requiresSealed() && !sealed) {
            resetRecipeProgress();
            return;
        }

        int[] itemConsumption = computeItemConsumption(recipe);
        if (itemConsumption == null) {
            resetRecipeProgress();
            return;
        }

        int[] fluidConsumption = computeFluidConsumption(recipe);
        if (fluidConsumption == null) {
            resetRecipeProgress();
            return;
        }

        if (!canAcceptFluidResults(recipe.fluidResults())) {
            resetRecipeProgress();
            return;
        }

        if (recipe.requiresSealed()) {
            int targetTicks = Math.max(1, recipe.durationTicks());
            recipeProgress++;
            if (recipeProgress >= targetTicks) {
                if (executeCraft(recipe, itemConsumption, fluidConsumption)) {
                    recipeProgress = 0;
                } else {
                    resetRecipeProgress();
                }
            }
            return;
        }

        executeCraft(recipe, itemConsumption, fluidConsumption);
        recipeProgress = 0;
    }

    private @Nullable BarrelRecipe resolveActiveRecipe() {
        BarrelRecipeInput input = createRecipeInput();

        if (activeRecipeId != null) {
            BarrelRecipe activeRecipe = findRecipeById(activeRecipeId);
            if (activeRecipe != null && activeRecipe.matches(input)) {
                return activeRecipe;
            }

            activeRecipeId = null;
            recipeProgress = 0;
        }

        if (!recipeStateDirty && level != null && level.getGameTime() % 20L != 0L) {
            return null;
        }

        for (BarrelRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.BARREL.get())) {
            if (recipe.matches(input)) {
                if (recipe.requiresSealed() && !sealed) {
                    continue;
                }
                activeRecipeId = recipe.getId();
                recipeStateDirty = false;
                return recipe;
            }
        }

        recipeStateDirty = false;
        return null;
    }

    private @Nullable BarrelRecipe findRecipeById(ResourceLocation id) {
        for (BarrelRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.BARREL.get())) {
            if (recipe.getId().equals(id)) {
                return recipe;
            }
        }
        return null;
    }

    private BarrelRecipeInput createRecipeInput() {
        List<ItemStack> inputItems = new ArrayList<>(ITEM_GRID_COUNT);
        for (int i = 0; i < ITEM_GRID_COUNT; i++) {
            inputItems.add(itemHandler.getStackInSlot(ITEM_GRID_START + i));
        }

        List<FluidStack> fluids = new ArrayList<>(INPUT_TANK_COUNT);
        for (FluidTank tank : inputTanks) {
            fluids.add(tank.getFluid());
        }
        return new BarrelRecipeInput(inputItems, fluids);
    }

    private @Nullable int[] computeItemConsumption(BarrelRecipe recipe) {
        int[] available = new int[ITEM_GRID_COUNT];
        int[] consume = new int[ITEM_GRID_COUNT];

        for (int i = 0; i < ITEM_GRID_COUNT; i++) {
            available[i] = itemHandler.getStackInSlot(ITEM_GRID_START + i).getCount();
        }

        for (BarrelRecipe.IngredientWithCount requirement : recipe.itemIngredients()) {
            int remaining = requirement.count();
            for (int slot = 0; slot < ITEM_GRID_COUNT && remaining > 0; slot++) {
                ItemStack stack = itemHandler.getStackInSlot(ITEM_GRID_START + slot);
                if (stack.isEmpty() || !requirement.ingredient().test(stack)) {
                    continue;
                }

                int taken = Math.min(remaining, available[slot]);
                if (taken > 0) {
                    available[slot] -= taken;
                    consume[slot] += taken;
                    remaining -= taken;
                }
            }

            if (remaining > 0) {
                return null;
            }
        }

        return consume;
    }

    private @Nullable int[] computeFluidConsumption(BarrelRecipe recipe) {
        List<FluidStack> requirements = recipe.fluidIngredients();
        if (requirements.isEmpty()) {
            return new int[INPUT_TANK_COUNT];
        }

        int[] available = new int[INPUT_TANK_COUNT];
        int[] consume = new int[INPUT_TANK_COUNT];
        for (int i = 0; i < INPUT_TANK_COUNT; i++) {
            available[i] = inputTanks[i].getFluidAmount();
        }

        for (FluidStack need : requirements) {
            int remaining = need.getAmount();
            for (int tankIndex = 0; tankIndex < INPUT_TANK_COUNT && remaining > 0; tankIndex++) {
                FluidStack stored = inputTanks[tankIndex].getFluid();
                if (stored.isEmpty() || !stored.isFluidEqual(need)) {
                    continue;
                }

                int taken = Math.min(remaining, available[tankIndex]);
                if (taken > 0) {
                    available[tankIndex] -= taken;
                    consume[tankIndex] += taken;
                    remaining -= taken;
                }
            }

            if (remaining > 0) {
                return null;
            }
        }

        return consume;
    }

    private boolean canAcceptFluidResults(List<FluidStack> results) {
        if (results.isEmpty()) {
            return true;
        }

        FluidStack[] simulatedFluids = new FluidStack[OUTPUT_TANK_COUNT];
        int[] simulatedAmounts = new int[OUTPUT_TANK_COUNT];
        for (int i = 0; i < OUTPUT_TANK_COUNT; i++) {
            simulatedFluids[i] = outputTanks[i].getFluid().copy();
            simulatedAmounts[i] = outputTanks[i].getFluidAmount();
        }

        for (FluidStack result : results) {
            int remaining = result.getAmount();

            for (int i = 0; i < OUTPUT_TANK_COUNT && remaining > 0; i++) {
                if (simulatedAmounts[i] <= 0 || !simulatedFluids[i].isFluidEqual(result)) {
                    continue;
                }
                int free = outputTanks[i].getCapacity() - simulatedAmounts[i];
                int moved = Math.min(remaining, free);
                simulatedAmounts[i] += moved;
                remaining -= moved;
            }

            for (int i = 0; i < OUTPUT_TANK_COUNT && remaining > 0; i++) {
                if (simulatedAmounts[i] > 0) {
                    continue;
                }
                int moved = Math.min(remaining, outputTanks[i].getCapacity());
                simulatedFluids[i] = new FluidStack(result, moved);
                simulatedAmounts[i] = moved;
                remaining -= moved;
            }

            if (remaining > 0) {
                return false;
            }
        }

        return true;
    }

    private boolean executeCraft(BarrelRecipe recipe, int[] itemConsumption, int[] fluidConsumption) {
        if (level == null) {
            return false;
        }

        for (int slot = 0; slot < ITEM_GRID_COUNT; slot++) {
            int amount = itemConsumption[slot];
            if (amount > 0) {
                itemHandler.extractItem(ITEM_GRID_START + slot, amount, false);
            }
        }

        for (int i = 0; i < INPUT_TANK_COUNT; i++) {
            int amount = fluidConsumption[i];
            if (amount > 0) {
                inputTanks[i].drain(amount, IFluidHandler.FluidAction.EXECUTE);
            }
        }

        for (ItemStack result : recipe.itemResults()) {
            if (!result.isEmpty()) {
                insertResultIntoGridOrDrop(result.copy());
            }
        }

        for (FluidStack resultFluid : recipe.fluidResults()) {
            if (resultFluid.isEmpty()) {
                continue;
            }
            int filled = fillIntoTanks(outputTanks, resultFluid, IFluidHandler.FluidAction.EXECUTE);
            if (filled < resultFluid.getAmount()) {
                return false;
            }
        }

        recipeStateDirty = true;
        setChanged();
        syncBlockSealState();
        markForSync();
        return true;
    }

    private void insertResultIntoGridOrDrop(ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < ITEM_GRID_COUNT && !remaining.isEmpty(); slot++) {
            remaining = itemHandler.insertItem(ITEM_GRID_START + slot, remaining, false);
        }

        if (!remaining.isEmpty() && level != null) {
            Block.popResource(level, worldPosition, remaining);
        }
    }

    private void resetRecipeProgress() {
        recipeProgress = 0;
        activeRecipeId = null;
        recipeStateDirty = true;
    }
    @Override
    public void onLoad() {
        super.onLoad();
        syncBlockSealState();
    }

    private void syncBlockSealState() {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        if (state.getBlock() instanceof BarrelBlock && state.hasProperty(BarrelBlock.SEALED) && state.getValue(BarrelBlock.SEALED) != sealed) {
            level.setBlock(worldPosition, state.setValue(BarrelBlock.SEALED, sealed), Block.UPDATE_ALL);
        }
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
        return getBlockState().getBlock().getName();
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

        for (int i = 0; i < INPUT_TANK_COUNT; i++) {
            tag.put(TAG_INPUT_TANK + i, inputTanks[i].writeToNBT(new CompoundTag()));
        }
        for (int i = 0; i < OUTPUT_TANK_COUNT; i++) {
            tag.put(TAG_OUTPUT_TANK + i, outputTanks[i].writeToNBT(new CompoundTag()));
        }

        tag.putBoolean(TAG_SEALED, sealed);
        tag.putInt(TAG_RECIPE_PROGRESS, recipeProgress);

        if (activeRecipeId != null) {
            tag.putString(TAG_ACTIVE_RECIPE, activeRecipeId.toString());
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound(TAG_ITEMS));

        if (tag.contains(TAG_INPUT_TANK + "0")) {
            for (int i = 0; i < INPUT_TANK_COUNT; i++) {
                inputTanks[i].readFromNBT(tag.getCompound(TAG_INPUT_TANK + i));
            }
        } else if (tag.contains(TAG_INPUT_TANK)) {
            inputTanks[0].readFromNBT(tag.getCompound(TAG_INPUT_TANK));
        } else {
            inputTanks[0].readFromNBT(tag.getCompound(TAG_TANK));
        }

        if (tag.contains(TAG_OUTPUT_TANK + "0")) {
            for (int i = 0; i < OUTPUT_TANK_COUNT; i++) {
                outputTanks[i].readFromNBT(tag.getCompound(TAG_OUTPUT_TANK + i));
            }
        } else if (tag.contains(TAG_OUTPUT_TANK)) {
            outputTanks[0].readFromNBT(tag.getCompound(TAG_OUTPUT_TANK));
        }

        sealed = tag.getBoolean(TAG_SEALED);
        recipeProgress = tag.getInt(TAG_RECIPE_PROGRESS);
        activeRecipeId = tag.contains(TAG_ACTIVE_RECIPE) ? ResourceLocation.tryParse(tag.getString(TAG_ACTIVE_RECIPE)) : null;
        recipeStateDirty = true;
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

    private FluidTank[] createInputTanks() {
        FluidTank[] tanks = new FluidTank[INPUT_TANK_COUNT];
        for (int i = 0; i < INPUT_TANK_COUNT; i++) {
            tanks[i] = new FluidTank(getConfiguredCapacity()) {
                @Override
                protected void onContentsChanged() {
                    recipeStateDirty = true;
                    setChanged();
                    markForSync();
                }
            };
        }
        return tanks;
    }

    private FluidTank[] createOutputTanks() {
        FluidTank[] tanks = new FluidTank[OUTPUT_TANK_COUNT];
        for (int i = 0; i < OUTPUT_TANK_COUNT; i++) {
            tanks[i] = new FluidTank(getConfiguredCapacity()) {
                @Override
                protected void onContentsChanged() {
                    setChanged();
                    markForSync();
                }
            };
        }
        return tanks;
    }

    private int fillIntoTanks(FluidTank[] tanks, FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }

        int remaining = resource.getAmount();
        int filled = 0;

        for (FluidTank tank : tanks) {
            FluidStack stored = tank.getFluid();
            if (stored.isEmpty() || !stored.isFluidEqual(resource)) {
                continue;
            }
            FluidStack piece = resource.copy();
            piece.setAmount(remaining);
            int moved = tank.fill(piece, action);
            filled += moved;
            remaining -= moved;
            if (remaining <= 0) {
                return filled;
            }
        }

        for (FluidTank tank : tanks) {
            if (!tank.getFluid().isEmpty()) {
                continue;
            }
            FluidStack piece = resource.copy();
            piece.setAmount(remaining);
            int moved = tank.fill(piece, action);
            filled += moved;
            remaining -= moved;
            if (remaining <= 0) {
                return filled;
            }
        }

        return filled;
    }

    private FluidStack drainFromTanks(FluidTank[] tanks, FluidStack resource, IFluidHandler.FluidAction action) {
        for (FluidTank tank : tanks) {
            FluidStack stored = tank.getFluid();
            if (stored.isEmpty() || !stored.isFluidEqual(resource)) {
                continue;
            }
            return tank.drain(resource, action);
        }
        return FluidStack.EMPTY;
    }

    private FluidStack drainFromTanks(FluidTank[] tanks, int maxDrain, IFluidHandler.FluidAction action) {
        for (FluidTank tank : tanks) {
            if (tank.getFluidAmount() > 0) {
                return tank.drain(maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }

    private FluidStack getFirstNonEmpty(FluidTank[] tanks) {
        for (FluidTank tank : tanks) {
            if (tank.getFluidAmount() > 0) {
                return tank.getFluid().copy();
            }
        }
        return FluidStack.EMPTY;
    }

    private int getTotalInputAmount() {
        int amount = 0;
        for (FluidTank tank : inputTanks) {
            amount += tank.getFluidAmount();
        }
        return amount;
    }

    private int getTotalOutputAmount() {
        int amount = 0;
        for (FluidTank tank : outputTanks) {
            amount += tank.getFluidAmount();
        }
        return amount;
    }

    private int getTotalInputCapacity() {
        return INPUT_TANK_COUNT * Math.max(1, getConfiguredCapacity());
    }

    private int getTotalOutputCapacity() {
        return OUTPUT_TANK_COUNT * Math.max(1, getConfiguredCapacity());
    }

    private static int getConfiguredCapacity() {
        return Math.max(1000, AgesCraftingConfig.SERVER.barrelTankCapacityMb.get());
    }
}














