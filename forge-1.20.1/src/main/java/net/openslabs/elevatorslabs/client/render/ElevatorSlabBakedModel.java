package net.openslabs.elevatorslabs.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Custom BakedModel wrapper for Elevator Slabs.
 * Delegates rendering to the camouflaged slab model when disguised,
 * strictly preventing recursive self-rendering and StackOverflowError.
 */
public class ElevatorSlabBakedModel extends BakedModelWrapper<BakedModel> {

    private final BakedModel baseModel;

    public ElevatorSlabBakedModel(BakedModel originalModel) {
        super(unwrap(originalModel));
        this.baseModel = unwrap(originalModel);
    }

    private static BakedModel unwrap(BakedModel model) {
        while (model instanceof ElevatorSlabBakedModel wrapper) {
            model = wrapper.baseModel;
        }
        return model;
    }

    private BlockState adaptCamoState(BlockState camoState, @Nullable BlockState elevatorState) {
        if (camoState == null || elevatorState == null) {
            return camoState;
        }
        // Preserve slab half (BOTTOM, TOP, DOUBLE) and waterlogging
        if (elevatorState.hasProperty(SlabBlock.TYPE) && camoState.hasProperty(SlabBlock.TYPE)) {
            camoState = camoState.setValue(SlabBlock.TYPE, elevatorState.getValue(SlabBlock.TYPE));
        }
        if (elevatorState.hasProperty(SlabBlock.WATERLOGGED) && camoState.hasProperty(SlabBlock.WATERLOGGED)) {
            camoState = camoState.setValue(SlabBlock.WATERLOGGED, elevatorState.getValue(SlabBlock.WATERLOGGED));
        }
        return camoState;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData extraData, @Nullable RenderType renderType) {
        if (state == null) {
            return this.baseModel.getQuads(state, side, rand, ModelData.EMPTY, renderType);
        }

        SlabType type = state.hasProperty(SlabBlock.TYPE) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;

        BlockState camoBottom = extraData.get(ElevatorSlabBlockEntity.CAMO_BOTTOM);
        BlockState camoTop = extraData.get(ElevatorSlabBlockEntity.CAMO_TOP);

        if (type == SlabType.BOTTOM) {
            if (camoBottom != null) {
                return getCamoQuads(camoBottom, state, side, rand, renderType);
            }
            return this.baseModel.getQuads(state, side, rand, ModelData.EMPTY, renderType);
        }

        if (type == SlabType.TOP) {
            if (camoTop != null) {
                return getCamoQuads(camoTop, state, side, rand, renderType);
            }
            return this.baseModel.getQuads(state, side, rand, ModelData.EMPTY, renderType);
        }

        // SlabType.DOUBLE:
        if (camoBottom == null && camoTop == null) {
            return this.baseModel.getQuads(state, side, rand, ModelData.EMPTY, renderType);
        }

        Boolean appliedAsFull = extraData.get(ElevatorSlabBlockEntity.APPLIED_AS_FULL_BLOCK);
        net.minecraft.world.level.block.Block fullBlock = extraData.get(ElevatorSlabBlockEntity.FULL_BLOCK_SOURCE);
        if (Boolean.TRUE.equals(appliedAsFull)) {
            if (fullBlock != null) {
                return getCamoQuads(fullBlock.defaultBlockState(), state, side, rand, renderType);
            }
            if (camoBottom != null && camoBottom.equals(camoTop)) {
                return getCamoQuads(camoBottom, state, side, rand, renderType);
            }
        }

        // If covered by a full block that has no slab variant (e.g. Wool, Concrete, Obsidian):
        if (camoBottom != null && camoBottom.equals(camoTop) && !camoBottom.hasProperty(SlabBlock.TYPE)) {
            return getCamoQuads(camoBottom, state, side, rand, renderType);
        }

        // Render split halves with strict partitioning and no co-planar overlapping faces:
        java.util.ArrayList<BakedQuad> combined = new java.util.ArrayList<>();

        // 1. Bottom half (Y: 0.0 to 0.5)
        BlockState bottomState = state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        List<BakedQuad> bottomQuads;
        if (camoBottom != null) {
            bottomQuads = getCamoQuads(camoBottom, bottomState, side, rand, renderType);
        } else {
            bottomQuads = getOriginalSlabQuads(bottomState, side, rand, renderType);
        }
        for (BakedQuad q : bottomQuads) {
            if (isBottomHalfQuad(q)) {
                combined.add(q);
            }
        }

        // 2. Top half (Y: 0.5 to 1.0)
        BlockState topState = state.setValue(SlabBlock.TYPE, SlabType.TOP);
        List<BakedQuad> topQuads;
        if (camoTop != null) {
            topQuads = getCamoQuads(camoTop, topState, side, rand, renderType);
        } else {
            topQuads = getOriginalSlabQuads(topState, side, rand, renderType);
        }
        for (BakedQuad q : topQuads) {
            if (isTopHalfQuad(q)) {
                combined.add(q);
            }
        }

        return combined;
    }

