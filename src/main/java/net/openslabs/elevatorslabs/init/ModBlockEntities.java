package net.openslabs.elevatorslabs.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

/**
 * Registers BlockEntityTypes for Elevator Slabs.
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ElevatorSlabsMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElevatorSlabBlockEntity>> ELEVATOR_SLAB_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("elevator_slab", () ->
                    BlockEntityType.Builder.of(
                            ElevatorSlabBlockEntity::new,
                            ModBlocks.SLABS_BY_COLOR.values().stream().map(DeferredBlock::get).toArray(Block[]::new)
                    ).build(null)
            );

    private ModBlockEntities() {}
}
