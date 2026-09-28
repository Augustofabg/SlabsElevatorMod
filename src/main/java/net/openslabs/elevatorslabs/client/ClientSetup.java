package net.openslabs.elevatorslabs.client;

import com.vsngarcia.ElevatorBlockBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;
import net.openslabs.elevatorslabs.client.gui.ElevatorOptionsScreen;
import net.openslabs.elevatorslabs.client.render.ElevatorSlabBER;
import net.openslabs.elevatorslabs.client.render.ElevatorSlabBakedModel;
import net.openslabs.elevatorslabs.init.ModBlockEntities;
import net.openslabs.elevatorslabs.init.ModBlocks;
import net.openslabs.elevatorslabs.init.ModMenus;

import java.util.Map;

/**
 * Client-side mod bus event subscriber registering GUI screens,
 * BlockEntityRenderer (ElevatorSlabBER), custom camouflage BakedModel, and block color handlers.
 * Implements ThreadLocal recursion protection in registerBlockColors to prevent StackOverflowError.
 */
@EventBusSubscriber(modid = ElevatorSlabsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private static final ThreadLocal<Boolean> COLOR_RECURSION_GUARD = ThreadLocal.withInitial(() -> false);

    private ClientSetup() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ELEVATOR_OPTIONS_MENU.get(), ElevatorOptionsScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ELEVATOR_SLAB_BLOCK_ENTITY.get(), ElevatorSlabBER::new);
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        for (Map.Entry<ModelResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
            ModelResourceLocation mrl = entry.getKey();
            if (mrl.id().getNamespace().equals(ElevatorSlabsMod.MOD_ID) && mrl.id().getPath().contains("elevator_slab_")) {
                BakedModel current = entry.getValue();
                if (!(current instanceof ElevatorSlabBakedModel)) {
                    entry.setValue(new ElevatorSlabBakedModel(current));
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) -> {
            // Guard against recursive evaluations on the same thread
            if (COLOR_RECURSION_GUARD.get() || level == null || pos == null) {
                return -1;
            }

            try {
                COLOR_RECURSION_GUARD.set(true);

                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ElevatorSlabBlockEntity tile) {
                    BlockState camo = tile.getCamouflagedBlock();
                    if (camo != null) {
                        // Guard against recursion: never delegate if camo is an Elevator Slab, Elevator Block, or same block
                        if (camo.getBlock() instanceof ElevatorSlabBlock
                                || camo.getBlock() instanceof ElevatorBlockBase
                                || camo.getBlock() == state.getBlock()) {
                            return -1;
                        }

                        // Safely query the camouflaged block's color handler
                        return Minecraft.getInstance().getBlockColors().getColor(camo, level, pos, tintIndex);
                    }
                }
            } catch (Throwable ignored) {
                return -1;
            } finally {
                COLOR_RECURSION_GUARD.set(false);
            }

            return -1;
        }, ModBlocks.SLABS_BY_COLOR.values().stream().map(DeferredBlock::get).toArray(Block[]::new));
    }
}
