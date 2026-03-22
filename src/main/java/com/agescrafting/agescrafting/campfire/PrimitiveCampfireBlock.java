package com.agescrafting.agescrafting.campfire;

import com.agescrafting.agescrafting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PrimitiveCampfireBlock extends BaseEntityBlock {
    public static final EnumProperty<PrimitiveCampfireBlockEntity.Variant> VARIANT = EnumProperty.create("variant", PrimitiveCampfireBlockEntity.Variant.class);
    public static final IntegerProperty ASH = IntegerProperty.create("ash", 0, PrimitiveCampfireBlockEntity.MAX_ASH_LEVEL);

    private static final VoxelShape SHAPE_TINDER = Block.box(2.0, 0.0, 2.0, 14.0, 5.0, 14.0);
    private static final VoxelShape SHAPE_FULL = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);

    public PrimitiveCampfireBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(VARIANT, PrimitiveCampfireBlockEntity.Variant.NORMAL)
                .setValue(ASH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, ASH);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public int getLightEmission(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.getValue(VARIANT) == PrimitiveCampfireBlockEntity.Variant.LIT ? 13 : 0;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(VARIANT) == PrimitiveCampfireBlockEntity.Variant.LIT ? SHAPE_FULL : SHAPE_TINDER;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PrimitiveCampfireBlockEntity campfire)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.isEmpty() && !player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack cooked = campfire.extractCookItem();
                if (!cooked.isEmpty()) {
                    giveToPlayerOrDrop(player, cooked);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack removed = campfire.removeTopFuel();
                if (!removed.isEmpty()) {
                    giveToPlayerOrDrop(player, removed);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!held.isEmpty() && (held.is(Items.FLINT_AND_STEEL) || held.is(Items.FIRE_CHARGE))) {
            if (!level.isClientSide && campfire.tryIgnite()) {
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

        if (!held.isEmpty() && held.is(ItemTags.LOGS)) {
            if (!level.isClientSide && campfire.addFuel(held)) {
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!held.isEmpty() && held.getItem() instanceof ShovelItem && campfire.getAshLevel() > 0) {
            if (!level.isClientSide) {
                campfire.reduceAsh(1);
                Block.popResource(level, pos, new ItemStack(Items.BONE_MEAL));
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                level.playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!held.isEmpty()) {
            if (!level.isClientSide && campfire.insertCookItem(held)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(VARIANT) != PrimitiveCampfireBlockEntity.Variant.LIT) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.35;
        double z = pos.getZ() + 0.5;

        if (random.nextFloat() < 0.1F) {
            level.playLocalSound(x, pos.getY(), z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 1.0F, false);
        }

        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5D) * 0.3D;
            double offsetZ = (random.nextDouble() - 0.5D) * 0.3D;
            level.addParticle(ParticleTypes.SMOKE, x + offsetX, y, z + offsetZ, 0.0D, 0.02D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, x + offsetX, y, z + offsetZ, 0.0D, 0.02D, 0.0D);
        }

        if (level.getBlockEntity(pos) instanceof PrimitiveCampfireBlockEntity campfire
                && campfire.hasCookItem()
                && campfire.isCooked()
                && !campfire.isOvercooked()) {
            for (int i = 0; i < 2; i++) {
                double offsetX = (random.nextDouble() - 0.5D) * 0.35D;
                double offsetZ = (random.nextDouble() - 0.5D) * 0.35D;
                level.addParticle(ParticleTypes.LARGE_SMOKE, x + offsetX, y + 0.05D, z + offsetZ, 0.0D, 0.025D, 0.0D);
            }
        }
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PrimitiveCampfireBlockEntity campfire) {
            if (!level.isClientSide) {
                Block.popResource(level, pos, new ItemStack(state.getBlock().asItem()));
                campfire.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PrimitiveCampfireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.PRIMITIVE_CAMPFIRE_BE.get(), PrimitiveCampfireBlockEntity::serverTick);
    }

    private static void giveToPlayerOrDrop(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            Block.popResource(player.level(), player.blockPosition(), stack);
        }
    }
}
