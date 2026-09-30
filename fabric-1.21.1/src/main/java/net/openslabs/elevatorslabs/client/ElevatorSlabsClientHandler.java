package net.openslabs.elevatorslabs.client;

import com.vsngarcia.fabric.ElevatorBlock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.network.TeleportSlabPayload;
import net.openslabs.elevatorslabs.util.ElevatorSearchHelper;

/**
 * Client-side input event handler detecting Space (jump) and Shift (sneak)
 * on elevator slabs or original elevator blocks for Fabric 1.21.1.
 * Ported from NeoForge using Fabric's ClientTickEvents.
 */
public final class ElevatorSlabsClientHandler {

    private static boolean lastSneaking;
    private static boolean lastJumping;

    private ElevatorSlabsClientHandler() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> handleInput());
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
        try {
            Class<?> cfgClass = Class.forName("com.vsngarcia.Config");
            Object general = cfgClass.getField("GENERAL").get(null);
            Object rangeObj = general.getClass().getField("range").get(general);
            maxRange = (Integer) rangeObj.getClass().getMethod("get").invoke(rangeObj);
        } catch (Throwable ignored) {}

        // Inter-colors: all 16 colors communicate freely
        ElevatorSearchHelper.TargetResult target =
                ElevatorSearchHelper.findTargetElevator(level, fromPos, facing, fromColor, maxRange, false);

        if (target != null) {
            ClientPlayNetworking.send(new TeleportSlabPayload(fromPos, target.pos()));
        }
    }
}
