package net.openslabs.elevatorslabs.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void elevatorslabs$interceptDoubleSlabBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Level level = this.minecraft.level;
        Player player = this.minecraft.player;
        if (level == null || player == null) return;

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ElevatorSlabBlock && state.hasProperty(ElevatorSlabBlock.TYPE)) {
            if (state.getValue(ElevatorSlabBlock.TYPE) == SlabType.DOUBLE) {
                // Calculate which half is being broken
                Boolean brokeTopOpt = ElevatorSlabBlock.getBrokeTop(
                        pos,
                        player.getEyePosition(),
                        player.getViewVector(1.0F),
                        this.minecraft.hitResult
                );

                if (brokeTopOpt != null) {
                    boolean brokeTop = brokeTopOpt;
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    if (blockEntity instanceof ElevatorSlabBlockEntity elevatorBe) {
                        BlockState brokenVisualState = brokeTop ? elevatorBe.getCamouflagedTopState() : elevatorBe.getCamouflagedBottomState();
                        if (brokenVisualState == null || brokenVisualState.isAir()) {
                            brokenVisualState = state;
                        }

                        // Play break sound and emit particles locally (predicting the server's levelEvent)
                        level.levelEvent(2001, pos, Block.getId(brokenVisualState));

                        // 1. Update BlockEntity data BEFORE state changes (prevents flickering)
                        elevatorBe.batchClearOnBreak(brokeTop);

                        // 2. Calculate and apply the new state locally without deleting the BlockEntity
                        BlockState newState = state.setValue(ElevatorSlabBlock.TYPE, brokeTop ? SlabType.BOTTOM : SlabType.TOP);
                        
                        // Flag 11 is used by vanilla (UPDATE_ALL_IMMEDIATE = 1 | 2 | 8) for client-side destroy predictions.
                        boolean success = level.setBlock(pos, newState, 11);

                        // Return false overall to cancel Vanilla's AIR replacement, but complete the method
                        cir.setReturnValue(success);
                    }
                }
            }
        }
    }
}
