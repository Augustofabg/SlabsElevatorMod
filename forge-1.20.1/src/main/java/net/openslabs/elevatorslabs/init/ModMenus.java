package net.openslabs.elevatorslabs.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.menu.ElevatorOptionsMenu;

/**
 * Registers MenuTypes for GUI containers for Forge 1.20.1.
 */
public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ElevatorSlabsMod.MOD_ID);

    public static final RegistryObject<MenuType<ElevatorOptionsMenu>> ELEVATOR_OPTIONS_MENU =
            MENUS.register("elevator_options", () ->
                    IForgeMenuType.create((containerId, playerInv, extraData) ->
                            new ElevatorOptionsMenu(containerId, playerInv, extraData.readBlockPos())
                    )
            );

    private ModMenus() {}
}
