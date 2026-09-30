package net.openslabs.elevatorslabs.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;

/**
 * Tag keys used by Elevator Slabs for cross-mod compatibility.
 */
public final class ModTags {

    private ModTags() {}

    public static final class Blocks {
        private Blocks() {}

        /**
         * Tag from OpenBlocks Elevator for recognizing all elevator blocks.
         */
        public static final TagKey<Block> ELEVATORID_ELEVATORS = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("elevatorid", "elevators")
        );

        /**
         * Common convention tag for elevator blocks across mods.
         */
        public static final TagKey<Block> COMMON_ELEVATORS = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("c", "elevators")
        );

        /**
         * Tag specific to elevator slab blocks from this addon.
         */
        public static final TagKey<Block> ELEVATOR_SLABS = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "elevator_slabs")
        );
    }

    public static final class Items {
        private Items() {}

        public static final TagKey<Item> ELEVATORID_ELEVATORS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("elevatorid", "elevators")
        );

        public static final TagKey<Item> COMMON_ELEVATORS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "elevators")
        );

        public static final TagKey<Item> ELEVATOR_SLABS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "elevator_slabs")
        );
    }
}
