package net.openslabs.elevatorslabs.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.init.ModBlockEntities;
import net.openslabs.elevatorslabs.menu.ElevatorOptionsMenu;

import javax.annotation.Nullable;

/**
 * BlockEntity for Elevator Slabs. Stores configuration state:
 * - directional: whether forced rotation on teleport is enabled (default false)
 * - facing: the Direction the player faces upon arrival (default NORTH)
 * - hideArrow: whether the directional indicator arrow should be hidden (default false)
 * - camouflagedBottom: optional disguised appearance for the bottom slab half
 * - camouflagedTop: optional disguised appearance for the top slab half
 * - appliedAsFullBlock: whether the disguise came from a solid full block covering the space
 * - fullBlockSource: the original full block used when appliedAsFullBlock is true
 */
public class ElevatorSlabBlockEntity extends BlockEntity implements MenuProvider {

    public static final ModelProperty<BlockState> CAMO_BOTTOM = new ModelProperty<>();
    public static final ModelProperty<BlockState> CAMO_TOP = new ModelProperty<>();
    public static final ModelProperty<BlockState> CAMO_STATE = new ModelProperty<>();
    public static final ModelProperty<Boolean> APPLIED_AS_FULL_BLOCK = new ModelProperty<>();
    public static final ModelProperty<Block> FULL_BLOCK_SOURCE = new ModelProperty<>();

    private boolean directional = false;
    private Direction facing = Direction.NORTH;
    private boolean hideArrow = false;

    @Nullable
    private BlockState camouflagedBottomState = null;
    @Nullable
    private BlockState camouflagedTopState = null;
    private boolean appliedAsFullBlock = false;
    @Nullable
    private Block fullBlockSource = null;

    public ElevatorSlabBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELEVATOR_SLAB_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("directional", this.directional);
        tag.putString("facing", this.facing.getName());
        tag.putBoolean("hideArrow", this.hideArrow);
        tag.putBoolean("appliedAsFullBlock", this.appliedAsFullBlock);

