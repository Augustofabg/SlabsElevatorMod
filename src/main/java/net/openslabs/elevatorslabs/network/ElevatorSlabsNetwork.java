package net.openslabs.elevatorslabs.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;

/**
 * Registers custom network payloads with NeoForge's PayloadRegistrar.
 */
@EventBusSubscriber(modid = ElevatorSlabsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ElevatorSlabsNetwork {

    private ElevatorSlabsNetwork() {}

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                TeleportSlabPayload.TYPE,
                TeleportSlabPayload.STREAM_CODEC,
                TeleportSlabHandler::handle
        );

        registrar.playToServer(
                UpdateSlabOptionsPayload.TYPE,
                UpdateSlabOptionsPayload.STREAM_CODEC,
                UpdateSlabOptionsHandler::handle
        );
    }
}
