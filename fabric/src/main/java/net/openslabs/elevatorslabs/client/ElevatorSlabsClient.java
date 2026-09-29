package net.openslabs.elevatorslabs.client;

import com.vsngarcia.fabric.ElevatorBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;
import net.openslabs.elevatorslabs.client.gui.ElevatorOptionsScreen;
import net.openslabs.elevatorslabs.client.render.ElevatorSlabBER;
import net.openslabs.elevatorslabs.init.ModBlockEntities;
import net.openslabs.elevatorslabs.init.ModBlocks;
import net.openslabs.elevatorslabs.init.ModMenus;

/**
 * Client entry point for Elevator Slabs on Fabric 1.21.1.
 */
public class ElevatorSlabsClient implements ClientModInitializer {

    private static final ThreadLocal<Boolean> COLOR_RECURSION_GUARD = ThreadLocal.withInitial(() -> false);

    @Override
    public void onInitializeClient() {
        // Register GUI Screen
        MenuScreens.register(ModMenus.ELEVATOR_OPTIONS_MENU, ElevatorOptionsScreen::new);

        // Register Block Entity Renderer (Directional Arrow)
        BlockEntityRenderers.register(ModBlockEntities.ELEVATOR_SLAB_BLOCK_ENTITY, ElevatorSlabBER::new);

        // Register Client Input Handler (Jump/Sneak Teleport)
        ElevatorSlabsClientHandler.register();

        // Register Block Color Handlers (Camouflage block tinting)
        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> {
            if (COLOR_RECURSION_GUARD.get() || level == null || pos == null) {
                return -1;
            }

            try {
                COLOR_RECURSION_GUARD.set(true);

                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ElevatorSlabBlockEntity tile) {
                    BlockState camo = tile.getCamouflagedBlock();
                    if (camo != null) {
                        if (camo.getBlock() instanceof ElevatorSlabBlock
                                || camo.getBlock() instanceof ElevatorBlock
                                || camo.getBlock() == state.getBlock()) {
                            return -1;
                        }

                        return Minecraft.getInstance().getBlockColors().getColor(camo, level, pos, tintIndex);
                    }
                }
            } catch (Throwable ignored) {
                return -1;
            } finally {
                COLOR_RECURSION_GUARD.set(false);
            }

            return -1;
        }, ModBlocks.getAllSlabs());
    }
}
