package net.openslabs.elevatorslabs.init;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.ElevatorSlabBlock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registers all 16 color variants of Elevator Slabs for Forge 1.20.1.
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ElevatorSlabsMod.MOD_ID);

    private static final Map<DyeColor, RegistryObject<ElevatorSlabBlock>> SLABS_MAP = new EnumMap<>(DyeColor.class);

    // 16 Elevator Slab color variations
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_WHITE = registerSlab(DyeColor.WHITE);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_ORANGE = registerSlab(DyeColor.ORANGE);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_MAGENTA = registerSlab(DyeColor.MAGENTA);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_LIGHT_BLUE = registerSlab(DyeColor.LIGHT_BLUE);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_YELLOW = registerSlab(DyeColor.YELLOW);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_LIME = registerSlab(DyeColor.LIME);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_PINK = registerSlab(DyeColor.PINK);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_GRAY = registerSlab(DyeColor.GRAY);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_LIGHT_GRAY = registerSlab(DyeColor.LIGHT_GRAY);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_CYAN = registerSlab(DyeColor.CYAN);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_PURPLE = registerSlab(DyeColor.PURPLE);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_BLUE = registerSlab(DyeColor.BLUE);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_BROWN = registerSlab(DyeColor.BROWN);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_GREEN = registerSlab(DyeColor.GREEN);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_RED = registerSlab(DyeColor.RED);
    public static final RegistryObject<ElevatorSlabBlock> ELEVATOR_SLAB_BLACK = registerSlab(DyeColor.BLACK);

    public static final Map<DyeColor, RegistryObject<ElevatorSlabBlock>> SLABS_BY_COLOR = Collections.unmodifiableMap(SLABS_MAP);

    private static RegistryObject<ElevatorSlabBlock> registerSlab(DyeColor color) {
        String name = "elevator_slab_" + color.getName();
        RegistryObject<ElevatorSlabBlock> block = BLOCKS.register(name, () -> new ElevatorSlabBlock(color));
        SLABS_MAP.put(color, block);
        return block;
    }

    public static RegistryObject<ElevatorSlabBlock> getByColor(DyeColor color) {
        return SLABS_BY_COLOR.get(color);
    }

    private ModBlocks() {}
}
