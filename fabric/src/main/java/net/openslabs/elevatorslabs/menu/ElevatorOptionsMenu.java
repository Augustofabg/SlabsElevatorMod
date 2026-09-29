package net.openslabs.elevatorslabs.menu;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;
import net.openslabs.elevatorslabs.init.ModMenus;

import org.jetbrains.annotations.Nullable;

/**
 * Container menu for Elevator Options GUI on Fabric 1.21.1.
 */
public class ElevatorOptionsMenu extends AbstractContainerMenu {

    public record PosPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<PosPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "menu_pos"));
        public static final StreamCodec<ByteBuf, PosPayload> PACKET_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, PosPayload::pos,
                PosPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private final BlockPos pos;
    private final ContainerLevelAccess access;
    private final Direction playerFacing;
    @Nullable
    private final ElevatorSlabBlockEntity blockEntity;

    public ElevatorOptionsMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenus.ELEVATOR_OPTIONS_MENU, containerId);
        this.pos = pos;
        Level level = playerInventory.player.level();
        this.access = ContainerLevelAccess.create(level, pos);
        this.playerFacing = playerInventory.player.getDirection();

        if (level.getBlockEntity(pos) instanceof ElevatorSlabBlockEntity tile) {
            this.blockEntity = tile;
        } else {
            this.blockEntity = null;
        }
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getPlayerFacing() {
        return this.playerFacing;
    }

    @Nullable
    public ElevatorSlabBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, blockPos) ->
                level.getBlockState(blockPos).getBlock() instanceof ElevatorSlabBlock
                        && player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D,
                true
        );
    }
}
