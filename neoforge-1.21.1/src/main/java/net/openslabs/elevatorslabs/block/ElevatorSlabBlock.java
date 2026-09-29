package net.openslabs.elevatorslabs.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vsngarcia.ElevatorBlockBase;
import com.vsngarcia.neoforge.init.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
 * Base block for Elevator Slabs, extending vanilla SlabBlock while retaining
 * color identification, expanded camouflage support (slabs and full blocks with slab variants),
 * native Minecraft dynamic selection box per half for double slabs, selective half breaking,
 * and priority-based configuration GUI interactions.
 */
public class ElevatorSlabBlock extends SlabBlock implements EntityBlock {

    public static final MapCodec<ElevatorSlabBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DyeColor.CODEC.fieldOf("color").forGetter(ElevatorSlabBlock::getColor),
                    propertiesCodec()
            ).apply(instance, ElevatorSlabBlock::new)
    );

    private final DyeColor color;

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

    @Override
    public MapCodec<ElevatorSlabBlock> codec() {
        return CODEC;
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
                    Vec3 endPos = eyePos.add(viewVec.scale(6.0D));
                    AABB box = new AABB(pos);
                    Optional<Vec3> boxHit = box.clip(eyePos, endPos);
                    if (boxHit.isPresent()) {
                        double localY = boxHit.get().y - pos.getY();
                        return (localY >= 0.5D) ? TOP_AABB : BOTTOM_AABB;
                    }
                    return (eyePos.y < pos.getY() + 0.5D) ? BOTTOM_AABB : TOP_AABB;
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
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        return super.playerWillDestroy(level, pos, state, player);
    }

    public static ItemStack getSlabItemForState(@Nullable BlockState state) {
        if (state == null || state.isAir()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(state.getBlock().asItem());
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (state.hasProperty(TYPE) && state.getValue(TYPE) == SlabType.DOUBLE) {
            BlockEntity be = level.getBlockEntity(pos);

            // Raycast preciso via player.pick()
            double localY;
            HitResult hit = player.pick(20.0D, 0.0F, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                localY = hit.getLocation().y - pos.getY();
            } else {
                localY = getHitY(level, pos, player);
            }

            // ZONA CENTRAL DE JUNÇÃO (0.45 <= localY <= 0.55): quebra as DUAS metades
            if (localY >= 0.45D && localY <= 0.55D) {
                // Drops do bloco completo são tratados em onRemove
                return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
            }

            if (be instanceof ElevatorSlabBlockEntity elevatorBe) {
                boolean brokeTop = localY > 0.55D;
                // localY < 0.45 → quebrou a base (novo estado = TOP)
                // localY > 0.55 → quebrou o topo (novo estado = BOTTOM)
                BlockState newState = state.setValue(SlabBlock.TYPE, brokeTop ? SlabType.BOTTOM : SlabType.TOP);
                boolean wasFullBlock = elevatorBe.isAppliedAsFullBlock();

                // CONTROLE DE DROPS (Survival apenas):
                if (!level.isClientSide && !player.isCreative()) {
                    popResource(level, pos, new ItemStack(this.asItem())); // 1 Elevator Slab

                    if (wasFullBlock) {
                        // Full block: dropa o bloco completo original
                        ItemStack fullDrop = elevatorBe.getFullBlockDropStack();
                        if (!fullDrop.isEmpty()) {
                            popResource(level, pos, fullDrop);
                        }
                    } else {
                        // Lajes independentes: dropa apenas a laje da metade quebrada
                        BlockState brokenCamo = brokeTop
                                ? elevatorBe.getCamouflagedTopState()
                                : elevatorBe.getCamouflagedBottomState();
                        if (brokenCamo != null && !brokenCamo.isAir()) {
                            popResource(level, pos, getSlabItemForState(brokenCamo));
                        }
                    }
                }
                // SE FOR CRIATIVO: NÃO DROPAR ABSOLUTAMENTE NADA!

                // ATUALIZAÇÃO DA BLOCK ENTITY:
                if (wasFullBlock) {
                    // Full block: limpa AMBAS as metades
                    elevatorBe.setCamouflagedBottomState(null);
                    elevatorBe.setCamouflagedTopState(null);
                    elevatorBe.setAppliedAsFullBlock(false);
                    elevatorBe.setFullBlockSource(null);
                } else {
                    // Lajes independentes: limpa APENAS a metade quebrada, preserva a outra
                    if (brokeTop) {
                        elevatorBe.setCamouflagedTopState(null);
                    } else {
                        elevatorBe.setCamouflagedBottomState(null);
                    }
                }
                elevatorBe.setChanged();

                // ATUALIZAR MUNDO E SINCRONIZAR CLIENTE:
                level.setBlock(pos, newState, 3);
                level.sendBlockUpdated(pos, state, newState, 3);
                elevatorBe.requestModelDataUpdate();
                level.sendBlockUpdated(pos, newState, newState, Block.UPDATE_ALL_IMMEDIATE);
                elevatorBe.notifyUpdate();

                if (!level.isClientSide) {
                    level.levelEvent(player, 2001, pos, Block.getId(state));
                }

                return false; // IMPEDE A ENGINE DE REMOVER O BLOCO INTEIRO
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && state.is(this) && oldState.is(this)) {
            if (oldState.hasProperty(TYPE) && state.hasProperty(TYPE)) {
                SlabType oldType = oldState.getValue(TYPE);
                SlabType newType = state.getValue(TYPE);
                if (oldType != newType && newType == SlabType.DOUBLE) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof ElevatorSlabBlockEntity slabTile) {
                        if (slabTile.isAppliedAsFullBlock()) {
                            if (oldType == SlabType.BOTTOM) {
                                slabTile.setCamouflagedTopState(slabTile.getAdaptedTopCamo());
                            } else if (oldType == SlabType.TOP) {
                                slabTile.setCamouflagedBottomState(slabTile.getAdaptedBottomCamo());
                            }
                        } else {
                            if (oldType == SlabType.BOTTOM) {
                                slabTile.setCamouflagedTopState(null);
                            } else if (oldType == SlabType.TOP) {
                                slabTile.setCamouflagedBottomState(null);
                            }
                        }
                        slabTile.setChanged();
                        slabTile.requestModelDataUpdate();
                        slabTile.notifyUpdate();
                        level.sendBlockUpdated(pos, oldState, state, Block.UPDATE_ALL);
                    }
                }
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && state.hasProperty(TYPE)) {
            SlabType type = state.getValue(TYPE);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ElevatorSlabBlockEntity slabTile) {
                if (type == SlabType.BOTTOM && !slabTile.isAppliedAsFullBlock()) {
                    slabTile.setCamouflagedTopState(null);
                } else if (type == SlabType.TOP && !slabTile.isAppliedAsFullBlock()) {
                    slabTile.setCamouflagedBottomState(null);
                }
                slabTile.setChanged();
                slabTile.requestModelDataUpdate();
                slabTile.notifyUpdate();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // Se está trocando para um bloco diferente (não é transição DOUBLE→BOTTOM/TOP do mesmo bloco)
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
        }
        // Se é o mesmo bloco mudando de tipo (ex: DOUBLE → BOTTOM/TOP via onDestroyedByPlayer),
        // NÃO chamamos super para não destruir a BlockEntity nem dropar itens novamente.
    }

    protected double getHitY(Level level, BlockPos pos, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(viewVec.scale(20.0D));
        AABB box = new AABB(pos);
        Optional<Vec3> boxHit = box.clip(eyePos, endPos);
        if (boxHit.isPresent()) {
            return boxHit.get().y - pos.getY();
        }
        return (eyePos.y < pos.getY() + 0.5D) ? 0.25D : 0.75D;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ElevatorSlabBlockEntity slabTile)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        SlabType slabType = state.hasProperty(SlabBlock.TYPE) ? state.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;

        // If holding matching slab on a single slab without sneaking, allow placing second slab:
        if (!player.isShiftKeyDown() && slabType != SlabType.DOUBLE && itemStack.is(this.asItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // ==========================================================
        // 1. Shift + Right Click (Remoção de Camuflagem):
        // ==========================================================
        if (player.isShiftKeyDown()) {
            if (!slabTile.hasAnyCamo()) {
                player.openMenu(slabTile, pos);
                return ItemInteractionResult.SUCCESS;
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
                            player.openMenu(slabTile, pos);
                            return ItemInteractionResult.SUCCESS;
                        }
                    } else {
                        BlockState topCamo = slabTile.getCamouflagedTopState();
                        if (topCamo != null) {
                            if (!player.isCreative()) {
                                returnItemStack(player, new ItemStack(topCamo.getBlock().asItem()));
                            }
                            slabTile.setCamouflagedTopState(null);
                        } else {
                            player.openMenu(slabTile, pos);
                            return ItemInteractionResult.SUCCESS;
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
            return ItemInteractionResult.SUCCESS;
        }

        // ==========================================================
        // 2. REGRA C: Mão vazia, ferramenta ou bloco inválido
        // ==========================================================
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem blockItem)) {
            player.openMenu(slabTile, pos);
            return ItemInteractionResult.SUCCESS;
        }

        Block heldBlock = blockItem.getBlock();
        if (!isValidCamoBlock(heldBlock)) {
            player.openMenu(slabTile, pos);
            return ItemInteractionResult.SUCCESS;
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
                player.openMenu(slabTile, pos);
                return ItemInteractionResult.SUCCESS;
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
            return ItemInteractionResult.SUCCESS;
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
                        player.openMenu(slabTile, pos);
                        return ItemInteractionResult.SUCCESS;
                    }

                    if (!player.isCreative()) {
                        if (slabTile.isAppliedAsFullBlock()) {
                            ItemStack oldDrop = slabTile.getFullBlockDropStack();
                            if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                        } else if (slabTile.getCamouflagedBottomState() != null) {
                            returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottomState().getBlock().asItem()));
                        }
                    }

                    if (slabTile.isAppliedAsFullBlock()) {
                        slabTile.setCamouflagedTopState(null);
                        slabTile.setAppliedAsFullBlock(false);
                        slabTile.setFullBlockSource(null);
                    }
                    slabTile.setCamouflagedBottomState(bottomSlabState);
                } else {
                    if (topSlabState.equals(slabTile.getCamouflagedTopState()) && !slabTile.isAppliedAsFullBlock()) {
                        player.openMenu(slabTile, pos);
                        return ItemInteractionResult.SUCCESS;
                    }

                    if (!player.isCreative()) {
                        if (slabTile.isAppliedAsFullBlock()) {
                            ItemStack oldDrop = slabTile.getFullBlockDropStack();
                            if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                        } else if (slabTile.getCamouflagedTopState() != null) {
                            returnItemStack(player, new ItemStack(slabTile.getCamouflagedTopState().getBlock().asItem()));
                        }
                    }

                    if (slabTile.isAppliedAsFullBlock()) {
                        slabTile.setCamouflagedBottomState(null);
                        slabTile.setAppliedAsFullBlock(false);
                        slabTile.setFullBlockSource(null);
                    }
                    slabTile.setCamouflagedTopState(topSlabState);
                }
            } else if (slabType == SlabType.TOP) {
                if (topSlabState.equals(slabTile.getCamouflagedTopState())) {
                    player.openMenu(slabTile, pos);
                    return ItemInteractionResult.SUCCESS;
                }
                if (!player.isCreative() && slabTile.getCamouflagedTopState() != null) {
                    returnItemStack(player, new ItemStack(slabTile.getCamouflagedTopState().getBlock().asItem()));
                }
                slabTile.setCamouflagedTopState(topSlabState);
                slabTile.setAppliedAsFullBlock(false);
                slabTile.setFullBlockSource(null);
            } else { // BOTTOM
                if (bottomSlabState.equals(slabTile.getCamouflagedBottomState())) {
                    player.openMenu(slabTile, pos);
                    return ItemInteractionResult.SUCCESS;
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
            return ItemInteractionResult.SUCCESS;
        }

        player.openMenu(slabTile, pos);
        return ItemInteractionResult.SUCCESS;
    }

    private void playCamoSound(Level level, BlockPos pos) {
        try {
            level.playSound(null, pos, Registry.CAMOUFLAGE_SOUND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        } catch (Throwable ignored) {}
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ElevatorSlabBlockEntity slabTile) {
            if (player.isShiftKeyDown() && slabTile.hasAnyCamo()) {
                ItemInteractionResult res = useItemOn(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND, hit);
                return res.result();
            }
            player.openMenu(slabTile, pos);
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    private static void returnItemStack(Player player, ItemStack stack) {
        if (!stack.isEmpty()) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    public static boolean isValidCamoBlock(Block block) {
        if (block == null || block instanceof ElevatorSlabBlock || block instanceof ElevatorBlockBase) {
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
            ResourceLocation directLoc = ResourceLocation.fromNamespaceAndPath(namespace, base);
            if (BuiltInRegistries.BLOCK.containsKey(directLoc)) {
                return BuiltInRegistries.BLOCK.get(directLoc);
            }
            ResourceLocation planksLoc = ResourceLocation.fromNamespaceAndPath(namespace, base + "_planks");
            if (BuiltInRegistries.BLOCK.containsKey(planksLoc)) {
                return BuiltInRegistries.BLOCK.get(planksLoc);
            }
            ResourceLocation bricksLoc = ResourceLocation.fromNamespaceAndPath(namespace, base + "s");
            if (BuiltInRegistries.BLOCK.containsKey(bricksLoc)) {
                return BuiltInRegistries.BLOCK.get(bricksLoc);
            }
            ResourceLocation blockLoc = ResourceLocation.fromNamespaceAndPath(namespace, base + "_block");
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
        if (block instanceof ElevatorSlabBlock || block instanceof ElevatorBlockBase) {
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
            ResourceLocation candidateLoc = ResourceLocation.fromNamespaceAndPath(namespace, candidate);
            if (BuiltInRegistries.BLOCK.containsKey(candidateLoc)) {
                Block candidateBlock = BuiltInRegistries.BLOCK.get(candidateLoc);
                if (candidateBlock instanceof SlabBlock foundSlab
                        && !(candidateBlock instanceof ElevatorSlabBlock)
                        && !(candidateBlock instanceof ElevatorBlockBase)) {
                    return foundSlab;
                }
            }
        }

        return null;
    }
}
