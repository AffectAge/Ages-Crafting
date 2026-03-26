package com.agescrafting.agescrafting.choppingblock;

import com.agescrafting.agescrafting.config.AgesCraftingConfig;
import com.agescrafting.agescrafting.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class WoodChipsPileBlock extends Block {
    public static final int MAX_LAYERS = 8;
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, MAX_LAYERS);

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D)
    };

    public WoodChipsPileBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        return !level.getBlockState(pos.below()).isAir();
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull net.minecraft.core.Direction direction, @NotNull BlockState neighborState, @NotNull net.minecraft.world.level.LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? state : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!canScoop(held)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            int layers = state.getValue(LAYERS);
            if (layers <= 1) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(LAYERS, layers - 1), Block.UPDATE_ALL);
            }

            Block.popResource(level, pos, new ItemStack(ModItems.WOOD_CHIPS.get()));
            level.playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.8F, 1.05F);

            if (held.getItem() instanceof ShovelItem && !player.getAbilities().instabuild) {
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private boolean canScoop(ItemStack held) {
        if (!AgesCraftingConfig.SERVER.choppingRequireShovelForSawdust.get()) {
            return true;
        }
        return held.getItem() instanceof ShovelItem;
    }
}
