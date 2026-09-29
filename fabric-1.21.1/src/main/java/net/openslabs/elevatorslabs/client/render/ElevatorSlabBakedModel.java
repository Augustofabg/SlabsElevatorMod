package net.openslabs.elevatorslabs.client.render;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

import java.util.function.Supplier;

/**
 * Custom Fabric ForwardingBakedModel for Elevator Slabs.
 * Delegates rendering to the camouflaged slab model when disguised,
 * strictly preventing recursive self-rendering and StackOverflowError.
 */
public class ElevatorSlabBakedModel extends ForwardingBakedModel {

    public ElevatorSlabBakedModel(BakedModel originalModel) {
        this.wrapped = unwrap(originalModel);
    }

    private static BakedModel unwrap(BakedModel model) {
        while (model instanceof ElevatorSlabBakedModel wrapper) {
            model = wrapper.wrapped;
        }
        return model;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    private BlockState adaptCamoState(BlockState camoState, BlockState elevatorState) {
        if (camoState == null || elevatorState == null) {
            return camoState;
        }
        if (elevatorState.hasProperty(SlabBlock.TYPE) && camoState.hasProperty(SlabBlock.TYPE)) {
            camoState = camoState.setValue(SlabBlock.TYPE, elevatorState.getValue(SlabBlock.TYPE));
        }
        if (elevatorState.hasProperty(SlabBlock.WATERLOGGED) && camoState.hasProperty(SlabBlock.WATERLOGGED)) {
            camoState = camoState.setValue(SlabBlock.WATERLOGGED, elevatorState.getValue(SlabBlock.WATERLOGGED));
        }
        return camoState;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                               Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (state == null) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }

        BlockEntity be = blockView.getBlockEntity(pos);
        if (!(be instanceof ElevatorSlabBlockEntity slabTile)) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }

        SlabType type = state.hasProperty(SlabBlock.TYPE) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;
        BlockState camoBottom = slabTile.getCamouflagedBottom();
        BlockState camoTop = slabTile.getCamouflagedTop();

        if (type == SlabType.BOTTOM) {
            if (camoBottom != null) {
                emitCamoQuads(blockView, camoBottom, state, pos, randomSupplier, context);
                return;
            }
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }

        if (type == SlabType.TOP) {
            if (camoTop != null) {
                emitCamoQuads(blockView, camoTop, state, pos, randomSupplier, context);
                return;
            }
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }

        // SlabType.DOUBLE:
        if (camoBottom == null && camoTop == null) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }

        // If covered by a full block that has no slab variant (e.g. Wool, Concrete, Obsidian):
        if (camoBottom != null && camoBottom.equals(camoTop) && !camoBottom.hasProperty(SlabBlock.TYPE)) {
            emitCamoQuads(blockView, camoBottom, state, pos, randomSupplier, context);
            return;
        }

        // 1. Bottom half (Y: 0.0 to 0.5)
        BlockState bottomState = state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        context.pushTransform(ElevatorSlabBakedModel::isBottomHalfQuad);
        if (camoBottom != null) {
            emitCamoQuads(blockView, camoBottom, bottomState, pos, randomSupplier, context);
        } else {
            super.emitBlockQuads(blockView, bottomState, pos, randomSupplier, context);
        }
        context.popTransform();

        // 2. Top half (Y: 0.5 to 1.0)
        BlockState topState = state.setValue(SlabBlock.TYPE, SlabType.TOP);
        context.pushTransform(ElevatorSlabBakedModel::isTopHalfQuad);
        if (camoTop != null) {
            emitCamoQuads(blockView, camoTop, topState, pos, randomSupplier, context);
        } else {
            super.emitBlockQuads(blockView, topState, pos, randomSupplier, context);
        }
        context.popTransform();
    }

    private void emitCamoQuads(BlockAndTintGetter blockView, BlockState camo, BlockState elevatorState,
                               BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        BlockState adapted = adaptCamoState(camo, elevatorState);
        BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(adapted);

        if (camoModel != null && camoModel != this && !(camoModel instanceof ElevatorSlabBakedModel)) {
            camoModel.emitBlockQuads(blockView, adapted, pos, randomSupplier, context);
        } else {
            super.emitBlockQuads(blockView, elevatorState, pos, randomSupplier, context);
        }
    }

    private static boolean isBottomHalfQuad(MutableQuadView quad) {
        float y0 = quad.y(0);
        float y1 = quad.y(1);
        float y2 = quad.y(2);
        float y3 = quad.y(3);

        if (Math.abs(y0 - 0.5f) < 0.01f && Math.abs(y1 - 0.5f) < 0.01f
                && Math.abs(y2 - 0.5f) < 0.01f && Math.abs(y3 - 0.5f) < 0.01f) {
            return false;
        }

        return y0 <= 0.501f && y1 <= 0.501f && y2 <= 0.501f && y3 <= 0.501f;
    }

    private static boolean isTopHalfQuad(MutableQuadView quad) {
        float y0 = quad.y(0);
        float y1 = quad.y(1);
        float y2 = quad.y(2);
        float y3 = quad.y(3);

        if (Math.abs(y0 - 0.5f) < 0.01f && Math.abs(y1 - 0.5f) < 0.01f
                && Math.abs(y2 - 0.5f) < 0.01f && Math.abs(y3 - 0.5f) < 0.01f) {
            return false;
        }

        return y0 >= 0.499f && y1 >= 0.499f && y2 >= 0.499f && y3 >= 0.499f;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }
}
