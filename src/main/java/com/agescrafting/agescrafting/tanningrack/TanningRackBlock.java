package com.agescrafting.agescrafting.tanningrack;

import com.agescrafting.agescrafting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TanningRackBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public TanningRackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public @NotNull BlockState updateShape(
            @NotNull BlockState state,
            @NotNull Direction direction,
            @NotNull BlockState neighborState,
            @NotNull LevelAccessor level,
            @NotNull BlockPos pos,
            @NotNull BlockPos neighborPos
    ) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TanningRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.TANNING_RACK_BE.get(), (l, p, s, be) -> { });
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TanningRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack extracted = rack.extractStored();
                if (!extracted.isEmpty()) {
                    if (!player.addItem(extracted)) {
                        Block.popResource(level, pos, extracted);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (rack.isEmpty()) {
            if (!level.isClientSide && rack.insertOne(held)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            TanningRackRecipe recipe = rack.getRecipeForTool(held);
            if (recipe == null) {
                return InteractionResult.CONSUME;
            }

            TanningRackBlockEntity.HitResult hitResult = rack.applyHit(level);
            level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 0.7F, 1.1F);

            int durabilityCost = Math.max(0, recipe.durabilityPerHit());
            if (!player.getAbilities().instabuild && held.isDamageableItem() && durabilityCost > 0) {
                held.hurtAndBreak(durabilityCost, player, p -> p.broadcastBreakEvent(hand));
            }

            if (hitResult == TanningRackBlockEntity.HitResult.FAILED) {
                level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            pos.getX() + 0.5D,
                            pos.getY() + 0.9D,
                            pos.getZ() + 0.5D,
                            5,
                            0.2D,
                            0.08D,
                            0.2D,
                            0.0D
                    );
                }
            } else if (hitResult == TanningRackBlockEntity.HitResult.COMPLETED) {
                ItemStack out = rack.completeAndReplaceResult();
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8F, 1.2F);
                if (!out.isEmpty() && level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5D,
                            pos.getY() + 0.9D,
                            pos.getZ() + 0.5D,
                            6,
                            0.22D,
                            0.08D,
                            0.22D,
                            0.0D
                    );
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TanningRackBlockEntity rack) {
            if (!level.isClientSide) {
                Block.popResource(level, pos, new ItemStack(state.getBlock().asItem()));
                ItemStack drop = rack.extractStored();
                if (!drop.isEmpty()) {
                    Block.popResource(level, pos, drop);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
