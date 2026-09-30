package net.openslabs.elevatorslabs.client.render;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Render data record passed from ElevatorSlabBlockEntity to ElevatorSlabBakedModel
 * via the FRAPI RenderAttachmentBlockEntity mechanism.
 */
public record ElevatorRenderData(
    @Nullable BlockState bottomCamo,
    @Nullable BlockState topCamo,
    boolean appliedAsFullBlock,
    @Nullable Block fullBlockSource
) {}
