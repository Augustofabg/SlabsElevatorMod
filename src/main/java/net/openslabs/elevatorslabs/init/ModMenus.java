package net.openslabs.elevatorslabs.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.menu.ElevatorOptionsMenu;

/**
 * Registers MenuTypes for GUI containers.
 */
public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ElevatorSlabsMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ElevatorOptionsMenu>> ELEVATOR_OPTIONS_MENU =
            MENUS.register("elevator_options", () ->
                    IMenuTypeExtension.create((containerId, playerInv, extraData) ->
                            new ElevatorOptionsMenu(containerId, playerInv, extraData.readBlockPos())
                    )
            );

    private ModMenus() {}
}
