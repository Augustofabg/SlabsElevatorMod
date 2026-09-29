package net.openslabs.elevatorslabs.event;

import com.vsngarcia.fabric.ElevatorBlock;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.init.ModTags;

/**
 * Event handler for intercepting interactions on original OpenBlocks Elevator blocks
 * and handling double elevator slab partial breaking on Fabric 1.21.1.
 */
public final class ElevatorInteractionHandler {

    private ElevatorInteractionHandler() {}

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof ElevatorSlabBlock slabBlock) {
                return slabBlock.onPlayerBreakBlock(level, player, pos, state, blockEntity);
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            boolean isOriginalElevator = (block instanceof ElevatorBlock && !(block instanceof ElevatorSlabBlock))
                    || (state.is(ModTags.Blocks.ELEVATORID_ELEVATORS) && !(block instanceof ElevatorSlabBlock));

            if (!isOriginalElevator) {
                return InteractionResult.PASS;
            }

            ItemStack heldStack = player.getItemInHand(hand);
            boolean isHoldingElevatorSlab = heldStack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof ElevatorSlabBlock;

            if (!isHoldingElevatorSlab) {
                return InteractionResult.PASS;
            }

            // On server side, open native options screen
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MenuProvider menuProvider) {
                    player.openMenu(menuProvider);
                }
            }

            return InteractionResult.SUCCESS;
        });
    }
}
