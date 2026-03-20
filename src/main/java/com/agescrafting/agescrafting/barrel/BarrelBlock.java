package com.agescrafting.agescrafting.barrel;

import com.agescrafting.agescrafting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BarrelBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty SEALED = BooleanProperty.create("sealed");
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public BarrelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SEALED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SEALED);
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
    public @NotNull PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return PushReaction.BLOCK;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.BARREL_BE.get(), BarrelBlockEntity::serverTick);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(SEALED, false);
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity blockEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && heldItem.isEmpty()) {
            if (!level.isClientSide) {
                boolean sealed = blockEntity.toggleSealed();
                if (state.getValue(SEALED) != sealed) {
                    level.setBlock(pos, state.setValue(SEALED, sealed), Block.UPDATE_ALL);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!blockEntity.isSealed() && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BarrelBlockEntity blockEntity) {
            if (blockEntity.isSealed()) {
                Block.popResource(level, pos, blockEntity.createSealedDropStack());
            } else {
                for (ItemStack stack : blockEntity.getUnsealedDrops()) {
                    Block.popResource(level, pos, stack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
