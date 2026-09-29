package net.openslabs.elevatorslabs.init;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.menu.ElevatorOptionsMenu;

/**
 * Registers MenuTypes for GUI containers on Fabric 1.21.1.
 */
public final class ModMenus {

    public static MenuType<ElevatorOptionsMenu> ELEVATOR_OPTIONS_MENU;

    public static void register() {
        ELEVATOR_OPTIONS_MENU = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "elevator_options"),
                new ExtendedScreenHandlerType<>((containerId, playerInv, payload) ->
                        new ElevatorOptionsMenu(containerId, playerInv, payload.pos()),
                        ElevatorOptionsMenu.PosPayload.PACKET_CODEC
                )
        );
    }

    private ModMenus() {}
}
