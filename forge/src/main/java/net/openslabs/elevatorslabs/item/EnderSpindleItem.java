package net.openslabs.elevatorslabs.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Utility crafting tool used to cut full OpenBlocks Elevators into Elevator Slabs.
 * Has 15 points of durability and takes 1 damage point each time it is used in a crafting grid.
 */
public class EnderSpindleItem extends Item {

    public EnderSpindleItem(Properties properties) {
        super(properties.durability(15));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack copy = itemStack.copy();
        int newDamage = copy.getDamageValue() + 1;
        if (newDamage >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        copy.setDamageValue(newDamage);
        return copy;
    }
}
