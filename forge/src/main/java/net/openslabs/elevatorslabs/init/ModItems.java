package net.openslabs.elevatorslabs.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.item.EnderSpindleItem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registers BlockItems for all 16 Elevator Slab colors for Forge 1.20.1.
 */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ElevatorSlabsMod.MOD_ID);

    private static final Map<DyeColor, RegistryObject<BlockItem>> ITEMS_MAP = new EnumMap<>(DyeColor.class);

    public static final RegistryObject<EnderSpindleItem> ENDER_SPINDLE = ITEMS.register("ender_spindle", () ->
            new EnderSpindleItem(new Item.Properties())
    );

    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_WHITE = registerSlabItem(DyeColor.WHITE);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_ORANGE = registerSlabItem(DyeColor.ORANGE);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_MAGENTA = registerSlabItem(DyeColor.MAGENTA);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_LIGHT_BLUE = registerSlabItem(DyeColor.LIGHT_BLUE);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_YELLOW = registerSlabItem(DyeColor.YELLOW);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_LIME = registerSlabItem(DyeColor.LIME);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_PINK = registerSlabItem(DyeColor.PINK);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_GRAY = registerSlabItem(DyeColor.GRAY);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_LIGHT_GRAY = registerSlabItem(DyeColor.LIGHT_GRAY);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_CYAN = registerSlabItem(DyeColor.CYAN);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_PURPLE = registerSlabItem(DyeColor.PURPLE);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_BLUE = registerSlabItem(DyeColor.BLUE);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_BROWN = registerSlabItem(DyeColor.BROWN);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_GREEN = registerSlabItem(DyeColor.GREEN);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_RED = registerSlabItem(DyeColor.RED);
    public static final RegistryObject<BlockItem> ELEVATOR_SLAB_BLACK = registerSlabItem(DyeColor.BLACK);

    public static final Map<DyeColor, RegistryObject<BlockItem>> SLAB_ITEMS_BY_COLOR = Collections.unmodifiableMap(ITEMS_MAP);

    private static RegistryObject<BlockItem> registerSlabItem(DyeColor color) {
        String name = "elevator_slab_" + color.getName();
        RegistryObject<BlockItem> item = ITEMS.register(name, () ->
                new BlockItem(ModBlocks.getByColor(color).get(), new Item.Properties())
        );
        ITEMS_MAP.put(color, item);
        return item;
    }

    public static RegistryObject<BlockItem> getByColor(DyeColor color) {
        return SLAB_ITEMS_BY_COLOR.get(color);
    }

    private ModItems() {}
}
