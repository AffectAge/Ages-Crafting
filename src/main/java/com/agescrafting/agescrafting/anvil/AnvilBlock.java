package com.agescrafting.agescrafting.anvil;

import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.agescrafting.agescrafting.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class AnvilBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 6.0D, 15.0D);

    public AnvilBlock(Properties properties) {
        super(properties);
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
        return defaultBlockState();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ANVIL_BE.get(), (l, p, s, be) -> {});
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean topFace = hit.getDirection() == Direction.UP;

        if (held.isEmpty() && !player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack extracted = anvil.extractStored();
                if (!extracted.isEmpty()) {
                    giveToPlayerOrDrop(player, extracted);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!topFace) {
            return InteractionResult.PASS;
        }

        if (anvil.isEmpty()) {
            if (!held.isEmpty() && !level.isClientSide && anvil.insertOne(held)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.isEmpty()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            AnvilRecipe recipe = anvil.getRecipeForTool(held);
            if (recipe == null) {
                return InteractionResult.CONSUME;
            }

            int minFood = AgesCraftingConfig.SERVER.anvilMinFoodLevel.get();
            if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() < minFood) {
                player.displayClientMessage(Component.translatable("message.agescrafting.anvil.low_hunger").withStyle(ChatFormatting.RED), true);
                return InteractionResult.CONSUME;
            }

            if (!player.getAbilities().instabuild) {
                player.causeFoodExhaustion((float) Math.max(0.0D, AgesCraftingConfig.SERVER.anvilExhaustionPerHit.get()));
            }

            boolean completed = anvil.advanceProgress();
            level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.8F, 0.95F + level.random.nextFloat() * 0.15F);

            if (completed) {
                ItemStack result = anvil.consumeForResult();
                if (!result.isEmpty()) {
                    Block.popResource(level, pos.above(), result);
                }
                if (!player.getAbilities().instabuild) {
                    player.causeFoodExhaustion((float) Math.max(0.0D, AgesCraftingConfig.SERVER.anvilExhaustionPerComplete.get()));
                }
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.9F, 1.0F);
                spawnRecipeCompleteFx(level, pos);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AnvilBlockEntity anvil) {
            if (!level.isClientSide) {
                Block.popResource(level, pos, new ItemStack(state.getBlock().asItem()));
                ItemStack drop = anvil.extractStored();
                if (!drop.isEmpty()) {
                    Block.popResource(level, pos, drop);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void spawnRecipeCompleteFx(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.95D,
                    pos.getZ() + 0.5D,
                    8,
                    0.28D,
                    0.12D,
                    0.28D,
                    0.01D);
        }
    }

    private static void giveToPlayerOrDrop(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            Block.popResource(player.level(), player.blockPosition(), stack);
        }
    }
}

