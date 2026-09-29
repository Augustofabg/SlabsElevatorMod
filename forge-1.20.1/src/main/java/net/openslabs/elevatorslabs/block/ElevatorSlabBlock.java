package net.openslabs.elevatorslabs.block;

import xyz.vsngamer.elevatorid.blocks.ElevatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base block for Elevator Slabs on Forge 1.20.1.
 */
public class ElevatorSlabBlock extends SlabBlock implements EntityBlock {

    private final DyeColor color;

    private void openOptionsScreen(Player player, ElevatorSlabBlockEntity slabTile, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, slabTile, pos);
        }
    }

    public ElevatorSlabBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
    }

    public ElevatorSlabBlock(DyeColor color) {
        this(color, BlockBehaviour.Properties.of()
                .mapColor(color)
                .sound(SoundType.WOOL)
                .strength(0.8F)
                .noOcclusion()
                .dynamicShape()
        );
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElevatorSlabBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        SlabType type = state.getValue(TYPE);
        switch (type) {
            case DOUBLE:
                if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
                    if (player.isCrouching()) {
                        return Shapes.block();
                    }
                    Vec3 eyePos = player.getEyePosition();
                    Vec3 viewVec = player.getViewVector(1.0F);
                    Vec3 endPos = eyePos.add(viewVec.scale(10.0D));
                    AABB box = new AABB(pos);
                    Optional<Vec3> hit = box.clip(eyePos, endPos);
                    if (hit.isPresent()) {
                        double localY = hit.get().y - pos.getY();
                        if (localY >= 0.55D) {
                            return TOP_AABB;
                        } else if (localY <= 0.45D) {
                            return BOTTOM_AABB;
                        } else {
                            return Shapes.block();
                        }
                    }
                }
                return Shapes.block();
            case TOP:
                return TOP_AABB;
            default:
                return BOTTOM_AABB;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        SlabType type = state.getValue(TYPE);
        switch (type) {
            case DOUBLE:
                return Shapes.block();
            case TOP:
                return TOP_AABB;
            default:
                return BOTTOM_AABB;
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ElevatorSlabBlockEntity slabTile) {
                if (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
                    boolean crouching = player.isCrouching();
                    double localY = getHitY(level, pos, player);
                    if (crouching || (localY > 0.45D && localY < 0.55D) || slabTile.isAppliedAsFullBlock()) {
                        slabTile.clearAllCamo();
                    } else if (localY <= 0.45D) {
                        slabTile.setCamouflagedBottom(null);
                    } else {
                        slabTile.setCamouflagedTop(null);
                    }
                } else {
                    slabTile.clearAllCamo();
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
            boolean crouching = player.isCrouching();
            double localY = getHitY(level, pos, player);

            if (!crouching) {
                if (localY < 0.5D) {
                    // Lower half selected: break only bottom slab
                    SlabType remainingType = SlabType.TOP;
                    BlockState remainingState = state.setValue(SlabBlock.TYPE, remainingType);

                    if (!level.isClientSide) {
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof ElevatorSlabBlockEntity slabTile) {
                            if (!player.isCreative()) {
                                // Drop 1 elevator slab
                                popResource(level, pos, new ItemStack(this));

                                if (slabTile.isAppliedAsFullBlock()) {
                                    // Cenário 1: Drop 1 bloco completo original (NÃO dropar lajes)
                                    ItemStack fullDrop = slabTile.getFullBlockDropStack();
                                    if (!fullDrop.isEmpty()) {
                                        popResource(level, pos, fullDrop);
                                    }
                                } else {
                                    // Cenário 2: Drop 1 laje correspondente à metade quebrada
                                    BlockState camo = slabTile.getCamouflagedBottomState();
                                    if (camo != null) {
                                        popResource(level, pos, new ItemStack(camo.getBlock().asItem()));
                                    }
                                }
                            }

                            if (slabTile.isAppliedAsFullBlock()) {
                                // Cenário 1: A metade superior perde a camuflagem e appliedAsFullBlock vira false
                                slabTile.clearAllCamo();
                            } else {
                                // Cenário 2: Metade restante mantém camuflagem intacta
                                slabTile.setCamouflagedBottomState(null);
                            }
                        }

                        level.setBlock(pos, remainingState, 3);
                        level.levelEvent(player, 2001, pos, Block.getId(state));
                    } else {
                        level.setBlock(pos, remainingState, 11);
                    }

                    return false;
                } else {
                    // Upper half selected: break only top slab
                    SlabType remainingType = SlabType.BOTTOM;
                    BlockState remainingState = state.setValue(SlabBlock.TYPE, remainingType);

                    if (!level.isClientSide) {
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof ElevatorSlabBlockEntity slabTile) {
                            if (!player.isCreative()) {
                                // Drop 1 elevator slab
                                popResource(level, pos, new ItemStack(this));

                                if (slabTile.isAppliedAsFullBlock()) {
                                    // Cenário 1: Drop 1 bloco completo original (NÃO dropar lajes)
                                    ItemStack fullDrop = slabTile.getFullBlockDropStack();
                                    if (!fullDrop.isEmpty()) {
                                        popResource(level, pos, fullDrop);
                                    }
                                } else {
                                    // Cenário 2: Drop 1 laje correspondente à metade quebrada
                                    BlockState camo = slabTile.getCamouflagedTopState();
                                    if (camo != null) {
                                        popResource(level, pos, new ItemStack(camo.getBlock().asItem()));
                                    }
                                }
                            }

                            if (slabTile.isAppliedAsFullBlock()) {
                                // Cenário 1: A metade inferior perde a camuflagem e appliedAsFullBlock vira false
                                slabTile.clearAllCamo();
                            } else {
                                // Cenário 2: Metade restante mantém camuflagem intacta
                                slabTile.setCamouflagedTopState(null);
                            }
                        }

                        level.setBlock(pos, remainingState, 3);
                        level.levelEvent(player, 2001, pos, Block.getId(state));
                    } else {
                        level.setBlock(pos, remainingState, 11);
                    }

                    return false;
                }
            }
            // Full block selected (crouching): break both halves together, drops handled in onRemove
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ElevatorSlabBlockEntity slabTile) {
                    if (slabTile.isAppliedAsFullBlock()) {
                        // Cenário 1: quebrar ambas as metades dropa 1 bloco completo original
                        ItemStack fullDrop = slabTile.getFullBlockDropStack();
                        if (!fullDrop.isEmpty()) {
                            popResource(level, pos, fullDrop);
                        }
                    } else {
                        // Cenário 2: quebrar ambas as metades dropa as respectivas lajes
                        BlockState bottomCamo = slabTile.getCamouflagedBottomState();
                        if (bottomCamo != null) {
                            popResource(level, pos, new ItemStack(bottomCamo.getBlock().asItem()));
                        }
                        BlockState topCamo = slabTile.getCamouflagedTopState();
                        if (topCamo != null) {
                            popResource(level, pos, new ItemStack(topCamo.getBlock().asItem()));
                        }
                    }
                    slabTile.clearAllCamo();
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    protected double getHitY(Level level, BlockPos pos, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(viewVec.scale(10.0D));
        AABB box = new AABB(pos);
        Optional<Vec3> hit = box.clip(eyePos, endPos);
        if (hit.isPresent()) {
            return hit.get().y - pos.getY();
        }
        return (eyePos.y < pos.getY() + 0.5D) ? 0.25D : 0.75D;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ElevatorSlabBlockEntity slabTile)) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        SlabType slabType = state.hasProperty(SlabBlock.TYPE) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;

        // If holding matching slab on a single slab without sneaking, allow placing second slab:
        if (!player.isShiftKeyDown() && slabType != SlabType.DOUBLE && itemStack.is(this.asItem())) {
            return InteractionResult.PASS;
        }

        // ==========================================================
        // 1. Shift + Right Click (Remoção de Camuflagem):
        // ==========================================================
        if (player.isShiftKeyDown()) {
            if (!slabTile.hasAnyCamo()) {
                openOptionsScreen(player, slabTile, pos);
                return InteractionResult.SUCCESS;
            }

            if (slabTile.isAppliedAsFullBlock()) {
                ItemStack dropStack = slabTile.getFullBlockDropStack();
                slabTile.clearAllCamo();
                if (!dropStack.isEmpty() && !player.isCreative()) {
                    returnItemStack(player, dropStack);
                }
            } else {
                double localY = hit.getLocation().y - pos.getY();
                if (slabType == SlabType.DOUBLE) {
                    if (localY < 0.5D) {
                        BlockState bottomCamo = slabTile.getCamouflagedBottomState();
                        if (bottomCamo != null) {
                            if (!player.isCreative()) {
                                returnItemStack(player, new ItemStack(bottomCamo.getBlock().asItem()));
                            }
                            slabTile.setCamouflagedBottomState(null);
                        } else {
                            openOptionsScreen(player, slabTile, pos);
                            return InteractionResult.SUCCESS;
                        }
                    } else {
                        BlockState topCamo = slabTile.getCamouflagedTopState();
                        if (topCamo != null) {
                            if (!player.isCreative()) {
                                returnItemStack(player, new ItemStack(topCamo.getBlock().asItem()));
                            }
                            slabTile.setCamouflagedTopState(null);
                        } else {
                            openOptionsScreen(player, slabTile, pos);
                            return InteractionResult.SUCCESS;
                        }
                    }
                } else if (slabType == SlabType.TOP) {
                    BlockState topCamo = slabTile.getCamouflagedTopState();
                    if (topCamo != null) {
                        if (!player.isCreative()) {
                            returnItemStack(player, new ItemStack(topCamo.getBlock().asItem()));
                        }
                        slabTile.setCamouflagedTopState(null);
                    }
                } else { // BOTTOM
                    BlockState bottomCamo = slabTile.getCamouflagedBottomState();
                    if (bottomCamo != null) {
                        if (!player.isCreative()) {
                            returnItemStack(player, new ItemStack(bottomCamo.getBlock().asItem()));
                        }
                        slabTile.setCamouflagedBottomState(null);
                    }
                }
            }

            playCamoSound(level, pos);
            return InteractionResult.SUCCESS;
        }

        // ==========================================================
        // 2. REGRA C: Mão vazia, ferramenta ou bloco inválido
        // ==========================================================
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem blockItem)) {
            openOptionsScreen(player, slabTile, pos);
            return InteractionResult.SUCCESS;
        }

        Block heldBlock = blockItem.getBlock();
        if (!isValidCamoBlock(heldBlock)) {
            openOptionsScreen(player, slabTile, pos);
            return InteractionResult.SUCCESS;
        }

        boolean isSlabItem = (heldBlock instanceof SlabBlock);
        SlabBlock correspondingSlab = findCorrespondingSlab(heldBlock);

        // ==========================================================
        // 3. REGRA A: Bloco sólido completo em DOUBLE
        // ==========================================================
        if (!isSlabItem && slabType == SlabType.DOUBLE) {
            BlockState bottomState;
            BlockState topState;
            if (correspondingSlab != null) {
                boolean waterlogged = state.hasProperty(SlabBlock.WATERLOGGED) && state.getValue(SlabBlock.WATERLOGGED);
                bottomState = correspondingSlab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                topState = correspondingSlab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
                if (bottomState.hasProperty(SlabBlock.WATERLOGGED)) bottomState = bottomState.setValue(SlabBlock.WATERLOGGED, waterlogged);
                if (topState.hasProperty(SlabBlock.WATERLOGGED)) topState = topState.setValue(SlabBlock.WATERLOGGED, waterlogged);
            } else {
                bottomState = heldBlock.defaultBlockState();
                topState = heldBlock.defaultBlockState();
            }

            if (bottomState.equals(slabTile.getCamouflagedBottomState()) && topState.equals(slabTile.getCamouflagedTopState()) && slabTile.isAppliedAsFullBlock()) {
                openOptionsScreen(player, slabTile, pos);
                return InteractionResult.SUCCESS;
            }

            if (!player.isCreative()) {
                if (slabTile.isAppliedAsFullBlock()) {
                    ItemStack oldDrop = slabTile.getFullBlockDropStack();
                    if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                } else {
                    if (slabTile.getCamouflagedBottomState() != null) returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottomState().getBlock().asItem()));
                    if (slabTile.getCamouflagedTopState() != null) returnItemStack(player, new ItemStack(slabTile.getCamouflagedTopState().getBlock().asItem()));
                }
            }

            slabTile.setCamouflagedBottomState(bottomState);
            slabTile.setCamouflagedTopState(topState);
            slabTile.setAppliedAsFullBlock(true);
            slabTile.setFullBlockSource(heldBlock);

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }

            playCamoSound(level, pos);
            return InteractionResult.SUCCESS;
        }

        // ==========================================================
        // 4. REGRA B: Laje (Slab) ou bloco com versão slab
        // ==========================================================
        if (isSlabItem || correspondingSlab != null) {
            SlabBlock slabToUse = isSlabItem ? (SlabBlock) heldBlock : correspondingSlab;
            boolean waterlogged = state.hasProperty(SlabBlock.WATERLOGGED) && state.getValue(SlabBlock.WATERLOGGED);
            BlockState bottomSlabState = slabToUse.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            BlockState topSlabState = slabToUse.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
            if (bottomSlabState.hasProperty(SlabBlock.WATERLOGGED)) bottomSlabState = bottomSlabState.setValue(SlabBlock.WATERLOGGED, waterlogged);
            if (topSlabState.hasProperty(SlabBlock.WATERLOGGED)) topSlabState = topSlabState.setValue(SlabBlock.WATERLOGGED, waterlogged);

            if (slabType == SlabType.DOUBLE) {
                double localY = hit.getLocation().y - pos.getY();
                if (localY < 0.5D) {
                    if (bottomSlabState.equals(slabTile.getCamouflagedBottomState()) && !slabTile.isAppliedAsFullBlock()) {
                        openOptionsScreen(player, slabTile, pos);
                        return InteractionResult.SUCCESS;
                    }

                    if (!player.isCreative()) {
                        if (slabTile.isAppliedAsFullBlock()) {
                            ItemStack oldDrop = slabTile.getFullBlockDropStack();
                            if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                        } else if (slabTile.getCamouflagedBottomState() != null) {
                            returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottomState().getBlock().asItem()));
                        }
                    }

                    slabTile.setCamouflagedBottomState(bottomSlabState);
                    if (slabTile.isAppliedAsFullBlock()) {
                        slabTile.setAppliedAsFullBlock(false);
                        slabTile.setFullBlockSource(null);
                    }
                } else {
                    if (topSlabState.equals(slabTile.getCamouflagedTopState()) && !slabTile.isAppliedAsFullBlock()) {
                        openOptionsScreen(player, slabTile, pos);
                        return InteractionResult.SUCCESS;
                    }

                    if (!player.isCreative()) {
                        if (slabTile.isAppliedAsFullBlock()) {
                            ItemStack oldDrop = slabTile.getFullBlockDropStack();
                            if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                        } else if (slabTile.getCamouflagedTopState() != null) {
                            returnItemStack(player, new ItemStack(slabTile.getCamouflagedTopState().getBlock().asItem()));
                        }
                    }

                    slabTile.setCamouflagedTopState(topSlabState);
                    if (slabTile.isAppliedAsFullBlock()) {
                        slabTile.setAppliedAsFullBlock(false);
                        slabTile.setFullBlockSource(null);
                    }
                }
            } else if (slabType == SlabType.TOP) {
                if (topSlabState.equals(slabTile.getCamouflagedTopState())) {
                    openOptionsScreen(player, slabTile, pos);
                    return InteractionResult.SUCCESS;
                }
                if (!player.isCreative() && slabTile.getCamouflagedTopState() != null) {
                    returnItemStack(player, new ItemStack(slabTile.getCamouflagedTopState().getBlock().asItem()));
                }
                slabTile.setCamouflagedTopState(topSlabState);
                slabTile.setAppliedAsFullBlock(false);
                slabTile.setFullBlockSource(null);
            } else { // BOTTOM
                if (bottomSlabState.equals(slabTile.getCamouflagedBottomState())) {
                    openOptionsScreen(player, slabTile, pos);
                    return InteractionResult.SUCCESS;
                }
                if (!player.isCreative() && slabTile.getCamouflagedBottomState() != null) {
                    returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottomState().getBlock().asItem()));
                }
                slabTile.setCamouflagedBottomState(bottomSlabState);
                slabTile.setAppliedAsFullBlock(false);
                slabTile.setFullBlockSource(null);
            }

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }

            playCamoSound(level, pos);
            return InteractionResult.SUCCESS;
        }

        openOptionsScreen(player, slabTile, pos);
        return InteractionResult.SUCCESS;
    }

    private void playCamoSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void returnItemStack(Player player, ItemStack stack) {
        if (!stack.isEmpty()) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    public static boolean isValidCamoBlock(Block block) {
        if (block == null || block instanceof ElevatorSlabBlock || block instanceof ElevatorBlock) {
            return false;
        }
        BlockState def = block.defaultBlockState();
        if (def.isAir() || def.hasBlockEntity()) {
            return false;
        }
        return def.getRenderShape() == net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Nullable
    public static Block findSourceBlock(Block slab) {
        if (!(slab instanceof SlabBlock)) {
            return slab;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(slab);
        if (key == null) {
            return null;
        }
        String path = key.getPath();
        String namespace = key.getNamespace();

        if (path.endsWith("_slab")) {
            String base = path.substring(0, path.length() - 5);
            ResourceLocation directLoc = new ResourceLocation(namespace, base);
            if (BuiltInRegistries.BLOCK.containsKey(directLoc)) {
                return BuiltInRegistries.BLOCK.get(directLoc);
            }
            ResourceLocation planksLoc = new ResourceLocation(namespace, base + "_planks");
            if (BuiltInRegistries.BLOCK.containsKey(planksLoc)) {
                return BuiltInRegistries.BLOCK.get(planksLoc);
            }
            ResourceLocation bricksLoc = new ResourceLocation(namespace, base + "s");
            if (BuiltInRegistries.BLOCK.containsKey(bricksLoc)) {
                return BuiltInRegistries.BLOCK.get(bricksLoc);
            }
            ResourceLocation blockLoc = new ResourceLocation(namespace, base + "_block");
            if (BuiltInRegistries.BLOCK.containsKey(blockLoc)) {
                return BuiltInRegistries.BLOCK.get(blockLoc);
            }
        }
        return null;
    }

    /**
     * Resolves a block to its corresponding SlabBlock:
     * - Returns the slab itself if the block is already a SlabBlock.
     * - Inspects the registry for slab variations of solid blocks (e.g., stone -> stone_slab,
     *   oak_planks -> oak_slab, stone_bricks -> stone_brick_slab, deepslate_tiles -> deepslate_tile_slab).
     */
    @Nullable
    public static SlabBlock findCorrespondingSlab(Block block) {
        if (block instanceof ElevatorSlabBlock || block instanceof ElevatorBlock) {
            return null;
        }
        if (block instanceof SlabBlock slab) {
            return slab;
        }

        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        if (key == null) {
            return null;
        }

        String namespace = key.getNamespace();
        String path = key.getPath();

        List<String> candidates = new ArrayList<>();
        // Direct suffix: e.g. stone -> stone_slab, cobblestone -> cobblestone_slab, granite -> granite_slab
        candidates.add(path + "_slab");

        // Planks suffix: e.g. oak_planks -> oak_slab, birch_planks -> birch_slab
        if (path.endsWith("_planks")) {
            candidates.add(path.replace("_planks", "_slab"));
        }

        // Bricks suffix: e.g. stone_bricks -> stone_brick_slab, mud_bricks -> mud_brick_slab
        if (path.endsWith("_bricks")) {
            candidates.add(path.substring(0, path.length() - 1) + "_slab");
        }

        // Tiles suffix: e.g. deepslate_tiles -> deepslate_tile_slab
        if (path.endsWith("_tiles")) {
            candidates.add(path.substring(0, path.length() - 1) + "_slab");
        }

        // Block suffix: e.g. quartz_block -> quartz_slab
        if (path.endsWith("_block")) {
            candidates.add(path.substring(0, path.length() - 6) + "_slab");
        }

        for (String candidate : candidates) {
            ResourceLocation candidateLoc = new ResourceLocation(namespace, candidate);
            if (BuiltInRegistries.BLOCK.containsKey(candidateLoc)) {
                Block candidateBlock = BuiltInRegistries.BLOCK.get(candidateLoc);
                if (candidateBlock instanceof SlabBlock foundSlab
                        && !(candidateBlock instanceof ElevatorSlabBlock)
                        && !(candidateBlock instanceof ElevatorBlock)) {
                    return foundSlab;
                }
            }
        }

        return null;
    }
}
