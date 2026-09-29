package net.openslabs.elevatorslabs.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.item.EnderSpindleItem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registers BlockItems for all 16 Elevator Slab colors on Fabric 1.21.1.
 */
public final class ModItems {

    private static final Map<DyeColor, BlockItem> ITEMS_MAP = new EnumMap<>(DyeColor.class);
    public static EnderSpindleItem ENDER_SPINDLE;

    public static void register() {
        ENDER_SPINDLE = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "ender_spindle"),
                new EnderSpindleItem(new Item.Properties())
        );

        for (DyeColor color : DyeColor.values()) {
            String name = "elevator_slab_" + color.getName();
            BlockItem item = new BlockItem(ModBlocks.getByColor(color), new Item.Properties());
            Registry.register(
                    BuiltInRegistries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, name),
                    item
            );
            ITEMS_MAP.put(color, item);
        }
    }

    public static BlockItem getByColor(DyeColor color) {
        return ITEMS_MAP.get(color);
    }

    public static Map<DyeColor, BlockItem> getItemsByColor() {
        return Collections.unmodifiableMap(ITEMS_MAP);
    }

    private ModItems() {}
}
