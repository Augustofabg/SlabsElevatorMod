package net.openslabs.elevatorslabs.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

import java.util.function.Supplier;

public class UpdateSlabOptionsPacket {

    private final BlockPos pos;
    private final boolean directional;
    private final boolean hideArrow;
    private final Direction facing;
    private final boolean resetCamo;

    public UpdateSlabOptionsPacket(BlockPos pos, boolean directional, boolean hideArrow, Direction facing, boolean resetCamo) {
        this.pos = pos;
        this.directional = directional;
        this.hideArrow = hideArrow;
        this.facing = facing;
        this.resetCamo = resetCamo;
    }

    public static void encode(UpdateSlabOptionsPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.directional);
        buf.writeBoolean(msg.hideArrow);
        buf.writeEnum(msg.facing);
        buf.writeBoolean(msg.resetCamo);
    }

    public static UpdateSlabOptionsPacket decode(FriendlyByteBuf buf) {
        return new UpdateSlabOptionsPacket(
                buf.readBlockPos(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readEnum(Direction.class),
                buf.readBoolean()
        );
    }

    public static void handle(UpdateSlabOptionsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.isAlive()) {
                return;
            }

            ServerLevel level = player.serverLevel();
            BlockPos pos = msg.pos;

            if (!level.isLoaded(pos) || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof ElevatorSlabBlockEntity slabEntity)) {
                return;
            }

            slabEntity.setDirectional(msg.directional);
            slabEntity.setHideArrow(msg.hideArrow);
            slabEntity.setFacing(msg.facing);

            if (msg.resetCamo) {
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
                    level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }

            slabEntity.setChanged();
            BlockState currentState = level.getBlockState(pos);
            level.sendBlockUpdated(pos, currentState, currentState, 3);
        });
        ctx.get().setPacketHandled(true);
    }
}
