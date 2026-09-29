package net.openslabs.elevatorslabs;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.openslabs.elevatorslabs.init.ModBlockEntities;
import net.openslabs.elevatorslabs.init.ModBlocks;
import net.openslabs.elevatorslabs.init.ModCreativeTabs;
import net.openslabs.elevatorslabs.init.ModItems;
import net.openslabs.elevatorslabs.init.ModMenus;
import net.openslabs.elevatorslabs.network.ElevatorSlabsNetwork;
import org.slf4j.Logger;

/**
 * Main entry point for Elevator Slabs addon on Minecraft Forge 1.20.1.
 */
@Mod(ElevatorSlabsMod.MOD_ID)
public class ElevatorSlabsMod {

    public static final String MOD_ID = "elevatorslabs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ElevatorSlabsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LOGGER.info("Initializing Elevator Slabs addon for Forge 1.20.1...");

        // Register Blocks and Items
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        // Register Block Entities
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // Register Menus
        ModMenus.MENUS.register(modEventBus);

        // Register Creative Tab
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Register Networking
        ElevatorSlabsNetwork.register();

        LOGGER.info("Elevator Slabs initialized successfully with 16 color variants for Forge 1.20.1.");
    }
}
