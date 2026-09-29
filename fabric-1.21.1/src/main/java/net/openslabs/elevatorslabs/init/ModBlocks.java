package net.openslabs.elevatorslabs.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registers all 16 color variants of Elevator Slabs for Fabric 1.21.1.
 */
public final class ModBlocks {

    private static final Map<DyeColor, ElevatorSlabBlock> SLABS_MAP = new EnumMap<>(DyeColor.class);

    public static void register() {
        for (DyeColor color : DyeColor.values()) {
            String name = "elevator_slab_" + color.getName();
            ElevatorSlabBlock block = new ElevatorSlabBlock(color);
            Registry.register(
                    BuiltInRegistries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, name),
                    block
            );
            SLABS_MAP.put(color, block);
        }
    }

    public static ElevatorSlabBlock getByColor(DyeColor color) {
        return SLABS_MAP.get(color);
    }

    public static Map<DyeColor, ElevatorSlabBlock> getSlabsByColor() {
        return Collections.unmodifiableMap(SLABS_MAP);
    }

    public static Block[] getAllSlabs() {
        return SLABS_MAP.values().toArray(new Block[0]);
    }

    private ModBlocks() {}
}
