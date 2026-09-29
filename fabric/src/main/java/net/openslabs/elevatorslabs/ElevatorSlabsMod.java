package net.openslabs.elevatorslabs;

import net.fabricmc.api.ModInitializer;
import net.openslabs.elevatorslabs.init.ModBlockEntities;
import net.openslabs.elevatorslabs.init.ModBlocks;
import net.openslabs.elevatorslabs.init.ModCreativeTabs;
import net.openslabs.elevatorslabs.init.ModItems;
import net.openslabs.elevatorslabs.init.ModMenus;
import net.openslabs.elevatorslabs.network.ElevatorSlabsNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for Elevator Slabs addon on Fabric 1.21.1.
 */
public class ElevatorSlabsMod implements ModInitializer {

    public static final String MOD_ID = "elevatorslabs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Elevator Slabs addon for Fabric 1.21.1...");

        // Register Blocks and Items
        ModBlocks.register();
        ModItems.register();

        // Register Block Entities
        ModBlockEntities.register();

        // Register Menus
        ModMenus.register();

        // Register Creative Tab
        ModCreativeTabs.register();

        // Register Networking Payloads & Handlers
        ElevatorSlabsNetwork.register();

        // Register Block Interactions
        net.openslabs.elevatorslabs.event.ElevatorInteractionHandler.register();

        LOGGER.info("Elevator Slabs initialized successfully with 16 color variants on Fabric 1.21.1.");
    }
}
