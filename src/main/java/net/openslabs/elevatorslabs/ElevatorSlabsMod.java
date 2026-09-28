package net.openslabs.elevatorslabs;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.openslabs.elevatorslabs.init.ModBlockEntities;
import net.openslabs.elevatorslabs.init.ModBlocks;
import net.openslabs.elevatorslabs.init.ModCreativeTabs;
import net.openslabs.elevatorslabs.init.ModItems;
import net.openslabs.elevatorslabs.init.ModMenus;
import org.slf4j.Logger;

/**
 * Main entry point for Elevator Slabs addon.
 */
@Mod(ElevatorSlabsMod.MOD_ID)
public class ElevatorSlabsMod {

    public static final String MOD_ID = "elevatorslabs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ElevatorSlabsMod(IEventBus modEventBus) {
        LOGGER.info("Initializing Elevator Slabs addon for NeoForge 1.21.1...");

        // Register Blocks and Items
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        // Register Block Entities
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // Register Menus
        ModMenus.MENUS.register(modEventBus);

        // Register Creative Tab
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        LOGGER.info("Elevator Slabs initialized successfully with 16 color variants.");
    }
}
