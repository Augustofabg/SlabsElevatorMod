package net.openslabs.elevatorslabs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;

/**
 * Network payload sent from the client to the server when elevator options are modified in the GUI.
 */
public record UpdateSlabOptionsPayload(
        BlockPos pos,
        boolean directional,
        boolean hideArrow,
        Direction facing,
        boolean resetCamo
) implements CustomPacketPayload {

    public static final Type<UpdateSlabOptionsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "update_slab_options"));

    private static final StreamCodec<ByteBuf, Direction> DIRECTION_CODEC = StreamCodec.of(
            (buf, dir) -> buf.writeByte(dir.get3DDataValue()),
            buf -> Direction.from3DDataValue(buf.readByte())
    );

    public static final StreamCodec<ByteBuf, UpdateSlabOptionsPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateSlabOptionsPayload::pos,
            ByteBufCodecs.BOOL, UpdateSlabOptionsPayload::directional,
            ByteBufCodecs.BOOL, UpdateSlabOptionsPayload::hideArrow,
            DIRECTION_CODEC, UpdateSlabOptionsPayload::facing,
            ByteBufCodecs.BOOL, UpdateSlabOptionsPayload::resetCamo,
            UpdateSlabOptionsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
