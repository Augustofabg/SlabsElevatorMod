package net.openslabs.elevatorslabs.network;

import com.vsngarcia.neoforge.init.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

/**
 * Server-side handler for UpdateSlabOptionsPayload.
 * Applies directional, arrow visibility, facing rotation, and camouflage reset to the BlockEntity.
 */
public final class UpdateSlabOptionsHandler {

    private UpdateSlabOptionsHandler() {}

    public static void handle(UpdateSlabOptionsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.isAlive()) {
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

            // Apply directional and orientation
            slabEntity.setDirectional(payload.directional());
            slabEntity.setHideArrow(payload.hideArrow());
            slabEntity.setFacing(payload.facing());

            // Handle camouflage reset if requested
            if (payload.resetCamo()) {
                BlockState oldBottom = slabEntity.getCamouflagedBottom();
                BlockState oldTop = slabEntity.getCamouflagedTop();
                if (oldBottom != null || oldTop != null) {
                    slabEntity.clearAllCamo();
                    if (!player.isCreative()) {
                        if (oldBottom != null) {
                            ItemStack returnStack = new ItemStack(oldBottom.getBlock().asItem());
                            if (!returnStack.isEmpty()) {
                                if (!player.getInventory().add(returnStack)) {
                                    player.drop(returnStack, false);
                                }
                            }
                        }
                        if (oldTop != null) {
                            ItemStack returnStack = new ItemStack(oldTop.getBlock().asItem());
                            if (!returnStack.isEmpty()) {
                                if (!player.getInventory().add(returnStack)) {
                                    player.drop(returnStack, false);
                                }
                            }
                        }
                    }
                    try {
                        level.playSound(null, pos, Registry.CAMOUFLAGE_SOUND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    } catch (Throwable ignored) {}
                }
            }

            slabEntity.setChanged();
            BlockState currentState = level.getBlockState(pos);
            level.sendBlockUpdated(pos, currentState, currentState, 3);
        });
    }
}
