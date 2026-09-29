package net.openslabs.elevatorslabs.event;

import xyz.vsngamer.elevatorid.blocks.ElevatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.init.ModTags;

/**
 * Event handler for intercepting interactions on original OpenBlocks Elevator blocks for Forge 1.20.1.
 */
@Mod.EventBusSubscriber(modid = ElevatorSlabsMod.MOD_ID)
public final class ElevatorInteractionHandler {

    private ElevatorInteractionHandler() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        // 1. Verify if clicked block belongs to the original mod (full elevator block, not a slab)
        boolean isOriginalElevator = (block instanceof ElevatorBlock && !(block instanceof ElevatorSlabBlock))
                || (state.is(ModTags.Blocks.ELEVATORID_ELEVATORS) && !(block instanceof ElevatorSlabBlock));

        if (!isOriginalElevator) {
            return;
        }

        // 2. Verify if the item held by the player is an Elevator Slab (or its BlockItem)
        ItemStack heldStack = event.getItemStack();
        boolean isHoldingElevatorSlab = heldStack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ElevatorSlabBlock;

        if (!isHoldingElevatorSlab) {
            return;
        }

        // 3. Cancel default item interaction to prevent the elevator from consuming the slab as camo
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // 4. On server side, open the native options screen of the original elevator
        if (!level.isClientSide) {
            Player player = event.getEntity();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MenuProvider menuProvider && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, menuProvider, pos);
            }
        }
    }
}
