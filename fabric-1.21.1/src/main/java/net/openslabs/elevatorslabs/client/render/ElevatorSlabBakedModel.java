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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

import java.util.function.Supplier;

/**
 * Custom Fabric ForwardingBakedModel for Elevator Slabs.
 * Delegates rendering to camouflaged slab models partitioned strictly by SlabType (BOTTOM, TOP, DOUBLE).
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

    private BlockState adaptCamoState(BlockState camoState, SlabType half) {
        if (camoState == null) {
            return null;
        }
        if (camoState.hasProperty(SlabBlock.TYPE)) {
            return camoState.setValue(SlabBlock.TYPE, half);
        }
        SlabBlock slab = ElevatorSlabBlock.findCorrespondingSlab(camoState.getBlock());
        if (slab != null) {
            return slab.defaultBlockState().setValue(SlabBlock.TYPE, half);
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

        SlabType slabType = state.hasProperty(SlabBlock.TYPE) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;
        BlockState bottomCamouflage = slabTile.getCamouflagedBottomState();
        BlockState topCamouflage = slabTile.getCamouflagedTopState();

        // CASO 1: LAJE INFERIOR (BOTTOM)
        if (slabType == SlabType.BOTTOM) {
            if (bottomCamouflage != null && !bottomCamouflage.isAir()) {
                emitTransformedQuads(blockView, bottomCamouflage, SlabType.BOTTOM, pos, randomSupplier, context, state);
            } else {
                emitBaseSlabQuads(blockView, state, SlabType.BOTTOM, pos, randomSupplier, context);
            }
            return;
        }

        // CASO 2: LAJE SUPERIOR (TOP)
        else if (slabType == SlabType.TOP) {
            if (topCamouflage != null && !topCamouflage.isAir()) {
                emitTransformedQuads(blockView, topCamouflage, SlabType.TOP, pos, randomSupplier, context, state);
            } else {
                emitBaseSlabQuads(blockView, state, SlabType.TOP, pos, randomSupplier, context);
            }
            return;
        }

        // CASO 3: LAJE DUPLA (DOUBLE)
        else if (slabType == SlabType.DOUBLE) {
            if (slabTile.isAppliedAsFullBlock()) {
                Block fullBlock = slabTile.getFullBlockSource();
                if (fullBlock != null) {
                    BlockState fullState = fullBlock.defaultBlockState();
                    BakedModel fullModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(fullState);
                    if (fullModel != null && fullModel != this && !(fullModel instanceof ElevatorSlabBakedModel)) {
                        fullModel.emitBlockQuads(blockView, fullState, pos, randomSupplier, context);
                        return;
                    }
                }
            }

            // Metade inferior
            if (bottomCamouflage != null && !bottomCamouflage.isAir()) {
                emitTransformedQuads(blockView, bottomCamouflage, SlabType.BOTTOM, pos, randomSupplier, context, state);
            } else {
                emitBaseSlabQuads(blockView, state, SlabType.BOTTOM, pos, randomSupplier, context);
            }

            // Metade superior
            if (topCamouflage != null && !topCamouflage.isAir()) {
                emitTransformedQuads(blockView, topCamouflage, SlabType.TOP, pos, randomSupplier, context, state);
            } else {
                emitBaseSlabQuads(blockView, state, SlabType.TOP, pos, randomSupplier, context);
            }
        }
    }

    private void emitBaseSlabQuads(BlockAndTintGetter blockView, BlockState elevatorState, SlabType half,
                                   BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        BlockState slabState = elevatorState.setValue(SlabBlock.TYPE, half);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(slabState);
        BakedModel unwrapped = unwrap(model);
        if (unwrapped != null && unwrapped != this) {
            unwrapped.emitBlockQuads(blockView, slabState, pos, randomSupplier, context);
        } else {
            this.wrapped.emitBlockQuads(blockView, slabState, pos, randomSupplier, context);
        }
    }

    private void emitTransformedQuads(BlockAndTintGetter blockView, BlockState camoState, SlabType half,
                                      BlockPos pos, Supplier<RandomSource> randomSupplier,
                                      RenderContext context, BlockState elevatorState) {
        BlockState adapted = adaptCamoState(camoState, half);
        BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(adapted);

        if (camoModel != null && camoModel != this && !(camoModel instanceof ElevatorSlabBakedModel)) {
            if (!adapted.hasProperty(SlabBlock.TYPE)) {
                if (half == SlabType.BOTTOM) {
                    context.pushTransform(ElevatorSlabBakedModel::isBottomHalfQuad);
                    camoModel.emitBlockQuads(blockView, adapted, pos, randomSupplier, context);
                    context.popTransform();
                } else {
                    context.pushTransform(ElevatorSlabBakedModel::isTopHalfQuad);
                    camoModel.emitBlockQuads(blockView, adapted, pos, randomSupplier, context);
                    context.popTransform();
                }
            } else {
                camoModel.emitBlockQuads(blockView, adapted, pos, randomSupplier, context);
            }
        } else {
            emitBaseSlabQuads(blockView, elevatorState, half, pos, randomSupplier, context);
        }
    }

    private static boolean isBottomHalfQuad(MutableQuadView quad) {
        float y0 = quad.y(0);
        float y1 = quad.y(1);
        float y2 = quad.y(2);
        float y3 = quad.y(3);

        return y0 <= 0.501f && y1 <= 0.501f && y2 <= 0.501f && y3 <= 0.501f;
    }

    private static boolean isTopHalfQuad(MutableQuadView quad) {
        float y0 = quad.y(0);
        float y1 = quad.y(1);
        float y2 = quad.y(2);
        float y3 = quad.y(3);

        return y0 >= 0.499f && y1 >= 0.499f && y2 >= 0.499f && y3 >= 0.499f;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }
}
