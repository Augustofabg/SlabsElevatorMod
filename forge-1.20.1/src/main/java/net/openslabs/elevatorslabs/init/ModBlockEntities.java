package net.openslabs.elevatorslabs.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

/**
 * Registers BlockEntityTypes for Elevator Slabs for Forge 1.20.1.
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ElevatorSlabsMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<ElevatorSlabBlockEntity>> ELEVATOR_SLAB_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("elevator_slab", () ->
                    BlockEntityType.Builder.of(
                            ElevatorSlabBlockEntity::new,
                            ModBlocks.SLABS_BY_COLOR.values().stream().map(RegistryObject<ElevatorSlabBlock>::get).toArray(Block[]::new)
                    ).build(null)
            );

    private ModBlockEntities() {}
}
