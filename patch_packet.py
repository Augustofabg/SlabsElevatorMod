import os
import re

paths = [
    r'd:\openslabs Elevator\fabric-1.21.1\src\main\java\net\openslabs\elevatorslabs\network\UpdateSlabOptionsHandler.java',
    r'd:\openslabs Elevator\neoforge-1.21.1\src\main\java\net\openslabs\elevatorslabs\network\UpdateSlabOptionsHandler.java',
    r'd:\openslabs Elevator\forge-1.20.1\src\main\java\net\openslabs\elevatorslabs\network\UpdateSlabOptionsPacket.java'
]

for path in paths:
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Find the block inside if (msg.resetCamo) or if (payload.resetCamo())
        # We replace the } else { if (slabEntity.isLastTargetedTopHalf()) { ... } else { ... } }
        
        replacement = '''                } else {
                    net.minecraft.world.level.block.state.BlockState currentState = level.getBlockState(pos);
                    net.minecraft.world.level.block.state.properties.SlabType type = currentState.getValue(net.minecraft.world.level.block.SlabBlock.TYPE);
                    boolean targetTop = slabEntity.isLastTargetedTopHalf();
                    if (type == net.minecraft.world.level.block.state.properties.SlabType.TOP) targetTop = true;
                    if (type == net.minecraft.world.level.block.state.properties.SlabType.BOTTOM) targetTop = false;

                    if (targetTop) {
                        net.minecraft.world.level.block.state.BlockState oldTop = slabEntity.getCamouflagedTop();
                        slabEntity.setCamouflagedTop(null);
                        if (!player.isCreative() && oldTop != null) {
                            net.minecraft.world.item.ItemStack returnStack = new net.minecraft.world.item.ItemStack(oldTop.getBlock().asItem());
                            if (!returnStack.isEmpty()) {
                                if (!player.getInventory().add(returnStack)) {
                                    player.drop(returnStack, false);
                                }
                            }
                        }
                    } else {
                        net.minecraft.world.level.block.state.BlockState oldBottom = slabEntity.getCamouflagedBottom();
                        slabEntity.setCamouflagedBottom(null);
                        if (!player.isCreative() && oldBottom != null) {
                            net.minecraft.world.item.ItemStack returnStack = new net.minecraft.world.item.ItemStack(oldBottom.getBlock().asItem());
                            if (!returnStack.isEmpty()) {
                                if (!player.getInventory().add(returnStack)) {
                                    player.drop(returnStack, false);
                                }
                            }
                        }
                    }
                }'''

        # We will use simple string replacement since the structure is identical in all 3 files.
        old_str = '''                } else {
                    if (slabEntity.isLastTargetedTopHalf()) {
                        BlockState oldTop = slabEntity.getCamouflagedTop();
                        slabEntity.setCamouflagedTop(null);
                        if (!player.isCreative() && oldTop != null) {
                            ItemStack returnStack = new ItemStack(oldTop.getBlock().asItem());
                            if (!returnStack.isEmpty()) {
                                if (!player.getInventory().add(returnStack)) {
                                    player.drop(returnStack, false);
                                }
                            }
                        }
                    } else {
                        BlockState oldBottom = slabEntity.getCamouflagedBottom();
                        slabEntity.setCamouflagedBottom(null);
                        if (!player.isCreative() && oldBottom != null) {
                            ItemStack returnStack = new ItemStack(oldBottom.getBlock().asItem());
                            if (!returnStack.isEmpty()) {
                                if (!player.getInventory().add(returnStack)) {
                                    player.drop(returnStack, false);
                                }
                            }
                        }
                    }
                }'''
        
        # Replace and handle BlockState / ItemStack imports if needed by using fully qualified names in replacement
        content = content.replace(old_str, replacement)
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'Patched {path}')
