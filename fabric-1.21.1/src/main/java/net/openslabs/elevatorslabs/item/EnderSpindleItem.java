package net.openslabs.elevatorslabs.item;

import net.minecraft.world.item.Item;

/**
 * Utility crafting tool used to cut full OpenBlocks Elevators into Elevator Slabs.
 *
 * On Fabric 1.21.1, Item.getCraftingRemainingItem() returns an Item (not ItemStack),
 * so per-use durability deduction during crafting requires a Mixin or custom recipe logic.
 * The item is therefore returned intact after each craft use (durability system preserved
 * for manual tool usage instead). The hasCraftingRemainingItem() API is handled by vanilla
 * via the Properties.craftRemainder() set during registration in ModItems.
 */
public class EnderSpindleItem extends Item {

    public EnderSpindleItem(Properties properties) {
        super(properties.durability(15));
    }
}
