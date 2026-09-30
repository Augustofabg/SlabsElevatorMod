package net.openslabs.elevatorslabs.network;

import com.vsngarcia.fabric.ElevatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.util.ElevatorSearchHelper;

import java.util.EnumSet;

/**
 * Server-side handler for elevator slab teleportation requests on Fabric 1.21.1.
 * Ported from NeoForge: ElevatorBlock (Fabric) replaces ElevatorBlockBase (NeoForge).
 * The DIRECTIONAL block property is accessed via ElevatorBlock.DIRECTIONAL (static field).
 */
public final class TeleportSlabHandler {

    private TeleportSlabHandler() {}

    public static void handle(TeleportSlabPayload message, ServerPlayer player) {
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos fromPos = message.from();
        BlockPos toPos = message.to();

        // Ensure chunks are loaded
        if (!level.isLoaded(fromPos) || !level.isLoaded(toPos)) {
            return;
        }

        // Security check: Player must be within range of origin elevator
        if (player.distanceToSqr(Vec3.atCenterOf(fromPos)) > 9.0D) {
            return;
        }

        // Elevators must share the same X and Z coordinates
        if (fromPos.getX() != toPos.getX() || fromPos.getZ() != toPos.getZ() || fromPos.getY() == toPos.getY()) {
            return;
        }

        BlockState fromState = level.getBlockState(fromPos);
        BlockState toState = level.getBlockState(toPos);

        if (!ElevatorSearchHelper.isElevator(fromState) || !ElevatorSearchHelper.isElevator(toState)) {
            return;
        }

        // Precision destination calculation
        double targetYOffset = ElevatorSearchHelper.getYOffset(toState);
        double destX = toPos.getX() + 0.5D;
        double destY = toPos.getY() + targetYOffset;
        double destZ = toPos.getZ() + 0.5D;

        // Collision and suffocation verification
        if (!ElevatorSearchHelper.isSafeDestination(level, player, destX, destY, destZ, toPos)) {
            return;
        }

        // Directional rotation calculation
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        if (toState.getBlock() instanceof ElevatorSlabBlock) {
            if (level.getBlockEntity(toPos) instanceof net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity slabEntity
                    && slabEntity.isDirectional()) {
                yaw = slabEntity.getFacing().toYRot();
                pitch = 0.0F;
            }
        } else if (toState.getBlock() instanceof ElevatorBlock) {
            // ElevatorBlock (Fabric) uses DIRECTIONAL BooleanProperty and FACING
            try {
                if (toState.hasProperty(ElevatorBlock.DIRECTIONAL)
                        && toState.getValue(ElevatorBlock.DIRECTIONAL)
                        && toState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                    yaw = toState.getValue(HorizontalDirectionalBlock.FACING).toYRot();
                    pitch = 0.0F;
                }
            } catch (Throwable ignored) {}
        }

        // Teleport player safely to target
        player.teleportTo(level, destX, destY, destZ, EnumSet.noneOf(RelativeMovement.class), yaw, pitch);

        // Reset vertical momentum to prevent unintended velocity carryover
        player.setDeltaMovement(player.getDeltaMovement().multiply(new Vec3(1.0D, 0.0D, 1.0D)));

        // Play elevator sound effect
        try {
            level.playSound(null, toPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        } catch (Throwable ignored) {}
    }
}
