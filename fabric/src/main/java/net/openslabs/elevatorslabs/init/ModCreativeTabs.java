package net.openslabs.elevatorslabs.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;

/**
 * Creative Mode Tab for Elevator Slabs addon on Fabric 1.21.1.
 */
public final class ModCreativeTabs {

    public static CreativeModeTab ELEVATOR_SLABS_TAB;

    public static void register() {
        ELEVATOR_SLABS_TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "elevator_slabs_tab"),
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup." + ElevatorSlabsMod.MOD_ID))
                        .icon(() -> new ItemStack(ModItems.getByColor(DyeColor.WHITE)))
                        .displayItems((parameters, output) -> {
                            output.accept(ModItems.ENDER_SPINDLE);
                            for (DyeColor color : DyeColor.values()) {
                                output.accept(ModItems.getByColor(color));
                            }
                        })
                        .build()
        );
    }

    private ModCreativeTabs() {}
}
