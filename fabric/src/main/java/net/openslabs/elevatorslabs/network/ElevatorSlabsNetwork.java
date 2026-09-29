package net.openslabs.elevatorslabs.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers network payloads and server-side handlers for Fabric 1.21.1.
 */
public final class ElevatorSlabsNetwork {

    private ElevatorSlabsNetwork() {}

    public static void register() {
        // Register client-to-server payload types
        PayloadTypeRegistry.playC2S().register(
                TeleportSlabPayload.TYPE,
                TeleportSlabPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                UpdateSlabOptionsPayload.TYPE,
                UpdateSlabOptionsPayload.STREAM_CODEC
        );

        // Register server-side receivers
        ServerPlayNetworking.registerGlobalReceiver(
                TeleportSlabPayload.TYPE,
                (payload, context) -> context.server().execute(() -> TeleportSlabHandler.handle(payload, context.player()))
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateSlabOptionsPayload.TYPE,
                (payload, context) -> context.server().execute(() -> UpdateSlabOptionsHandler.handle(payload, context.player()))
        );
    }
}
