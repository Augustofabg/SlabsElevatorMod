package net.openslabs.elevatorslabs.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;

/**
 * Registers network packets using Forge's SimpleChannel on 1.20.1.
 */
public final class ElevatorSlabsNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ElevatorSlabsMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ElevatorSlabsNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                TeleportSlabPacket.class,
                TeleportSlabPacket::encode,
                TeleportSlabPacket::decode,
                TeleportSlabPacket::handle
        );
        CHANNEL.registerMessage(
                id++,
                UpdateSlabOptionsPacket.class,
                UpdateSlabOptionsPacket::encode,
                UpdateSlabOptionsPacket::decode,
                UpdateSlabOptionsPacket::handle
        );
    }
}
