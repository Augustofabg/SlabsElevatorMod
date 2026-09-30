package net.openslabs.elevatorslabs.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;

/**
 * Creative Mode Tab for Elevator Slabs addon on Fabric 1.21.1.
 */
public final class ModCreativeTabs {

    public static void register() {
        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup." + ElevatorSlabsMod.MOD_ID))
                .icon(() -> new ItemStack(ModBlocks.getByColor(net.minecraft.world.item.DyeColor.WHITE)))
                .displayItems((parameters, output) -> {
                    output.accept(ModItems.ENDER_SPINDLE);
                    ModItems.getSlabItemsByColor().values().forEach(output::accept);
                })
                .build();

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "elevator_slabs_tab"),
                tab
        );
    }

    private ModCreativeTabs() {}
}
