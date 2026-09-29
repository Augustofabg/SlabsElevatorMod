package net.openslabs.elevatorslabs.block;

import com.vsngarcia.ElevatorBlockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
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
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
            boolean crouching = player.isCrouching();
            double localY = getHitY(level, pos, player);

            if (!crouching && localY <= 0.45D) {
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
                                // Full block drop!
                                ItemStack fullDrop = slabTile.getFullBlockDropStack();
                                if (!fullDrop.isEmpty()) {
                                    popResource(level, pos, fullDrop);
                                }
                            } else {
                                BlockState camo = slabTile.getCamouflagedBottom();
                                if (camo != null) {
                                    popResource(level, pos, new ItemStack(camo.getBlock().asItem()));
                                }
                            }
                        }

                        if (slabTile.isAppliedAsFullBlock()) {
                            // Section 5: A laje restante perde a camuflagem imediatamente
                            slabTile.clearAllCamo();
                        } else {
                            slabTile.setCamouflagedBottom(null);
                        }
                    }

                    level.setBlock(pos, remainingState, 3);
                    level.levelEvent(player, 2001, pos, Block.getId(state));
                } else {
                    level.setBlock(pos, remainingState, 11);
                }

                return false;
            } else if (!crouching && localY >= 0.55D) {
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
                                // Full block drop!
                                ItemStack fullDrop = slabTile.getFullBlockDropStack();
                                if (!fullDrop.isEmpty()) {
                                    popResource(level, pos, fullDrop);
                                }
                            } else {
                                BlockState camo = slabTile.getCamouflagedTop();
                                if (camo != null) {
                                    popResource(level, pos, new ItemStack(camo.getBlock().asItem()));
                                }
                            }
                        }

                        if (slabTile.isAppliedAsFullBlock()) {
                            // Section 5: A laje restante perde a camuflagem imediatamente
                            slabTile.clearAllCamo();
                        } else {
                            slabTile.setCamouflagedTop(null);
                        }
                    }

                    level.setBlock(pos, remainingState, 3);
                    level.levelEvent(player, 2001, pos, Block.getId(state));
                } else {
                    level.setBlock(pos, remainingState, 11);
                }

                return false;
            }
            // Full block selected (crouching or 0.45 < localY < 0.55):
            // Destroy entire block, dropping 2 slabs via loot table and drops handled in onRemove
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
                            // Section 3: Se foi camuflada com full block, a nova metade herda a mesma camuflagem
                            if (oldType == SlabType.BOTTOM) {
                                slabTile.setCamouflagedTop(slabTile.getAdaptedTopCamo());
                            } else if (oldType == SlabType.TOP) {
                                slabTile.setCamouflagedBottom(slabTile.getAdaptedBottomCamo());
                            }
                        } else {
                            // Section 3: Se foi camuflada com laje, a nova metade nasce SEM camuflagem (null)
                            if (oldType == SlabType.BOTTOM) {
                                slabTile.setCamouflagedTop(null);
                            } else if (oldType == SlabType.TOP) {
                                slabTile.setCamouflagedBottom(null);
                            }
                        }
                    }
                }
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && state.hasProperty(TYPE)) {
            SlabType type = state.getValue(TYPE);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ElevatorSlabBlockEntity slabTile) {
                if (type == SlabType.BOTTOM && !slabTile.isAppliedAsFullBlock()) {
                    slabTile.setCamouflagedTop(null);
                } else if (type == SlabType.TOP && !slabTile.isAppliedAsFullBlock()) {
                    slabTile.setCamouflagedBottom(null);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ElevatorSlabBlockEntity slabTile) {
                    if (slabTile.isAppliedAsFullBlock()) {
                        ItemStack fullDrop = slabTile.getFullBlockDropStack();
                        if (!fullDrop.isEmpty()) {
                            popResource(level, pos, fullDrop);
                        }
                    } else {
                        BlockState bottomCamo = slabTile.getCamouflagedBottom();
                        if (bottomCamo != null) {
                            popResource(level, pos, new ItemStack(bottomCamo.getBlock().asItem()));
                        }
                        BlockState topCamo = slabTile.getCamouflagedTop();
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

        // If holding matching slab on a single slab without sneaking, pass to allow placing second slab to form DOUBLE:
        if (!player.isShiftKeyDown() && slabType != SlabType.DOUBLE && itemStack.is(this.asItem())) {
            return InteractionResult.PASS;
        }

        // 0. Elevator as configuration tool:
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            Block heldBlock = blockItem.getBlock();
            if (heldBlock instanceof ElevatorSlabBlock || heldBlock instanceof ElevatorBlockBase) {
                player.openMenu(slabTile, pos);
                return InteractionResult.SUCCESS;
            }
        }

        // 4. REMOÇÃO DE CAMUFLAGEM VIA SHIFT + CLIQUE DIREITO:
        if (player.isShiftKeyDown()) {
            if (!slabTile.hasAnyCamo()) {
                player.openMenu(slabTile, pos);
                return InteractionResult.SUCCESS;
            }

            if (slabTile.isAppliedAsFullBlock()) {
                ItemStack dropStack = slabTile.getFullBlockDropStack();
                slabTile.clearAllCamo();
                if (!dropStack.isEmpty() && !player.isCreative()) {
                    popResource(level, pos, dropStack);
                }
            } else {
                double localY = hit.getLocation().y - pos.getY();
                if (slabType == SlabType.DOUBLE) {
                    if (localY < 0.5D) {
                        BlockState bottomCamo = slabTile.getCamouflagedBottom();
                        if (bottomCamo != null) {
                            if (!player.isCreative()) {
                                popResource(level, pos, new ItemStack(bottomCamo.getBlock().asItem()));
                            }
                            slabTile.setCamouflagedBottom(null);
                        } else {
                            player.openMenu(slabTile, pos);
                            return InteractionResult.SUCCESS;
                        }
                    } else {
                        BlockState topCamo = slabTile.getCamouflagedTop();
                        if (topCamo != null) {
                            if (!player.isCreative()) {
                                popResource(level, pos, new ItemStack(topCamo.getBlock().asItem()));
                            }
                            slabTile.setCamouflagedTop(null);
                        } else {
                            player.openMenu(slabTile, pos);
                            return InteractionResult.SUCCESS;
                        }
                    }
                } else if (slabType == SlabType.TOP) {
                    BlockState topCamo = slabTile.getCamouflagedTop();
                    if (topCamo != null) {
                        if (!player.isCreative()) {
                            popResource(level, pos, new ItemStack(topCamo.getBlock().asItem()));
                        }
                        slabTile.setCamouflagedTop(null);
                    }
                } else { // BOTTOM
                    BlockState bottomCamo = slabTile.getCamouflagedBottom();
                    if (bottomCamo != null) {
                        if (!player.isCreative()) {
                            popResource(level, pos, new ItemStack(bottomCamo.getBlock().asItem()));
                        }
                        slabTile.setCamouflagedBottom(null);
                    }
                }
            }

            level.sendBlockUpdated(pos, state, state, 3);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // 2C. Mão vazia ou item que não seja bloco: abre a interface de opções
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem blockItem)) {
            player.openMenu(slabTile, pos);
            return InteractionResult.SUCCESS;
        }

        Block heldBlock = blockItem.getBlock();
        if (!isValidCamoBlock(heldBlock)) {
            player.openMenu(slabTile, pos);
            return InteractionResult.SUCCESS;
        }

        SlabBlock targetSlab = findCorrespondingSlab(heldBlock);
        boolean isSlab = (heldBlock instanceof SlabBlock);

        // 2A. Bloco que NÃO possui versão em laje:
        if (targetSlab == null) {
            if (slabType != SlabType.DOUBLE) {
                player.openMenu(slabTile, pos);
                return InteractionResult.SUCCESS;
            }

            BlockState camoState = heldBlock.defaultBlockState();
            if (camoState.equals(slabTile.getCamouflagedBottom()) && camoState.equals(slabTile.getCamouflagedTop())) {
                player.openMenu(slabTile, pos);
                return InteractionResult.SUCCESS;
            }

            if (!player.isCreative()) {
                if (slabTile.isAppliedAsFullBlock()) {
                    ItemStack oldDrop = slabTile.getFullBlockDropStack();
                    if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                } else {
                    if (slabTile.getCamouflagedBottom() != null) {
                        returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottom().getBlock().asItem()));
                    }
                    if (slabTile.getCamouflagedTop() != null) {
                        returnItemStack(player, new ItemStack(slabTile.getCamouflagedTop().getBlock().asItem()));
                    }
                }
            }

            slabTile.setCamouflagedBottom(camoState);
            slabTile.setCamouflagedTop(camoState);
            slabTile.setAppliedAsFullBlock(true);
            slabTile.setFullBlockSource(heldBlock);

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }

            level.sendBlockUpdated(pos, state, state, 3);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // 2B. Bloco que POSSUI versão em laje ou a própria LAJE (Slab):
        boolean waterlogged = state.hasProperty(SlabBlock.WATERLOGGED) && state.getValue(SlabBlock.WATERLOGGED);
        BlockState targetBottomState = targetSlab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (targetBottomState.hasProperty(SlabBlock.WATERLOGGED)) {
            targetBottomState = targetBottomState.setValue(SlabBlock.WATERLOGGED, waterlogged);
        }
        BlockState targetTopState = targetSlab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
        if (targetTopState.hasProperty(SlabBlock.WATERLOGGED)) {
            targetTopState = targetTopState.setValue(SlabBlock.WATERLOGGED, waterlogged);
        }

        if (!isSlab) {
            if (slabType == SlabType.DOUBLE) {
                boolean bottomMatch = targetBottomState.equals(slabTile.getCamouflagedBottom());
                boolean topMatch = targetTopState.equals(slabTile.getCamouflagedTop());
                if (bottomMatch && topMatch) {
                    player.openMenu(slabTile, pos);
                    return InteractionResult.SUCCESS;
                }

                if (!player.isCreative()) {
                    if (slabTile.isAppliedAsFullBlock()) {
                        ItemStack oldDrop = slabTile.getFullBlockDropStack();
                        if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                    } else {
                        if (slabTile.getCamouflagedBottom() != null) {
                            returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottom().getBlock().asItem()));
                        }
                        if (slabTile.getCamouflagedTop() != null) {
                            returnItemStack(player, new ItemStack(slabTile.getCamouflagedTop().getBlock().asItem()));
                        }
                    }
                }

                slabTile.setCamouflagedBottom(targetBottomState);
                slabTile.setCamouflagedTop(targetTopState);
                slabTile.setAppliedAsFullBlock(true);
                slabTile.setFullBlockSource(heldBlock);
            } else if (slabType == SlabType.TOP) {
                if (targetTopState.equals(slabTile.getCamouflagedTop())) {
                    player.openMenu(slabTile, pos);
                    return InteractionResult.SUCCESS;
                }
                if (!player.isCreative()) {
                    if (slabTile.isAppliedAsFullBlock()) {
                        ItemStack oldDrop = slabTile.getFullBlockDropStack();
                        if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                    } else if (slabTile.getCamouflagedTop() != null) {
                        returnItemStack(player, new ItemStack(slabTile.getCamouflagedTop().getBlock().asItem()));
                    }
                }
                slabTile.setCamouflagedTop(targetTopState);
                slabTile.setAppliedAsFullBlock(true);
                slabTile.setFullBlockSource(heldBlock);
            } else { // BOTTOM
                if (targetBottomState.equals(slabTile.getCamouflagedBottom())) {
                    player.openMenu(slabTile, pos);
                    return InteractionResult.SUCCESS;
                }
                if (!player.isCreative()) {
                    if (slabTile.isAppliedAsFullBlock()) {
                        ItemStack oldDrop = slabTile.getFullBlockDropStack();
                        if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                    } else if (slabTile.getCamouflagedBottom() != null) {
                        returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottom().getBlock().asItem()));
                    }
                }
                slabTile.setCamouflagedBottom(targetBottomState);
                slabTile.setAppliedAsFullBlock(true);
                slabTile.setFullBlockSource(heldBlock);
            }

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }

            level.sendBlockUpdated(pos, state, state, 3);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;

        } else {
            // Laje (Slab): appliedAsFullBlock = false
            double localY = hit.getLocation().y - pos.getY();

            if (slabType == SlabType.DOUBLE) {
                if (localY < 0.5D) {
                    if (targetBottomState.equals(slabTile.getCamouflagedBottom())) {
                        player.openMenu(slabTile, pos);
                        return InteractionResult.SUCCESS;
                    }
                    if (!player.isCreative()) {
                        if (slabTile.isAppliedAsFullBlock()) {
                            ItemStack oldDrop = slabTile.getFullBlockDropStack();
                            if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                        } else if (slabTile.getCamouflagedBottom() != null) {
                            returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottom().getBlock().asItem()));
                        }
                    }
                    slabTile.setCamouflagedBottom(targetBottomState);
                    slabTile.setAppliedAsFullBlock(false);
                    slabTile.setFullBlockSource(null);
                } else {
                    if (targetTopState.equals(slabTile.getCamouflagedTop())) {
                        player.openMenu(slabTile, pos);
                        return InteractionResult.SUCCESS;
                    }
                    if (!player.isCreative()) {
                        if (slabTile.isAppliedAsFullBlock()) {
                            ItemStack oldDrop = slabTile.getFullBlockDropStack();
                            if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                        } else if (slabTile.getCamouflagedTop() != null) {
                            returnItemStack(player, new ItemStack(slabTile.getCamouflagedTop().getBlock().asItem()));
                        }
                    }
                    slabTile.setCamouflagedTop(targetTopState);
                    slabTile.setAppliedAsFullBlock(false);
                    slabTile.setFullBlockSource(null);
                }
            } else if (slabType == SlabType.TOP) {
                if (targetTopState.equals(slabTile.getCamouflagedTop())) {
                    player.openMenu(slabTile, pos);
                    return InteractionResult.SUCCESS;
                }
                if (!player.isCreative()) {
                    if (slabTile.isAppliedAsFullBlock()) {
                        ItemStack oldDrop = slabTile.getFullBlockDropStack();
                        if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                    } else if (slabTile.getCamouflagedTop() != null) {
                        returnItemStack(player, new ItemStack(slabTile.getCamouflagedTop().getBlock().asItem()));
                    }
                }
                slabTile.setCamouflagedTop(targetTopState);
                slabTile.setAppliedAsFullBlock(false);
                slabTile.setFullBlockSource(null);
            } else { // BOTTOM
                if (targetBottomState.equals(slabTile.getCamouflagedBottom())) {
                    player.openMenu(slabTile, pos);
                    return InteractionResult.SUCCESS;
                }
                if (!player.isCreative()) {
                    if (slabTile.isAppliedAsFullBlock()) {
                        ItemStack oldDrop = slabTile.getFullBlockDropStack();
                        if (!oldDrop.isEmpty()) returnItemStack(player, oldDrop);
                    } else if (slabTile.getCamouflagedBottom() != null) {
                        returnItemStack(player, new ItemStack(slabTile.getCamouflagedBottom().getBlock().asItem()));
                    }
                }
                slabTile.setCamouflagedBottom(targetBottomState);
                slabTile.setAppliedAsFullBlock(false);
                slabTile.setFullBlockSource(null);
            }

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }

            level.sendBlockUpdated(pos, state, state, 3);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
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
