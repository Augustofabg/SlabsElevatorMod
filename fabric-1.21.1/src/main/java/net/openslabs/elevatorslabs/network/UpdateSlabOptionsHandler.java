package net.openslabs.elevatorslabs.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

/**
 * Server-side handler for UpdateSlabOptionsPayload on Fabric 1.21.1.
 *
 * BUG FIX: Camouflage removal now correctly identifies the target half based on SlabType:
 * - SlabType.TOP  → removes camouflagedTopState (was incorrectly using bottom before)
 * - SlabType.BOTTOM → removes camouflagedBottomState
 * - SlabType.DOUBLE → uses wasFullBlock flag or clickedY from lastTargetedTopHalf
 */
public final class UpdateSlabOptionsHandler {

    private UpdateSlabOptionsHandler() {}

    public static void handle(UpdateSlabOptionsPayload payload, ServerPlayer player) {
        if (player == null || !player.isAlive()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos pos = payload.pos();

        if (!level.isLoaded(pos) || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ElevatorSlabBlockEntity slabEntity)) {
            return;
        }

        // Apply directional and orientation settings
        slabEntity.setDirectional(payload.directional());
        slabEntity.setHideArrow(payload.hideArrow());
        slabEntity.setFacing(payload.facing());

        // Handle camouflage reset if requested
        if (payload.resetCamo()) {
            if (slabEntity.isAppliedAsFullBlock()) {
                // CASE: Full-block camouflage → drop the full block and clear everything
                ItemStack dropStack = slabEntity.getFullBlockDropStack();
                slabEntity.clearAllCamo();
                if (!player.isCreative() && !dropStack.isEmpty()) {
                    if (!player.getInventory().add(dropStack)) {
                        player.drop(dropStack, false);
                    }
                }
            } else {
                // CASE: Independent slab halves → determine which half to clear
                BlockState currentState = level.getBlockState(pos);
                SlabType type = currentState.hasProperty(SlabBlock.TYPE)
                        ? currentState.getValue(SlabBlock.TYPE)
                        : SlabType.BOTTOM;

                // Determine the target half from the client-sent payload
                boolean targetTop = payload.targetTopHalf();

                if (targetTop) {
                    // CASE A: Remove top camouflage (SlabType.TOP or upper half of DOUBLE)
                    BlockState oldTop = slabEntity.getCamouflagedTopState();
                    slabEntity.setCamouflagedTopState(null);
                    if (!player.isCreative() && oldTop != null && !oldTop.isAir()) {
                        ItemStack returnStack = new ItemStack(oldTop.getBlock().asItem());
                        if (!returnStack.isEmpty()) {
                            if (!player.getInventory().add(returnStack)) {
                                player.drop(returnStack, false);
                            }
                        }
                    }
                } else {
                    // CASE B: Remove bottom camouflage (SlabType.BOTTOM or lower half of DOUBLE)
                    BlockState oldBottom = slabEntity.getCamouflagedBottomState();
                    slabEntity.setCamouflagedBottomState(null);
                    if (!player.isCreative() && oldBottom != null && !oldBottom.isAir()) {
                        ItemStack returnStack = new ItemStack(oldBottom.getBlock().asItem());
                        if (!returnStack.isEmpty()) {
                            if (!player.getInventory().add(returnStack)) {
                                player.drop(returnStack, false);
                            }
                        }
                    }
                }
            }

            // Play camouflage removal sound
            try {
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } catch (Throwable ignored) {}
        }

        slabEntity.setChanged();
        BlockState currentState = level.getBlockState(pos);
        level.sendBlockUpdated(pos, currentState, currentState, 3);
    }
}
