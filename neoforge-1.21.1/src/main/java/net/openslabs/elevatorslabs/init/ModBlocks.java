package net.openslabs.elevatorslabs.init;

import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registers all 16 color variants of Elevator Slabs matching Minecraft's dye colors.
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ElevatorSlabsMod.MOD_ID);

    private static final Map<DyeColor, DeferredBlock<ElevatorSlabBlock>> SLABS_MAP = new EnumMap<>(DyeColor.class);

    // 16 Elevator Slab color variations
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_WHITE = registerSlab(DyeColor.WHITE);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_ORANGE = registerSlab(DyeColor.ORANGE);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_MAGENTA = registerSlab(DyeColor.MAGENTA);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_LIGHT_BLUE = registerSlab(DyeColor.LIGHT_BLUE);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_YELLOW = registerSlab(DyeColor.YELLOW);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_LIME = registerSlab(DyeColor.LIME);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_PINK = registerSlab(DyeColor.PINK);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_GRAY = registerSlab(DyeColor.GRAY);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_LIGHT_GRAY = registerSlab(DyeColor.LIGHT_GRAY);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_CYAN = registerSlab(DyeColor.CYAN);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_PURPLE = registerSlab(DyeColor.PURPLE);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_BLUE = registerSlab(DyeColor.BLUE);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_BROWN = registerSlab(DyeColor.BROWN);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_GREEN = registerSlab(DyeColor.GREEN);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_RED = registerSlab(DyeColor.RED);
    public static final DeferredBlock<ElevatorSlabBlock> ELEVATOR_SLAB_BLACK = registerSlab(DyeColor.BLACK);

    public static final Map<DyeColor, DeferredBlock<ElevatorSlabBlock>> SLABS_BY_COLOR = Collections.unmodifiableMap(SLABS_MAP);

    private static DeferredBlock<ElevatorSlabBlock> registerSlab(DyeColor color) {
        String name = "elevator_slab_" + color.getName();
        DeferredBlock<ElevatorSlabBlock> block = BLOCKS.register(name, () -> new ElevatorSlabBlock(color));
        SLABS_MAP.put(color, block);
        return block;
    }

    public static DeferredBlock<ElevatorSlabBlock> getByColor(DyeColor color) {
        return SLABS_BY_COLOR.get(color);
    }

    private ModBlocks() {}
}
