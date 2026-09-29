package net.openslabs.elevatorslabs.client;

import xyz.vsngamer.elevatorid.init.ModConfig;
import xyz.vsngamer.elevatorid.blocks.ElevatorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.network.ElevatorSlabsNetwork;
import net.openslabs.elevatorslabs.network.TeleportSlabPacket;
import net.openslabs.elevatorslabs.util.ElevatorSearchHelper;

/**
 * Client-side input event handler detecting Space (jump) and Shift (sneak)
 * on elevator slabs or original elevator blocks for Forge 1.20.1.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ElevatorSlabsMod.MOD_ID)
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
        try {
            maxRange = ModConfig.GENERAL.range.get();
        } catch (Throwable ignored) {}

        // Inter-colors: as 16 cores se comunicam livremente entre si
        ElevatorSearchHelper.TargetResult target =
                ElevatorSearchHelper.findTargetElevator(level, fromPos, facing, fromColor, maxRange, false);

        if (target != null) {
            ElevatorSlabsNetwork.CHANNEL.sendToServer(new TeleportSlabPacket(fromPos, target.pos()));
        }
    }
}
