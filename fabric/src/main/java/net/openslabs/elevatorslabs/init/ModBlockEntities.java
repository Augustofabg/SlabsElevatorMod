package net.openslabs.elevatorslabs.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

/**
 * Registers BlockEntityTypes for Elevator Slabs on Fabric 1.21.1.
 */
public final class ModBlockEntities {

    public static BlockEntityType<ElevatorSlabBlockEntity> ELEVATOR_SLAB_BLOCK_ENTITY;

    public static void register() {
        ELEVATOR_SLAB_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "elevator_slab"),
                FabricBlockEntityTypeBuilder.create(
                        ElevatorSlabBlockEntity::new,
                        ModBlocks.getAllSlabs()
                ).build()
        );
    }

    private ModBlockEntities() {}
}
