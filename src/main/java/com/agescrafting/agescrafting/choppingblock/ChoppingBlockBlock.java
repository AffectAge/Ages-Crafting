package com.agescrafting.agescrafting.choppingblock;

import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.agescrafting.agescrafting.registry.ModBlocks;
import com.agescrafting.agescrafting.registry.ModItems;
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
import net.minecraft.world.level.LevelAccessor;
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
public class ChoppingBlockBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    public ChoppingBlockBlock(Properties properties) {
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
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ChoppingBlockBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return null;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ChoppingBlockBlockEntity chopping)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean topFace = hit.getDirection() == Direction.UP;

        if (held.isEmpty() && !player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack extracted = chopping.extractStored();
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

        if (chopping.isEmpty()) {
            if (!held.isEmpty() && !level.isClientSide && chopping.insertOne(held)) {
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
            ChoppingBlockRecipe recipe = chopping.getRecipeForTool(held);
            if (recipe == null) {
                return InteractionResult.CONSUME;
            }

            int minFood = AgesCraftingConfig.SERVER.choppingMinFoodLevel.get();
            if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() < minFood) {
                player.displayClientMessage(Component.translatable("message.agescrafting.chopping_block.low_hunger").withStyle(ChatFormatting.RED), true);
                return InteractionResult.CONSUME;
            }

            if (!player.getAbilities().instabuild) {
                player.causeFoodExhaustion((float) Math.max(0.0D, AgesCraftingConfig.SERVER.choppingExhaustionPerHit.get()));
            }

            boolean completed = chopping.advanceProgress();
            int durabilityCost = Math.max(0, recipe.durabilityPerChop());
            if (!player.getAbilities().instabuild && held.isDamageableItem() && durabilityCost > 0) {
                held.hurtAndBreak(durabilityCost, player, p -> p.broadcastBreakEvent(hand));
            }
            level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 0.9F, 0.9F + level.random.nextFloat() * 0.2F);
            tryAccumulateWoodChips(level, pos, hit);

            if (completed) {
                ItemStack result = chopping.consumeForResult();
                if (!result.isEmpty()) {
                    Block.popResource(level, pos.above(), result);
                }
                level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
                spawnRecipeCompleteFx(level, pos);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ChoppingBlockBlockEntity chopping) {
            if (!level.isClientSide) {
                Block.popResource(level, pos, new ItemStack(state.getBlock().asItem()));

                ItemStack drop = chopping.extractStored();
                if (!drop.isEmpty()) {
                    Block.popResource(level, pos, drop);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static int getNearbyChipLayers(LevelAccessor level, BlockPos pos) {
        int total = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState sideState = level.getBlockState(pos.relative(direction));
            if (sideState.is(ModBlocks.WOOD_CHIPS_PILE.get())) {
                total += sideState.getValue(WoodChipsPileBlock.LAYERS);
            }
        }
        return total;
    }

    private void tryAccumulateWoodChips(Level level, BlockPos pos, BlockHitResult hit) {
        double chance = AgesCraftingConfig.SERVER.choppingSawdustChancePerHit.get();
        if (chance <= 0.0D || level.random.nextDouble() > chance) {
            return;
        }

        Direction[] order = getHorizontalPriority(pos, hit);
        for (Direction direction : order) {
            BlockPos sidePos = pos.relative(direction);
            BlockState sideState = level.getBlockState(sidePos);

            if (sideState.is(ModBlocks.WOOD_CHIPS_PILE.get())) {
                int layers = sideState.getValue(WoodChipsPileBlock.LAYERS);
                if (layers < WoodChipsPileBlock.MAX_LAYERS) {
                    level.setBlock(sidePos, sideState.setValue(WoodChipsPileBlock.LAYERS, layers + 1), Block.UPDATE_ALL);
                    return;
                }
                continue;
            }

            BlockState pileState = ModBlocks.WOOD_CHIPS_PILE.get().defaultBlockState();
            if (sideState.canBeReplaced() && pileState.canSurvive(level, sidePos)) {
                level.setBlock(sidePos, pileState, Block.UPDATE_ALL);
                return;
            }
        }

        double dropChance = AgesCraftingConfig.SERVER.choppingScatterChipChancePerHit.get();
        if (dropChance > 0.0D && level.random.nextDouble() <= dropChance) {
            Block.popResource(level, pos, new ItemStack(ModItems.WOOD_CHIPS.get()));
        }
    }

    private static Direction[] getHorizontalPriority(BlockPos pos, BlockHitResult hit) {
        double dx = hit.getLocation().x - (pos.getX() + 0.5D);
        double dz = hit.getLocation().z - (pos.getZ() + 0.5D);

        Direction primary;
        if (Math.abs(dx) > Math.abs(dz)) {
            primary = dx >= 0.0D ? Direction.EAST : Direction.WEST;
        } else {
            primary = dz >= 0.0D ? Direction.SOUTH : Direction.NORTH;
        }

        Direction right = primary.getClockWise();
        Direction left = primary.getCounterClockWise();
        Direction back = primary.getOpposite();
        return new Direction[]{primary, right, left, back};
    }

    private static void spawnRecipeCompleteFx(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.75D,
                    pos.getZ() + 0.5D,
                    10,
                    0.32D,
                    0.12D,
                    0.32D,
                    0.01D);
        }
    }

    private static void giveToPlayerOrDrop(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            Block.popResource(player.level(), player.blockPosition(), stack);
        }
    }
}
