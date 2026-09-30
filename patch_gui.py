import os
import re

paths = [
    r'd:\openslabs Elevator\fabric-1.21.1\src\main\java\net\openslabs\elevatorslabs\client\gui\ElevatorOptionsScreen.java',
    r'd:\openslabs Elevator\neoforge-1.21.1\src\main\java\net\openslabs\elevatorslabs\client\gui\ElevatorOptionsScreen.java',
    r'd:\openslabs Elevator\forge-1.20.1\src\main\java\net\openslabs\elevatorslabs\client\gui\ElevatorOptionsScreen.java'
]

replacement = '''        boolean hasCamo = false;
        if (tile != null) {
            net.minecraft.world.phys.HitResult hit = net.minecraft.client.Minecraft.getInstance().hitResult;
            boolean topClicked = false;
            if (hit instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                topClicked = (blockHit.getLocation().y - blockHit.getBlockPos().getY()) >= 0.5D;
            }
            net.minecraft.world.level.block.state.properties.SlabType type = tile.getBlockState().getValue(net.minecraft.world.level.block.SlabBlock.TYPE);
            if (type == net.minecraft.world.level.block.state.properties.SlabType.BOTTOM) {
                hasCamo = tile.getCamouflagedBottomState() != null;
            } else if (type == net.minecraft.world.level.block.state.properties.SlabType.TOP) {
                hasCamo = tile.getCamouflagedTopState() != null;
            } else {
                if (tile.isAppliedAsFullBlock()) {
                    hasCamo = tile.getCamouflagedBottomState() != null;
                } else {
                    hasCamo = topClicked ? tile.getCamouflagedTopState() != null : tile.getCamouflagedBottomState() != null;
                }
            }
        }'''

for path in paths:
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Replace in init()
        content = re.sub(r'boolean hasCamo = tile != null && tile\.getCamouflagedBlock\(\) != null;', replacement, content)
        
        # Replace in containerTick()
        content = re.sub(r'this\.resetCamoButton\.active = \(tile\.getCamouflagedBlock\(\) != null\);', 'this.resetCamoButton.active = hasCamo;', content)
        # We need to insert the hasCamo logic inside containerTick too!
        tick_replacement = replacement.replace('boolean hasCamo', 'hasCamo')
        tick_replacement = '        boolean hasCamo = false;\n        if (tile != null) {\n' + tick_replacement.split('if (tile != null) {')[1]
        
        # Replace the simple check in containerTick with the full logic
        content = re.sub(r'this\.resetCamoButton\.active = \(tile\.getCamouflagedBlock\(\) != null\);', tick_replacement + '\n            this.resetCamoButton.active = hasCamo;', content)
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'Patched {path}')
