const fs = require('fs');

const paths = [
    'd:/openslabs Elevator/fabric-1.21.1/src/main/java/net/openslabs/elevatorslabs/network/UpdateSlabOptionsHandler.java',
    'd:/openslabs Elevator/neoforge-1.21.1/src/main/java/net/openslabs/elevatorslabs/network/UpdateSlabOptionsHandler.java',
    'd:/openslabs Elevator/forge-1.20.1/src/main/java/net/openslabs/elevatorslabs/network/UpdateSlabOptionsPacket.java'
];

const replacement =                 } else {
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
                };

const oldStr =                 } else {
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
                };

paths.forEach(path => {
    if (fs.existsSync(path)) {
        let content = fs.readFileSync(path, 'utf8');
        content = content.replace(oldStr, replacement);
        fs.writeFileSync(path, content, 'utf8');
        console.log('Patched ' + path);
    }
});
