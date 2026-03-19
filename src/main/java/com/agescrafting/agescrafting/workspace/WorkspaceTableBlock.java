package com.agescrafting.agescrafting.workspace;

import com.agescrafting.agescrafting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class WorkspaceTableBlock extends BaseEntityBlock {
    public static final VoxelShape BASE_SHAPE = Block.box(0, 0, 0, 16, 2, 16);
    public static final VoxelShape CELL_BOX = Shapes.box(0, 0, 0, 1 / 3.0, 1 / 3.0, 1 / 3.0);
    private static final VoxelShape[] CELL_SHAPES = new VoxelShape[27];
    private static final java.util.Map<Integer, VoxelShape> SHAPE_CACHE = java.util.Collections.synchronizedMap(new java.util.HashMap<>());

    static {
        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    int index = x + z * 3 + y * 9;
                    CELL_SHAPES[index] = CELL_BOX.move(x / 3.0, y / 3.0, z / 3.0);
                }
            }
        }
        SHAPE_CACHE.put(0, BASE_SHAPE);
    }

    public WorkspaceTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof WorkspaceTableBlockEntity blockEntity) {
            int bitmask = blockEntity.getBitmask();
            return SHAPE_CACHE.computeIfAbsent(bitmask, WorkspaceTableBlock::buildShape);
        }

        return BASE_SHAPE;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof WorkspaceTableBlockEntity blockEntity) {
            return blockEntity.onUse(player, hand, hit);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof WorkspaceTableBlockEntity blockEntity) {
            Containers.dropContents(level, pos, blockEntity.getItems());
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WorkspaceTableBlockEntity(pos, state);
    }

    public static boolean validPosition(int @Nullable [] position) {
        return position != null && position[0] >= 0 && position[0] < 3 && position[1] >= 0 && position[1] < 3 && position[2] >= 0 && position[2] < 3;
    }

    public static int getIndex(int @Nullable [] position) {
        if (validPosition(position)) {
            return position[0] + position[2] * 3 + position[1] * 9;
        }

        return -1;
    }

    public static int @Nullable [] getPosition(int index) {
        if (index < 0 || index > 26) {
            return null;
        }

        int y = index / 9;
        int remaining = index % 9;
        int z = remaining / 3;
        int x = remaining % 3;
        return new int[] {x, y, z};
    }

    public static int @Nullable [] getPosition(@NotNull BlockHitResult hit, boolean emptyHand) {
        BlockPos blockPos = hit.getBlockPos();
        Direction direction = hit.getDirection();
        Vec3 localHit = hit.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3i normal = direction.getNormal();
        int handFlag = emptyHand ? -1 : 1;

        double eps = 1 / 6.0;
        int x = (int) Math.floor(localHit.x * 3 + eps * normal.getX() * handFlag);
        int y = (int) Math.floor(localHit.y * 3 + eps * normal.getY() * handFlag);
        int z = (int) Math.floor(localHit.z * 3 + eps * normal.getZ() * handFlag);

        if (x >= 0 && x < 3 && y >= 0 && y < 3 && z >= 0 && z < 3) {
            return new int[] {x, y, z};
        }

        return null;
    }

    private static VoxelShape buildShape(int bitmask) {
        VoxelShape shape = BASE_SHAPE;

        for (int i = 0; i < 27; i++) {
            if (((bitmask >> i) & 1) == 1) {
                shape = Shapes.or(shape, CELL_SHAPES[i]);
            }
        }

        return shape;
    }
}