    private List<BakedQuad> getOriginalSlabQuads(BlockState slabState, @Nullable Direction side,
                                                 RandomSource rand, @Nullable RenderType renderType) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(slabState);
        BakedModel unwrapped = unwrap(model);
        if (unwrapped != null && unwrapped != this) {
            return unwrapped.getQuads(slabState, side, rand, ModelData.EMPTY, renderType);
        }
        return List.of();
    }

    private List<BakedQuad> getCamoQuads(BlockState camo, BlockState elevatorState, @Nullable Direction side,
                                         RandomSource rand, @Nullable RenderType renderType) {
        BlockState adapted = adaptCamoState(camo, elevatorState);
        BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(adapted);

        if (camoModel != null && camoModel != this && !(camoModel instanceof ElevatorSlabBakedModel)) {
            List<BakedQuad> quads = camoModel.getQuads(adapted, side, rand, ModelData.EMPTY, renderType);
            if (!adapted.hasProperty(SlabBlock.TYPE) && elevatorState.hasProperty(SlabBlock.TYPE)) {
                SlabType type = elevatorState.getValue(SlabBlock.TYPE);
                if (type == SlabType.BOTTOM) {
                    List<BakedQuad> filtered = new java.util.ArrayList<>();
                    for (BakedQuad q : quads) {
                        if (isBottomHalfQuad(q)) filtered.add(q);
                    }
                    return filtered;
                } else if (type == SlabType.TOP) {
                    List<BakedQuad> filtered = new java.util.ArrayList<>();
                    for (BakedQuad q : quads) {
                        if (isTopHalfQuad(q)) filtered.add(q);
                    }
                    return filtered;
                }
            }
            return quads;
        }
        return getOriginalSlabQuads(elevatorState, side, rand, renderType);
    }

    private static boolean isBottomHalfQuad(BakedQuad quad) {
        int[] vertices = quad.getVertices();
        if (vertices.length < 32) {
            return false;
        }
        float y0 = Float.intBitsToFloat(vertices[1]);
        float y1 = Float.intBitsToFloat(vertices[9]);
        float y2 = Float.intBitsToFloat(vertices[17]);
        float y3 = Float.intBitsToFloat(vertices[25]);

        // Discard internal horizontal dividing face at y ≈ 0.5
        if (Math.abs(y0 - 0.5f) < 0.01f && Math.abs(y1 - 0.5f) < 0.01f
                && Math.abs(y2 - 0.5f) < 0.01f && Math.abs(y3 - 0.5f) < 0.01f) {
            return false;
        }

        // Must be in bottom half: y <= 0.501
        return y0 <= 0.501f && y1 <= 0.501f && y2 <= 0.501f && y3 <= 0.501f;
    }

    private static boolean isTopHalfQuad(BakedQuad quad) {
        int[] vertices = quad.getVertices();
        if (vertices.length < 32) {
            return false;
        }
        float y0 = Float.intBitsToFloat(vertices[1]);
        float y1 = Float.intBitsToFloat(vertices[9]);
        float y2 = Float.intBitsToFloat(vertices[17]);
        float y3 = Float.intBitsToFloat(vertices[25]);

        // Discard internal horizontal dividing face at y ≈ 0.5
        if (Math.abs(y0 - 0.5f) < 0.01f && Math.abs(y1 - 0.5f) < 0.01f
                && Math.abs(y2 - 0.5f) < 0.01f && Math.abs(y3 - 0.5f) < 0.01f) {
            return false;
        }

        // Must be in top half: y >= 0.499
        return y0 >= 0.499f && y1 >= 0.499f && y2 >= 0.499f && y3 >= 0.499f;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState camo = data.get(ElevatorSlabBlockEntity.CAMO_BOTTOM);
        if (camo == null) {
            camo = data.get(ElevatorSlabBlockEntity.CAMO_TOP);
        }
        if (camo == null) {
            camo = data.get(ElevatorSlabBlockEntity.CAMO_STATE);
        }
        if (camo != null) {
            BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camo);
            if (camoModel != null && camoModel != this && !(camoModel instanceof ElevatorSlabBakedModel)) {
                return camoModel.getParticleIcon(ModelData.EMPTY);
            }
        }
        return this.baseModel.getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        SlabType type = (state != null && state.hasProperty(SlabBlock.TYPE)) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;
        BlockState camoBottom = data.get(ElevatorSlabBlockEntity.CAMO_BOTTOM);
        BlockState camoTop = data.get(ElevatorSlabBlockEntity.CAMO_TOP);

        ChunkRenderTypeSet types = ChunkRenderTypeSet.none();
        boolean needsBase = false;

        if (type == SlabType.BOTTOM) {
            if (camoBottom != null) {
                BlockState adapted = adaptCamoState(camoBottom, state);
                BakedModel m = Minecraft.getInstance().getBlockRenderer().getBlockModel(adapted);
                if (m != null && m != this && !(m instanceof ElevatorSlabBakedModel)) {
                    types = ChunkRenderTypeSet.union(types, m.getRenderTypes(adapted, rand, ModelData.EMPTY));
                } else {
                    needsBase = true;
                }
            } else {
                needsBase = true;
            }
        } else if (type == SlabType.TOP) {
            if (camoTop != null) {
                BlockState adapted = adaptCamoState(camoTop, state);
                BakedModel m = Minecraft.getInstance().getBlockRenderer().getBlockModel(adapted);
                if (m != null && m != this && !(m instanceof ElevatorSlabBakedModel)) {
                    types = ChunkRenderTypeSet.union(types, m.getRenderTypes(adapted, rand, ModelData.EMPTY));
                } else {
                    needsBase = true;
                }
            } else {
                needsBase = true;
            }
        } else { // DOUBLE
            Boolean appliedAsFull = data.get(ElevatorSlabBlockEntity.APPLIED_AS_FULL_BLOCK);
            net.minecraft.world.level.block.Block fullBlock = data.get(ElevatorSlabBlockEntity.FULL_BLOCK_SOURCE);
            if (Boolean.TRUE.equals(appliedAsFull)) {
                BlockState fullState = fullBlock != null ? fullBlock.defaultBlockState() : (camoBottom != null ? camoBottom : camoTop);
                if (fullState != null) {
                    BakedModel m = Minecraft.getInstance().getBlockRenderer().getBlockModel(fullState);
                    if (m != null && m != this && !(m instanceof ElevatorSlabBakedModel)) {
                        types = ChunkRenderTypeSet.union(types, m.getRenderTypes(fullState, rand, ModelData.EMPTY));
                    } else {
                        needsBase = true;
                    }
                    if (needsBase || types.isEmpty()) {
                        types = ChunkRenderTypeSet.union(types, this.baseModel.getRenderTypes(state, rand, ModelData.EMPTY));
                    }
                    return types;
                }
            }

            if (camoBottom != null) {
                BakedModel m = Minecraft.getInstance().getBlockRenderer().getBlockModel(camoBottom);
                if (m != null && m != this && !(m instanceof ElevatorSlabBakedModel)) {
                    types = ChunkRenderTypeSet.union(types, m.getRenderTypes(camoBottom, rand, ModelData.EMPTY));
                } else {
                    needsBase = true;
                }
            } else {
                needsBase = true;
            }

            if (camoTop != null) {
                BakedModel m = Minecraft.getInstance().getBlockRenderer().getBlockModel(camoTop);
                if (m != null && m != this && !(m instanceof ElevatorSlabBakedModel)) {
                    types = ChunkRenderTypeSet.union(types, m.getRenderTypes(camoTop, rand, ModelData.EMPTY));
                } else {
                    needsBase = true;
                }
            } else {
                needsBase = true;
            }
        }

        if (needsBase || types.isEmpty()) {
            types = ChunkRenderTypeSet.union(types, this.baseModel.getRenderTypes(state, rand, ModelData.EMPTY));
        }
        return types;
    }
}
