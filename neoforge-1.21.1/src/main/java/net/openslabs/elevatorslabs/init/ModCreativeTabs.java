package net.openslabs.elevatorslabs.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;

/**
 * Creative Mode Tab for Elevator Slabs addon.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ElevatorSlabsMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ELEVATOR_SLABS_TAB =
            CREATIVE_MODE_TABS.register("elevator_slabs_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ElevatorSlabsMod.MOD_ID))
                    .icon(() -> new ItemStack(ModItems.ELEVATOR_SLAB_WHITE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ENDER_SPINDLE.get());
                        ModItems.SLAB_ITEMS_BY_COLOR.values().forEach(item -> output.accept(item.get()));
                    })
                    .build()
            );

    private ModCreativeTabs() {}
}
