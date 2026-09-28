package net.openslabs.elevatorslabs.client;

import com.vsngarcia.Config;
import com.vsngarcia.ElevatorBlockBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.network.TeleportSlabPayload;
import net.openslabs.elevatorslabs.util.ElevatorSearchHelper;

/**
 * Client-side input event handler detecting Space (jump) and Shift (sneak)
 * on elevator slabs or original elevator blocks, triggering vertical teleportation.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = ElevatorSlabsMod.MOD_ID)
public final class ElevatorSlabsClientHandler {

    private static boolean lastSneaking;
    private static boolean lastJumping;

    private ElevatorSlabsClientHandler() {}

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        handleInput();
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Post event) {
        handleInput();
    }

    private static void handleInput() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator() || !player.isAlive() || player.input == null) {
            return;
        }

        // Detect crouch / sneak (Shift) -> Go DOWN
        boolean sneaking = player.input.shiftKeyDown;
        if (lastSneaking != sneaking) {
            lastSneaking = sneaking;
            if (sneaking) {
                tryTeleport(player, Direction.DOWN);
            }
        }

        // Detect jump (Space) -> Go UP
        boolean jumping = player.input.jumping;
        if (lastJumping != jumping) {
            lastJumping = jumping;
            if (jumping) {
                tryTeleport(player, Direction.UP);
            }
        }
    }

    private static void tryTeleport(LocalPlayer player, Direction facing) {
        Level level = player.level();
        BlockPos fromPos = ElevatorSearchHelper.getOriginElevator(player);
        if (fromPos == null) {
            return;
        }

        BlockState fromState = level.getBlockState(fromPos);
        DyeColor fromColor = ElevatorSearchHelper.getColor(fromState);

        int maxRange = 256;
        boolean sameColor = true;
        try {
            maxRange = Config.GENERAL.range.get();
            sameColor = Config.GENERAL.sameColor.get();
        } catch (Throwable ignored) {}

        ElevatorSearchHelper.TargetResult target =
                ElevatorSearchHelper.findTargetElevator(level, fromPos, facing, fromColor, maxRange, sameColor);

        if (target != null) {
            BlockState toState = target.state();

            // Bidirectional Interoperability:
            // If both origin and target are standard ElevatorBlock (full block), let elevatorid handle it.
            // If either origin or target is an ElevatorSlabBlock, handle via our TeleportSlabPayload.
            boolean bothStandardBlocks = (fromState.getBlock() instanceof ElevatorBlockBase)
                    && (toState.getBlock() instanceof ElevatorBlockBase);

            if (!bothStandardBlocks || fromState.getBlock() instanceof ElevatorSlabBlock || toState.getBlock() instanceof ElevatorSlabBlock) {
                PacketDistributor.sendToServer(new TeleportSlabPayload(fromPos, target.pos()));
            }
        }
    }
}
