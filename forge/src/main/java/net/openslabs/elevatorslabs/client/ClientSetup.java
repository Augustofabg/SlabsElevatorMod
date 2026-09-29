package net.openslabs.elevatorslabs.client;

import com.vsngarcia.ElevatorBlockBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
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
 * Client-side mod bus event subscriber for Forge 1.20.1.
 */
@Mod.EventBusSubscriber(modid = ElevatorSlabsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private static final ThreadLocal<Boolean> COLOR_RECURSION_GUARD = ThreadLocal.withInitial(() -> false);

    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ELEVATOR_OPTIONS_MENU.get(), ElevatorOptionsScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ELEVATOR_SLAB_BLOCK_ENTITY.get(), ElevatorSlabBER::new);
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        for (Map.Entry<ResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
            ResourceLocation id = entry.getKey();
            if (id.getNamespace().equals(ElevatorSlabsMod.MOD_ID) && id.getPath().contains("elevator_slab_")) {
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
            if (COLOR_RECURSION_GUARD.get() || level == null || pos == null) {
                return -1;
            }

            try {
                COLOR_RECURSION_GUARD.set(true);

                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ElevatorSlabBlockEntity tile) {
                    BlockState camo = tile.getCamouflagedBlock();
                    if (camo != null) {
                        if (camo.getBlock() instanceof ElevatorSlabBlock
                                || camo.getBlock() instanceof ElevatorBlockBase
                                || camo.getBlock() == state.getBlock()) {
                            return -1;
                        }

                        return Minecraft.getInstance().getBlockColors().getColor(camo, level, pos, tintIndex);
                    }
                }
            } catch (Throwable ignored) {
                return -1;
            } finally {
                COLOR_RECURSION_GUARD.set(false);
            }

            return -1;
        }, ModBlocks.SLABS_BY_COLOR.values().stream().map(RegistryObject<ElevatorSlabBlock>::get).toArray(Block[]::new));
    }
}
