package net.openslabs.elevatorslabs.util;

import com.vsngarcia.fabric.ElevatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.init.ModTags;
import org.jetbrains.annotations.Nullable;

/**
 * Utility helper that unifies elevator detection, color extraction,
 * height offset calculations, and safe destination verification for Fabric 1.21.1.
 * Ported from NeoForge with ElevatorBlock (Fabric) instead of ElevatorBlockBase (NeoForge).
 */
public final class ElevatorSearchHelper {

    private ElevatorSearchHelper() {}

    /**
     * Checks if a given BlockState is recognized as an elevator.
     */
    public static boolean isElevator(@Nullable BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }

        if (state.getBlock() instanceof ElevatorSlabBlock) {
            return true;
        }

        if (state.getBlock() instanceof ElevatorBlock) {
            return true;
        }

        return state.is(ModTags.Blocks.ELEVATOR_SLABS)
                || state.is(ModTags.Blocks.ELEVATORID_ELEVATORS)
                || state.is(ModTags.Blocks.COMMON_ELEVATORS);
    }

    /**
     * Extracts the DyeColor associated with the elevator block.
     */
    @Nullable
    public static DyeColor getColor(@Nullable BlockState state) {
        if (state == null) {
            return null;
        }

        if (state.getBlock() instanceof ElevatorSlabBlock slab) {
            return slab.getColor();
        }

        if (state.getBlock() instanceof ElevatorBlock elevator) {
            return elevator.getColor();
        }

        return null;
    }

    /**
     * Calculates the Y offset for player positioning:
     * - Bottom slab (BOTTOM) -> 0.5
     * - Top slab (TOP) or double slab (DOUBLE) -> 1.0
     * - Standard full elevator block -> 1.0
     */
    public static double getYOffset(BlockState state) {
        if (state.getBlock() instanceof SlabBlock) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            if (type == SlabType.BOTTOM) {
                return 0.5D;
            }
            return 1.0D;
        }
        return 1.0D;
    }

    /**
     * Determines whether the target position is safe for the player to teleport to.
     */
    public static boolean isSafeDestination(Level level, @Nullable Entity entity, double x, double feetY, double z, BlockPos toPos) {
        double width = entity != null ? entity.getBbWidth() : 0.6D;
        double height = entity != null ? entity.getBbHeight() : 1.8D;
        double halfWidth = width / 2.0D;

        AABB targetBox = new AABB(
                x - halfWidth, feetY, z - halfWidth,
                x + halfWidth, feetY + height, z + halfWidth
        );

        if (!level.noCollision(entity, targetBox)) {
            return false;
        }

        double eyeHeight = entity != null ? entity.getEyeHeight() : 1.62D;
        BlockPos eyePos = BlockPos.containing(x, feetY + eyeHeight, z);
        if (level.getBlockState(eyePos).isSuffocating(level, eyePos)) {
            return false;
        }

        BlockPos bodyPos = BlockPos.containing(x, feetY + 0.2D, z);
        if (bodyPos.getY() != toPos.getY() && level.getBlockState(bodyPos).isSuffocating(level, bodyPos)) {
            return false;
        }

        return true;
    }

    /**
     * Finds the elevator block the player is currently standing on.
     */
    @Nullable
    public static BlockPos getOriginElevator(Player player) {
        Level level = player.level();
        BlockPos feetPos = BlockPos.containing(player.getX(), player.getY(), player.getZ());

        BlockState stateAtFeet = level.getBlockState(feetPos);
        if (isElevator(stateAtFeet)) {
            double surfaceY = feetPos.getY() + getYOffset(stateAtFeet);
            if (Math.abs(player.getY() - surfaceY) < 0.25D) {
                return feetPos;
            }
        }

        BlockPos belowPos = feetPos.below();
        BlockState stateBelow = level.getBlockState(belowPos);
        if (isElevator(stateBelow)) {
            double surfaceY = belowPos.getY() + getYOffset(stateBelow);
            if (Math.abs(player.getY() - surfaceY) < 0.25D) {
                return belowPos;
            }
        }

        if (isElevator(stateAtFeet)) return feetPos;
        if (isElevator(stateBelow)) return belowPos;

        return null;
    }

    public record TargetResult(BlockPos pos, BlockState state, double yOffset) {}

    /**
     * Scans vertically in the specified direction to find the next valid elevator.
     */
    @Nullable
    public static TargetResult findTargetElevator(Level level, BlockPos originPos, Direction facing,
                                                  @Nullable DyeColor originColor, int maxRange, boolean sameColor) {
        BlockPos.MutableBlockPos currentPos = originPos.mutable();

        while (true) {
            currentPos.setY(currentPos.getY() + facing.getStepY());

            if (level.isOutsideBuildHeight(currentPos) || Math.abs(currentPos.getY() - originPos.getY()) > maxRange) {
                break;
            }

            BlockState state = level.getBlockState(currentPos);
            if (isElevator(state)) {
                DyeColor targetColor = getColor(state);

                if (!sameColor || (originColor != null && originColor == targetColor)) {
                    double yOffset = getYOffset(state);
                    double destX = currentPos.getX() + 0.5D;
                    double destY = currentPos.getY() + yOffset;
                    double destZ = currentPos.getZ() + 0.5D;

                    if (isSafeDestination(level, null, destX, destY, destZ, currentPos)) {
                        return new TargetResult(currentPos.immutable(), state, yOffset);
                    }
                }
            }
        }

        return null;
    }
}
