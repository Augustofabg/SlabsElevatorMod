package net.openslabs.elevatorslabs.client.render;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
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
 * Custom Fabric FabricBakedModel (ForwardingBakedModel) for Elevator Slabs.
 * Delivers quads strictly partitioned by SlabType (BOTTOM, TOP, DOUBLE).
 *
 * Data flow:
 * 1. Primary: RenderAttachedBlockView attachment (FRAPI native channel, fastest)
 * 2. Fallback: Direct BlockEntity query (covers transitions and chunk boundary cases)
 *
 * BOTTOM → emits only bottom-half quads
 * TOP    → emits only top-half quads
 * DOUBLE → emits bottom + top without overlap; full-block camo emits full model
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

    /**
     * Adapts a camouflage BlockState to a specific slab half.
     * If the camouflage is itself a slab, sets its TYPE property.
     * If it has a corresponding slab, uses that slab's state.
     * Otherwise returns the state as-is (full block camouflage).
     */
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

        // === Step 1: Obtain render data ===
        BlockState bottomCamouflage = null;
        BlockState topCamouflage = null;
        boolean appliedAsFullBlock = false;
        Block fullBlockSource = null;

        // PRIMARY CHANNEL: FRAPI RenderAttachedBlockView
        if (blockView instanceof RenderAttachedBlockView renderView) {
            Object attachment = renderView.getBlockEntityRenderAttachment(pos);
            if (attachment instanceof ElevatorRenderData data) {
                bottomCamouflage = data.bottomCamo();
                topCamouflage = data.topCamo();
                appliedAsFullBlock = data.appliedAsFullBlock();
                fullBlockSource = data.fullBlockSource();
            }
        }

        // FALLBACK CHANNEL: Direct BlockEntity access
        // ALWAYS cross-check with the live BlockEntity to avoid stale attachment data.
        // The attachment snapshot may lag behind the actual BE state during chunk rebuilds
        // triggered by setBlock/sendBlockUpdated in onPlayerBreakBlock.
        BlockEntity be = blockView.getBlockEntity(pos);
        if (be instanceof ElevatorSlabBlockEntity elevatorBe) {
            bottomCamouflage = elevatorBe.getCamouflagedBottomState();
            topCamouflage = elevatorBe.getCamouflagedTopState();
            appliedAsFullBlock = elevatorBe.isAppliedAsFullBlock();
            fullBlockSource = elevatorBe.getFullBlockSource();
        }

        // === Step 2: Emit quads based on SlabType ===
        SlabType slabType = state.hasProperty(SlabBlock.TYPE) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;

        // CASE 1: BOTTOM SLAB – emit exclusively bottom-half quads
        if (slabType == SlabType.BOTTOM) {
            if (bottomCamouflage != null && !bottomCamouflage.isAir()) {
                emitTransformedQuads(blockView, bottomCamouflage, SlabType.BOTTOM, pos, randomSupplier, context, state);
            } else {
                emitBaseSlabQuads(blockView, state, SlabType.BOTTOM, pos, randomSupplier, context);
            }
            return;
        }

        // CASE 2: TOP SLAB – emit exclusively top-half quads
        if (slabType == SlabType.TOP) {
            if (topCamouflage != null && !topCamouflage.isAir()) {
                emitTransformedQuads(blockView, topCamouflage, SlabType.TOP, pos, randomSupplier, context, state);
            } else {
                emitBaseSlabQuads(blockView, state, SlabType.TOP, pos, randomSupplier, context);
            }
            return;
        }

        // CASE 3: DOUBLE SLAB – emit both halves without overlap
        if (slabType == SlabType.DOUBLE) {
            // Full block camouflage applied: render full model
            if (appliedAsFullBlock && fullBlockSource != null) {
                BlockState fullState = fullBlockSource.defaultBlockState();
                BakedModel fullModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(fullState);
                if (fullModel != null && fullModel != this && !(fullModel instanceof ElevatorSlabBakedModel)) {
                    if (fullModel instanceof FabricBakedModel fabricModel && !fabricModel.isVanillaAdapter()) {
                        fabricModel.emitBlockQuads(blockView, fullState, pos, randomSupplier, context);
                    } else {
                        fullModel.emitBlockQuads(blockView, fullState, pos, randomSupplier, context);
                    }
                    return;
                }
            }

            // Bottom half
            if (bottomCamouflage != null && !bottomCamouflage.isAir()) {
                emitTransformedQuads(blockView, bottomCamouflage, SlabType.BOTTOM, pos, randomSupplier, context, state);
            } else {
                emitBaseSlabQuads(blockView, state, SlabType.BOTTOM, pos, randomSupplier, context);
            }

            // Top half
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
                // Full-block camouflage: filter quads by Y coordinate to get only the correct half
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
                // Slab camouflage: already partitioned correctly by the adapted SlabType
                camoModel.emitBlockQuads(blockView, adapted, pos, randomSupplier, context);
            }
        } else {
            // Fallback: render base elevator slab texture for this half
            emitBaseSlabQuads(blockView, elevatorState, half, pos, randomSupplier, context);
        }
    }

    /**
     * Quad transform filter: accepts quads entirely in the bottom half (Y <= 0.501).
     * Uses OR logic to include side-face quads that span both halves at the junction.
     */
    private static boolean isBottomHalfQuad(MutableQuadView quad) {
        float y0 = quad.y(0);
        float y1 = quad.y(1);
        float y2 = quad.y(2);
        float y3 = quad.y(3);
        float maxY = Math.max(Math.max(y0, y1), Math.max(y2, y3));
        float minY = Math.min(Math.min(y0, y1), Math.min(y2, y3));
        // Accept if the quad's top is at or below the midpoint
        return maxY <= 0.501f || (minY < 0.499f && maxY <= 0.501f);
    }

    /**
     * Quad transform filter: accepts quads entirely in the top half (Y >= 0.499).
     * Uses OR logic to include side-face quads that span both halves at the junction.
     */
    private static boolean isTopHalfQuad(MutableQuadView quad) {
        float y0 = quad.y(0);
        float y1 = quad.y(1);
        float y2 = quad.y(2);
        float y3 = quad.y(3);
        float minY = Math.min(Math.min(y0, y1), Math.min(y2, y3));
        float maxY = Math.max(Math.max(y0, y1), Math.max(y2, y3));
        // Accept if the quad's bottom is at or above the midpoint
        return minY >= 0.499f || (maxY > 0.501f && minY >= 0.499f);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }
}
