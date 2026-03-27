package com.agescrafting.agescrafting.pitkiln;

import com.agescrafting.agescrafting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PitKilnBlock extends BaseEntityBlock {
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);
    public static final IntegerProperty ASH = IntegerProperty.create("ash", 0, PitKilnBlockEntity.MAX_ASH_LEVEL);

    private static final VoxelShape SHAPE_EMPTY = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
    private static final VoxelShape SHAPE_THATCH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public PitKilnBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, Variant.EMPTY).setValue(ASH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, ASH);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return state.getValue(VARIANT) == Variant.RESULT ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Variant variant = state.getValue(VARIANT);
        if (variant == Variant.EMPTY || variant == Variant.RESULT) {
            return SHAPE_EMPTY;
        }
        if (variant == Variant.THATCH) {
            return SHAPE_THATCH;
        }
        return Shapes.block();
    }
    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln)) {
            return InteractionResult.PASS;
        }

        Variant variant = state.getValue(VARIANT);
        ItemStack held = player.getItemInHand(hand);

        if (variant == Variant.COMPLETE) {
            if (!held.isEmpty() && held.getItem() instanceof ShovelItem && kiln.getAshLevel() > 0) {
                if (!level.isClientSide) {
                    kiln.reduceAsh(1);
                    Block.popResource(level, pos, new ItemStack(Items.BONE_MEAL));
                    held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                    level.playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (kiln.getAshLevel() > 0) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (held.isEmpty()) {
                if (!level.isClientSide) {
                    ItemStack out = kiln.extractOutput();
                    if (!out.isEmpty()) {
                        giveToPlayerOrDrop(player, out);
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if (!held.isEmpty() && (held.is(Items.FLINT_AND_STEEL) || held.is(Items.FIRE_CHARGE))) {
            if (variant == Variant.WOOD && canIgnite(level, pos) && !level.isClientSide && kiln.ignite()) {
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    if (held.is(Items.FLINT_AND_STEEL)) {
                        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                    } else {
                        held.shrink(1);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!held.isEmpty() && held.is(ItemTags.LOGS) && (variant == Variant.THATCH || variant == Variant.WOOD)) {
            if (!level.isClientSide && kiln.addLog(held)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (kiln.getLogCount() >= 6 && variant != Variant.WOOD) {
                    level.setBlock(pos, state.setValue(VARIANT, Variant.WOOD).setValue(ASH, 0), Block.UPDATE_ALL);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!held.isEmpty() && held.is(Items.HAY_BLOCK) && variant == Variant.EMPTY && kiln.getInputCount() > 0) {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.setBlock(pos, state.setValue(VARIANT, Variant.THATCH).setValue(ASH, 0), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.isEmpty()) {
            if (player.isShiftKeyDown() && (variant == Variant.THATCH || variant == Variant.WOOD)) {
                if (!level.isClientSide) {
                    ItemStack removed = kiln.removeLog();
                    if (!removed.isEmpty()) {
                        giveToPlayerOrDrop(player, removed);
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                        if (kiln.getLogCount() < 6 && variant == Variant.WOOD) {
                            level.setBlock(pos, state.setValue(VARIANT, Variant.THATCH).setValue(ASH, 0), Block.UPDATE_ALL);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (variant == Variant.EMPTY || variant == Variant.RESULT) {
                if (!level.isClientSide) {
                    ItemStack out = kiln.extractOutput();
                    if (!out.isEmpty()) {
                        giveToPlayerOrDrop(player, out);
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                        if (!kiln.hasOutput() && kiln.getInputCount() <= 0) {
                            kiln.markRemoveWithoutItemDrop();
                            level.removeBlock(pos, false);
                        }
                    } else {
                        ItemStack extracted = kiln.extractInput();
                        if (!extracted.isEmpty()) {
                            giveToPlayerOrDrop(player, extracted);
                            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            return InteractionResult.PASS;
        }

        if (variant == Variant.EMPTY || variant == Variant.RESULT) {
            if (kiln.hasOutput()) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (variant == Variant.RESULT) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (!level.isClientSide) {
                int inserted = kiln.insertInput(held);
                if (inserted > 0) {
                    if (!player.getAbilities().instabuild) {
                        held.shrink(inserted);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(VARIANT) != Variant.ACTIVE) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.92D;
        double z = pos.getZ() + 0.5D;

        if (random.nextFloat() < 0.1F) {
            level.playLocalSound(x, pos.getY(), z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 1.0F, false);
        }

        level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5D) * 0.2D, y, z + (random.nextDouble() - 0.5D) * 0.2D, 0.0D, 0.02D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, x + (random.nextDouble() - 0.5D) * 0.2D, y, z + (random.nextDouble() - 0.5D) * 0.2D, 0.0D, 0.02D, 0.0D);
    }

    private boolean canIgnite(Level level, BlockPos pos) {
        BlockState up = level.getBlockState(pos.above());
        if (!(up.isAir() || up.is(Blocks.FIRE))) {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos supportPos = pos.relative(direction);
            BlockState support = level.getBlockState(supportPos);
            if (!support.isFaceSturdy(level, supportPos, direction.getOpposite())) {
                return false;
            }
        }

        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return below.isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (level instanceof Level world && state.getValue(VARIANT) == Variant.ACTIVE && !canIgnite(world, pos)) {
            if (level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln) {
                kiln.clearActiveFireFromRain();
            }
        }
        return state;
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln) {
            if (!level.isClientSide) {
                if (state.getValue(VARIANT) != Variant.ACTIVE && state.getValue(VARIANT) != Variant.COMPLETE && !kiln.consumeRemoveWithoutItemDrop()) {
                    Block.popResource(level, pos, new ItemStack(state.getBlock().asItem()));
                }
                kiln.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PitKilnBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.PIT_KILN_BE.get(), PitKilnBlockEntity::serverTick);
    }

    private static void giveToPlayerOrDrop(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            Block.popResource(player.level(), player.blockPosition(), stack);
        }
    }

    public enum Variant implements StringRepresentable {
        EMPTY("empty"),
        RESULT("result"),
        THATCH("thatch"),
        WOOD("wood"),
        ACTIVE("active"),
        COMPLETE("complete");

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
}