        if (this.fullBlockSource != null) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(this.fullBlockSource);
            if (key != null) {
                tag.putString("FullBlockSource", key.toString());
            }
        }

        if (this.camouflagedBottomState != null) {
            tag.put("CamouflagedBottom", NbtUtils.writeBlockState(this.camouflagedBottomState));
            tag.put("camouflagedBottomState", NbtUtils.writeBlockState(this.camouflagedBottomState));
            tag.put("camoBottom", NbtUtils.writeBlockState(this.camouflagedBottomState));
        }
        if (this.camouflagedTopState != null) {
            tag.put("CamouflagedTop", NbtUtils.writeBlockState(this.camouflagedTopState));
            tag.put("camouflagedTopState", NbtUtils.writeBlockState(this.camouflagedTopState));
            tag.put("camoTop", NbtUtils.writeBlockState(this.camouflagedTopState));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.directional = tag.getBoolean("directional");

        Direction loadedFacing = Direction.byName(tag.getString("facing"));
        this.facing = (loadedFacing != null && loadedFacing.getAxis().isHorizontal()) ? loadedFacing : Direction.NORTH;

        this.hideArrow = tag.getBoolean("hideArrow");
        this.appliedAsFullBlock = tag.getBoolean("appliedAsFullBlock");

        if (tag.contains("FullBlockSource", CompoundTag.TAG_STRING)) {
            try {
                ResourceLocation loc = ResourceLocation.parse(tag.getString("FullBlockSource"));
                this.fullBlockSource = BuiltInRegistries.BLOCK.get(loc);
            } catch (Exception e) {
                this.fullBlockSource = null;
            }
        } else {
            this.fullBlockSource = null;
        }

        // Load bottom camouflage strictly
        if (tag.contains("CamouflagedBottom", CompoundTag.TAG_COMPOUND)) {
            try {
                this.camouflagedBottomState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("CamouflagedBottom"));
            } catch (Exception e) {
                this.camouflagedBottomState = null;
            }
        } else if (tag.contains("camouflagedBottomState", CompoundTag.TAG_COMPOUND)) {
            try {
                this.camouflagedBottomState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("camouflagedBottomState"));
            } catch (Exception e) {
                this.camouflagedBottomState = null;
            }
        } else if (tag.contains("camoBottom", CompoundTag.TAG_COMPOUND)) {
            try {
                this.camouflagedBottomState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("camoBottom"));
            } catch (Exception e) {
                this.camouflagedBottomState = null;
            }
        } else {
            this.camouflagedBottomState = null;
        }

        // Load top camouflage strictly
        if (tag.contains("CamouflagedTop", CompoundTag.TAG_COMPOUND)) {
            try {
                this.camouflagedTopState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("CamouflagedTop"));
            } catch (Exception e) {
                this.camouflagedTopState = null;
            }
        } else if (tag.contains("camouflagedTopState", CompoundTag.TAG_COMPOUND)) {
            try {
                this.camouflagedTopState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("camouflagedTopState"));
            } catch (Exception e) {
                this.camouflagedTopState = null;
            }
        } else if (tag.contains("camoTop", CompoundTag.TAG_COMPOUND)) {
            try {
                this.camouflagedTopState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("camoTop"));
            } catch (Exception e) {
                this.camouflagedTopState = null;
            }
        } else {
            this.camouflagedTopState = null;
        }

        // Only if BOTH are null, check if there was a legacy single-block tag from old versions
        if (this.camouflagedBottomState == null && this.camouflagedTopState == null && tag.contains("camouflagedBlock", CompoundTag.TAG_COMPOUND)) {
            try {
                BlockState legacy = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("camouflagedBlock"));
                BlockState myState = getBlockState();
                SlabType type = myState.hasProperty(SlabBlock.TYPE) ? myState.getValue(SlabBlock.TYPE) : SlabType.BOTTOM;
                if (type == SlabType.TOP) {
                    this.camouflagedTopState = legacy;
                } else if (type == SlabType.BOTTOM) {
                    this.camouflagedBottomState = legacy;
                } else {
                    this.camouflagedBottomState = legacy;
                    this.camouflagedTopState = legacy;
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
        requestModelDataUpdate();
        if (this.level != null && this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(CAMO_BOTTOM, this.camouflagedBottomState)
                .with(CAMO_TOP, this.camouflagedTopState)
                .with(CAMO_STATE, getCamouflagedBlock())
                .with(APPLIED_AS_FULL_BLOCK, this.appliedAsFullBlock)
                .with(FULL_BLOCK_SOURCE, this.fullBlockSource)
                .build();
    }

    public boolean isDirectional() {
        return this.directional;
    }

    public void setDirectional(boolean directional) {
        this.directional = directional;
        setChanged();
        notifyUpdate();
    }

    public Direction getFacing() {
        return this.facing;
    }

    public void setFacing(Direction facing) {
        if (facing != null && facing.getAxis().isHorizontal()) {
            this.facing = facing;
            setChanged();
            notifyUpdate();
        }
    }

    public boolean isHideArrow() {
        return this.hideArrow;
    }

    public void setHideArrow(boolean hideArrow) {
        this.hideArrow = hideArrow;
        setChanged();
        notifyUpdate();
    }

    @Nullable
    public BlockState getCamouflagedBottomState() {
        return this.camouflagedBottomState;
    }

    public void setCamouflagedBottomState(@Nullable BlockState state) {
        this.camouflagedBottomState = state;
        if (this.appliedAsFullBlock && (this.camouflagedBottomState == null || this.camouflagedTopState == null || !this.camouflagedBottomState.equals(this.camouflagedTopState))) {
            this.appliedAsFullBlock = false;
            this.fullBlockSource = null;
        }
        setChanged();
        requestModelDataUpdate();
        notifyUpdate();
    }

    @Nullable
    public BlockState getCamouflagedTopState() {
        return this.camouflagedTopState;
    }

    public void setCamouflagedTopState(@Nullable BlockState state) {
        this.camouflagedTopState = state;
        if (this.appliedAsFullBlock && (this.camouflagedBottomState == null || this.camouflagedTopState == null || !this.camouflagedTopState.equals(this.camouflagedBottomState))) {
            this.appliedAsFullBlock = false;
            this.fullBlockSource = null;
        }
        setChanged();
        requestModelDataUpdate();
        notifyUpdate();
    }

    @Nullable
    public BlockState getCamouflagedBottom() {
        return getCamouflagedBottomState();
    }

    public void setCamouflagedBottom(@Nullable BlockState state) {
        setCamouflagedBottomState(state);
    }

    @Nullable
    public BlockState getCamouflagedTop() {
        return getCamouflagedTopState();
    }

    public void setCamouflagedTop(@Nullable BlockState state) {
        setCamouflagedTopState(state);
    }

    public boolean isAppliedAsFullBlock() {
        return this.appliedAsFullBlock;
    }

    public void setAppliedAsFullBlock(boolean appliedAsFullBlock) {
        this.appliedAsFullBlock = appliedAsFullBlock;
        setChanged();
        notifyUpdate();
    }

    @Nullable
    public Block getFullBlockSource() {
        return this.fullBlockSource;
    }

    public void setFullBlockSource(@Nullable Block fullBlockSource) {
        this.fullBlockSource = fullBlockSource;
        setChanged();
        notifyUpdate();
    }

    public boolean hasAnyCamo() {
        return this.camouflagedBottomState != null || this.camouflagedTopState != null;
    }

    public ItemStack getFullBlockDropStack() {
        if (this.fullBlockSource != null) {
            return new ItemStack(this.fullBlockSource);
        }
        if (this.camouflagedBottomState != null) {
            Block b = this.camouflagedBottomState.getBlock();
            Block source = ElevatorSlabBlock.findSourceBlock(b);
            return new ItemStack(source != null ? source : b.asItem());
        }
        if (this.camouflagedTopState != null) {
            Block b = this.camouflagedTopState.getBlock();
            Block source = ElevatorSlabBlock.findSourceBlock(b);
            return new ItemStack(source != null ? source : b.asItem());
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public BlockState getAdaptedTopCamo() {
        if (this.camouflagedBottomState != null) {
            if (this.camouflagedBottomState.hasProperty(SlabBlock.TYPE)) {
                return this.camouflagedBottomState.setValue(SlabBlock.TYPE, SlabType.TOP);
            }
            return this.camouflagedBottomState;
        }
        return null;
    }

    @Nullable
    public BlockState getAdaptedBottomCamo() {
        if (this.camouflagedTopState != null) {
            if (this.camouflagedTopState.hasProperty(SlabBlock.TYPE)) {
                return this.camouflagedTopState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            }
            return this.camouflagedTopState;
        }
        return null;
    }

    public void clearAllCamo() {
        this.camouflagedBottomState = null;
        this.camouflagedTopState = null;
        this.appliedAsFullBlock = false;
        this.fullBlockSource = null;
        setChanged();
        requestModelDataUpdate();
        notifyUpdate();
    }

    @Nullable
    public BlockState getCamouflagedBlock() {
        BlockState state = getBlockState();
        if (state.hasProperty(SlabBlock.TYPE)) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            if (type == SlabType.TOP) {
                return this.camouflagedTopState;
            } else if (type == SlabType.BOTTOM) {
                return this.camouflagedBottomState;
            }
        }
        return this.camouflagedBottomState != null ? this.camouflagedBottomState : this.camouflagedTopState;
    }

    public void setCamouflagedBlock(@Nullable BlockState camouflagedBlock) {
        this.camouflagedBottomState = camouflagedBlock;
        this.camouflagedTopState = camouflagedBlock;
        setChanged();
        requestModelDataUpdate();
        notifyUpdate();
    }

    public boolean setCamoAndUpdate(@Nullable BlockState newCamo) {
        setCamouflagedBlock(newCamo);
        return true;
    }

    public void notifyUpdate() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                Packet<?> packet = getUpdatePacket();
                if (packet != null) {
                    serverLevel.getChunkSource().chunkMap.getPlayers(new net.minecraft.world.level.ChunkPos(this.worldPosition), false)
                            .forEach(p -> p.connection.send(packet));
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.elevatorid.elevator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ElevatorOptionsMenu(containerId, playerInventory, this.worldPosition);
    }
}
