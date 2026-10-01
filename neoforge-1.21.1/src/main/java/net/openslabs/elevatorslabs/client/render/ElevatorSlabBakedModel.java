package net.openslabs.elevatorslabs.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom BakedModel wrapper for Elevator Slabs in NeoForge 1.21.1.
 * Delivers quads strictly partitioned by SlabType (BOTTOM, TOP, DOUBLE)
 * and caches results keyed by (SlabType, bottomCamouflage, topCamouflage, direction, renderType).
 */
public class ElevatorSlabBakedModel extends BakedModelWrapper<BakedModel> {

    private final BakedModel baseModel;
    private final Map<CacheKey, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public static final class CacheKey {
        private final SlabType slabType;
        @Nullable
        private final BlockState bottomCamouflage;
        @Nullable
        private final BlockState topCamouflage;
        private final boolean appliedAsFullBlock;
        @Nullable
        private final Direction direction;
        @Nullable
        private final RenderType renderType;
        private final int hashCode;

        public CacheKey(SlabType slabType, @Nullable BlockState bottomCamouflage, @Nullable BlockState topCamouflage,
                        boolean appliedAsFullBlock, @Nullable Direction direction, @Nullable RenderType renderType) {
            this.slabType = slabType;
            this.bottomCamouflage = bottomCamouflage;
            this.topCamouflage = topCamouflage;
            this.appliedAsFullBlock = appliedAsFullBlock;
            this.direction = direction;
            this.renderType = renderType;
            this.hashCode = Objects.hash(slabType, bottomCamouflage, topCamouflage, appliedAsFullBlock, direction, renderType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey other)) return false;
            return this.slabType == other.slabType
                    && Objects.equals(this.bottomCamouflage, other.bottomCamouflage)
                    && Objects.equals(this.topCamouflage, other.topCamouflage)
                    && this.appliedAsFullBlock == other.appliedAsFullBlock
                    && this.direction == other.direction
                    && Objects.equals(this.renderType, other.renderType);
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }
    }

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
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData modelData, @Nullable RenderType renderType) {
        if (state == null) {
            return this.baseModel.getQuads(state, side, rand, ModelData.EMPTY, renderType);
        }

        // LÓGICA OBRIGATÓRIA DE SELEÇÃO DE QUADS:
        SlabType slabType = state.hasProperty(SlabBlock.TYPE) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;
        BlockState bottomCamouflage = modelData.get(ElevatorSlabBlockEntity.CAMOUFLAGE_BOTTOM_PROPERTY);
        if (bottomCamouflage == null) {
            bottomCamouflage = modelData.get(ElevatorSlabBlockEntity.CAMO_BOTTOM);
        }
        BlockState topCamouflage = modelData.get(ElevatorSlabBlockEntity.CAMOUFLAGE_TOP_PROPERTY);
        if (topCamouflage == null) {
            topCamouflage = modelData.get(ElevatorSlabBlockEntity.CAMO_TOP);
        }
        Boolean appliedAsFull = modelData.get(ElevatorSlabBlockEntity.APPLIED_AS_FULL_BLOCK);
        boolean appliedAsFullBlock = appliedAsFull != null && appliedAsFull;

        CacheKey key = new CacheKey(slabType, bottomCamouflage, topCamouflage, appliedAsFullBlock, side, renderType);
        List<BakedQuad> cached = this.quadCache.get(key);
        if (cached != null) {
            return cached;
        }

        List<BakedQuad> quads = new ArrayList<>();

        // CASO 1: LAJE INFERIOR (BOTTOM)
        if (slabType == SlabType.BOTTOM) {
            if (bottomCamouflage != null && !bottomCamouflage.isAir()) {
                // Renderiza a laje do bloco camuflado na metade inferior (Y: 0.0 a 0.5)
                quads.addAll(getTransformedQuads(bottomCamouflage, SlabType.BOTTOM, side, rand, modelData, renderType));
            } else {
                // Renderiza a Elevator Slab branca padrão na metade inferior
                quads.addAll(getBaseSlabQuads(state, SlabType.BOTTOM, side, rand, renderType));
            }
        }
        // CASO 2: LAJE SUPERIOR (TOP)
        else if (slabType == SlabType.TOP) {
            if (topCamouflage != null && !topCamouflage.isAir()) {
                // Renderiza a laje do bloco camuflado na metade superior (Y: 0.5 a 1.0)
                quads.addAll(getTransformedQuads(topCamouflage, SlabType.TOP, side, rand, modelData, renderType));
            } else {
                // Renderiza a Elevator Slab branca padrão na metade superior
                quads.addAll(getBaseSlabQuads(state, SlabType.TOP, side, rand, renderType));
            }
        }
        // CASO 3: LAJE DUPLA (DOUBLE)
        else if (slabType == SlabType.DOUBLE) {
            Block fullBlock = modelData.get(ElevatorSlabBlockEntity.FULL_BLOCK_SOURCE);
            if (Boolean.TRUE.equals(appliedAsFull) && fullBlock != null) {
                BlockState fullState = fullBlock.defaultBlockState();
                BakedModel fullModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(fullState);
                if (fullModel != null && fullModel != this && !(fullModel instanceof ElevatorSlabBakedModel)) {
                    List<BakedQuad> fullQuads = fullModel.getQuads(fullState, side, rand, ModelData.EMPTY, renderType);
                    this.quadCache.put(key, fullQuads);
                    return fullQuads;
                }
            }

            // Metade inferior
            if (bottomCamouflage != null && !bottomCamouflage.isAir()) {
                quads.addAll(getTransformedQuads(bottomCamouflage, SlabType.BOTTOM, side, rand, modelData, renderType));
            } else {
                quads.addAll(getBaseSlabQuads(state, SlabType.BOTTOM, side, rand, renderType));
            }
            // Metade superior
            if (topCamouflage != null && !topCamouflage.isAir()) {
                quads.addAll(getTransformedQuads(topCamouflage, SlabType.TOP, side, rand, modelData, renderType));
            } else {
                quads.addAll(getBaseSlabQuads(state, SlabType.TOP, side, rand, renderType));
            }
        }

        this.quadCache.put(key, quads);
        return quads;
    }

    private List<BakedQuad> getBaseSlabQuads(BlockState state, SlabType half, @Nullable Direction side,
                                             RandomSource rand, @Nullable RenderType renderType) {
        BlockState halfState = state.setValue(SlabBlock.TYPE, half);
        BakedModel targetModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(halfState);
        BakedModel unwrapped = unwrap(targetModel);
        if (unwrapped != null && unwrapped != this) {
            return unwrapped.getQuads(halfState, side, rand, ModelData.EMPTY, renderType);
        }
        return this.baseModel.getQuads(halfState, side, rand, ModelData.EMPTY, renderType);
    }

    private List<BakedQuad> getTransformedQuads(BlockState camoState, SlabType half,
                                                @Nullable Direction side, RandomSource rand,
                                                ModelData modelData, @Nullable RenderType renderType) {
        if (camoState == null || camoState.isAir()) {
            return List.of();
        }

        BlockState adapted = adaptCamoState(camoState, half);
        BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(adapted);

        if (camoModel != null && camoModel != this && !(camoModel instanceof ElevatorSlabBakedModel)) {
            List<BakedQuad> quads = camoModel.getQuads(adapted, side, rand, ModelData.EMPTY, renderType);
            if (!adapted.hasProperty(SlabBlock.TYPE)) {
                List<BakedQuad> filtered = new ArrayList<>();
                for (BakedQuad q : quads) {
                    if (half == SlabType.BOTTOM && isBottomHalfQuad(q)) {
                        filtered.add(q);
                    } else if (half == SlabType.TOP && isTopHalfQuad(q)) {
                        filtered.add(q);
                    }
                }
                return filtered;
            }
            return quads;
        }

        return List.of();
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

        return y0 >= 0.499f && y1 >= 0.499f && y2 >= 0.499f && y3 >= 0.499f;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState camo = data.get(ElevatorSlabBlockEntity.CAMOUFLAGE_BOTTOM_PROPERTY);
        if (camo == null) camo = data.get(ElevatorSlabBlockEntity.CAMO_BOTTOM);
        if (camo == null) camo = data.get(ElevatorSlabBlockEntity.CAMOUFLAGE_TOP_PROPERTY);
        if (camo == null) camo = data.get(ElevatorSlabBlockEntity.CAMO_TOP);
        if (camo == null) camo = data.get(ElevatorSlabBlockEntity.CAMO_STATE);

        if (camo != null && !camo.isAir()) {
            BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camo);
            if (camoModel != null && camoModel != this && !(camoModel instanceof ElevatorSlabBakedModel)) {
                return camoModel.getParticleIcon(ModelData.EMPTY);
            }
        }
        return this.baseModel.getParticleIcon(ModelData.EMPTY);
    }

    private ChunkRenderTypeSet getCamoRenderTypes(BlockState camoState, SlabType half, RandomSource rand) {
        BlockState adapted = adaptCamoState(camoState, half);
        BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(adapted);
        if (camoModel != null && camoModel != this && !(camoModel instanceof ElevatorSlabBakedModel)) {
            return camoModel.getRenderTypes(adapted, rand, ModelData.EMPTY);
        }
        return ChunkRenderTypeSet.none();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        SlabType slabType = (state != null && state.hasProperty(SlabBlock.TYPE)) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;

        BlockState bottomCamouflage = data.get(ElevatorSlabBlockEntity.CAMOUFLAGE_BOTTOM_PROPERTY);
        if (bottomCamouflage == null) bottomCamouflage = data.get(ElevatorSlabBlockEntity.CAMO_BOTTOM);

        BlockState topCamouflage = data.get(ElevatorSlabBlockEntity.CAMOUFLAGE_TOP_PROPERTY);
        if (topCamouflage == null) topCamouflage = data.get(ElevatorSlabBlockEntity.CAMO_TOP);

        ChunkRenderTypeSet types = ChunkRenderTypeSet.none();
        boolean needsBase = false;

        if (slabType == SlabType.BOTTOM) {
            if (bottomCamouflage != null && !bottomCamouflage.isAir()) {
                types = ChunkRenderTypeSet.union(types, getCamoRenderTypes(bottomCamouflage, SlabType.BOTTOM, rand));
            } else {
                needsBase = true;
            }
        } else if (slabType == SlabType.TOP) {
            if (topCamouflage != null && !topCamouflage.isAir()) {
                types = ChunkRenderTypeSet.union(types, getCamoRenderTypes(topCamouflage, SlabType.TOP, rand));
            } else {
                needsBase = true;
            }
        } else { // DOUBLE
            Boolean appliedAsFull = data.get(ElevatorSlabBlockEntity.APPLIED_AS_FULL_BLOCK);
            Block fullBlock = data.get(ElevatorSlabBlockEntity.FULL_BLOCK_SOURCE);
            if (Boolean.TRUE.equals(appliedAsFull) && fullBlock != null) {
                BlockState fullState = fullBlock.defaultBlockState();
                BakedModel fullModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(fullState);
                if (fullModel != null && fullModel != this && !(fullModel instanceof ElevatorSlabBakedModel)) {
                    return fullModel.getRenderTypes(fullState, rand, ModelData.EMPTY);
                }
            }

            if (bottomCamouflage != null && !bottomCamouflage.isAir()) {
                types = ChunkRenderTypeSet.union(types, getCamoRenderTypes(bottomCamouflage, SlabType.BOTTOM, rand));
            } else {
                needsBase = true;
            }

            if (topCamouflage != null && !topCamouflage.isAir()) {
                types = ChunkRenderTypeSet.union(types, getCamoRenderTypes(topCamouflage, SlabType.TOP, rand));
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
