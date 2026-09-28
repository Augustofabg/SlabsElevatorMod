package net.openslabs.elevatorslabs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;

/**
 * Payload sent from client to server requesting vertical elevator teleportation.
 */
public record TeleportSlabPayload(BlockPos from, BlockPos to) implements CustomPacketPayload {

    public static final Type<TeleportSlabPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "teleport_slab"));

    public static final StreamCodec<ByteBuf, TeleportSlabPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TeleportSlabPayload::from,
            BlockPos.STREAM_CODEC, TeleportSlabPayload::to,
            TeleportSlabPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
