package net.openslabs.elevatorslabs.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;
import net.openslabs.elevatorslabs.util.ElevatorSearchHelper;

import java.util.EnumSet;
import java.util.function.Supplier;

public class TeleportSlabPacket {

    private final BlockPos from;
    private final BlockPos to;

    public TeleportSlabPacket(BlockPos from, BlockPos to) {
        this.from = from;
        this.to = to;
    }

    public static void encode(TeleportSlabPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.from);
        buf.writeBlockPos(msg.to);
    }

    public static TeleportSlabPacket decode(FriendlyByteBuf buf) {
        return new TeleportSlabPacket(buf.readBlockPos(), buf.readBlockPos());
    }

    public static void handle(TeleportSlabPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.isAlive() || player.isSpectator()) {
                return;
            }

            ServerLevel level = player.serverLevel();
            BlockPos fromPos = msg.from;
            BlockPos toPos = msg.to;

            if (!level.isLoaded(fromPos) || !level.isLoaded(toPos)) {
                return;
            }

            if (player.distanceToSqr(Vec3.atCenterOf(fromPos)) > 9.0D) {
                return;
            }

            if (fromPos.getX() != toPos.getX() || fromPos.getZ() != toPos.getZ() || fromPos.getY() == toPos.getY()) {
                return;
            }

            BlockState fromState = level.getBlockState(fromPos);
            BlockState toState = level.getBlockState(toPos);

            if (!ElevatorSearchHelper.isElevator(fromState) || !ElevatorSearchHelper.isElevator(toState)) {
                return;
            }

            DyeColor fromColor = ElevatorSearchHelper.getColor(fromState);
            DyeColor toColor = ElevatorSearchHelper.getColor(toState);
            if (fromColor != null && toColor != null && fromColor != toColor) {
                return;
            }

            double targetYOffset = ElevatorSearchHelper.getYOffset(toState);
            double destX = toPos.getX() + 0.5D;
            double destY = toPos.getY() + targetYOffset;
            double destZ = toPos.getZ() + 0.5D;

            if (!ElevatorSearchHelper.isSafeDestination(level, player, destX, destY, destZ, toPos)) {
                return;
            }

            float yaw = player.getYRot();
            float pitch = player.getXRot();

            if (toState.getBlock() instanceof ElevatorSlabBlock) {
                if (level.getBlockEntity(toPos) instanceof ElevatorSlabBlockEntity slabEntity
                        && slabEntity.isDirectional()) {
                    yaw = slabEntity.getFacing().toYRot();
                    pitch = 0.0F;
                }
            }

            player.teleportTo(level, destX, destY, destZ, EnumSet.noneOf(RelativeMovement.class), yaw, pitch);
            player.setDeltaMovement(player.getDeltaMovement().multiply(new Vec3(1.0D, 0.0D, 1.0D)));
            level.playSound(null, toPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        });
        ctx.get().setPacketHandled(true);
    }
}
