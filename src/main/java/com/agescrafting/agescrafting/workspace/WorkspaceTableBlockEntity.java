package com.agescrafting.agescrafting.workspace;

import com.agescrafting.agescrafting.compat.gamestages.GameStagesCompat;
import com.agescrafting.agescrafting.registry.ModBlockEntities;
import com.agescrafting.agescrafting.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WorkspaceTableBlockEntity extends BlockEntity {
    private static final String TAG_ITEMS = "items";

    private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int bitmask = 0;

    public WorkspaceTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WORKSPACE_TABLE_BE.get(), pos, state);
    }

    public @NotNull InteractionResult onUse(@NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.is(Items.FLINT)) {
            return tryCraft(serverLevel, player, hand, hit, held);
        }

        if (held.isEmpty()) {
            int index = findRemovalIndex(hit);
            if (index < 0 || index >= items.size() || items.get(index).isEmpty()) {
                return InteractionResult.PASS;
            }

            ItemStack extracted = items.get(index).copy();
            items.set(index, ItemStack.EMPTY);
            recomputeBitmask();
            Block.popResourceFromFace(serverLevel, worldPosition, hit.getDirection(), extracted);
            serverLevel.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
            markUpdated();
            return InteractionResult.CONSUME;
        }

        int[] slotPos = WorkspaceTableBlock.getPosition(hit, false);
        if (!WorkspaceTableBlock.validPosition(slotPos)) {
            return InteractionResult.PASS;
        }

        int index = WorkspaceTableBlock.getIndex(slotPos);
        if (index < 0 || index >= items.size()) {
            return InteractionResult.PASS;
        }

        if (items.get(index).isEmpty()) {
            items.set(index, held.split(1));
            serverLevel.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
            recomputeBitmask();
            markUpdated();
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public int getBitmask() {
        return bitmask;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    private @NotNull InteractionResult tryCraft(ServerLevel serverLevel, Player player, InteractionHand hand, BlockHitResult hit, ItemStack flint) {
        WorkspaceRecipeInput input = new WorkspaceRecipeInput(items);
        Optional<WorkspaceCraftingRecipe> recipe = serverLevel.getRecipeManager().getRecipeFor(ModRecipeTypes.WORKSPACE_CRAFTING.get(), input, serverLevel);

        if (recipe.isEmpty()) {
            return InteractionResult.PASS;
        }

        WorkspaceCraftingRecipe matchedRecipe = recipe.get();
        if (!GameStagesCompat.canCraft(player, matchedRecipe)) {
            String required = matchedRecipe.getRequiredStage().orElse("");
            player.displayClientMessage(Component.translatable("message.agescrafting.stage_required", required), true);
            return InteractionResult.CONSUME;
        }

        ItemStack result = matchedRecipe.assemble(input, serverLevel.registryAccess());
        if (!(result.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.PASS;
        }

        BlockState targetState = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, hand, hit)));
        if (targetState == null) {
            targetState = blockItem.getBlock().defaultBlockState();
        }

        // Clear internal inventory first so block replacement does not drop ingredients.
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        recomputeBitmask();
        setChanged();

        serverLevel.setBlockAndUpdate(worldPosition, targetState);
        serverLevel.blockEntityChanged(worldPosition);
        serverLevel.playSound(null, worldPosition, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        serverLevel.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.45F, 1.35F);
        spawnWorkspaceAssembleFx(serverLevel, targetState);

        flint.hurtAndBreak(1, player, breaker -> breaker.broadcastBreakEvent(hand));
        return InteractionResult.CONSUME;
    }


    private void spawnWorkspaceAssembleFx(ServerLevel serverLevel, BlockState targetState) {
        double cx = worldPosition.getX() + 0.5D;
        double cy = worldPosition.getY() + 0.5D;
        double cz = worldPosition.getZ() + 0.5D;

        for (Direction direction : Direction.values()) {
            double dx = direction.getStepX();
            double dy = direction.getStepY();
            double dz = direction.getStepZ();

            for (int i = 0; i < 8; i++) {
                double px = cx + dx * 0.54D + (serverLevel.random.nextDouble() - 0.5D) * (dy == 0 ? 0.24D : 0.18D);
                double py = cy + dy * 0.54D + (serverLevel.random.nextDouble() - 0.5D) * (dy == 0 ? 0.18D : 0.10D);
                double pz = cz + dz * 0.54D + (serverLevel.random.nextDouble() - 0.5D) * (dy == 0 ? 0.24D : 0.18D);

                double vx = -dx * 0.045D + (serverLevel.random.nextDouble() - 0.5D) * 0.012D;
                double vy = -dy * 0.045D + 0.004D + (serverLevel.random.nextDouble() - 0.5D) * 0.012D;
                double vz = -dz * 0.045D + (serverLevel.random.nextDouble() - 0.5D) * 0.012D;

                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, vx, vy, vz, 1.0D);
            }
        }

        for (int i = 0; i < 20; i++) {
            double vx = (serverLevel.random.nextDouble() - 0.5D) * 0.035D;
            double vy = 0.045D + serverLevel.random.nextDouble() * 0.045D;
            double vz = (serverLevel.random.nextDouble() - 0.5D) * 0.035D;
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, cx, cy + 0.36D, cz, 0, vx, vy, vz, 1.0D);
        }
    }

    private void markUpdated() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
            level.blockEntityChanged(worldPosition);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, items);
        recomputeBitmask();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
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

    private void recomputeBitmask() {
        bitmask = 0;
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                bitmask |= 1 << i;
            }
        }
    }

    private int findRemovalIndex(@NotNull BlockHitResult hit) {
        Vec3 localHit = hit.getLocation().subtract(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        double x = localHit.x;
        double y = localHit.y;
        double z = localHit.z;
        double eps = 1e-4;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                continue;
            }

            int[] pos = WorkspaceTableBlock.getPosition(i);
            if (pos == null) {
                continue;
            }

            double minX = pos[0] / 3.0;
            double minY = pos[1] / 3.0;
            double minZ = pos[2] / 3.0;
            double maxX = (pos[0] + 1) / 3.0;
            double maxY = (pos[1] + 1) / 3.0;
            double maxZ = (pos[2] + 1) / 3.0;

            if (x >= minX - eps && x <= maxX + eps
                    && y >= minY - eps && y <= maxY + eps
                    && z >= minZ - eps && z <= maxZ + eps) {
                return i;
            }
        }

        int[] fallback = WorkspaceTableBlock.getPosition(hit, true);
        if (WorkspaceTableBlock.validPosition(fallback)) {
            return WorkspaceTableBlock.getIndex(fallback);
        }

        return -1;
    }
}










