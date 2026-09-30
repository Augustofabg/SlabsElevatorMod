const fs = require('fs');

const paths = [
    'd:/openslabs Elevator/fabric-1.21.1/src/main/java/net/openslabs/elevatorslabs/client/gui/ElevatorOptionsScreen.java',
    'd:/openslabs Elevator/neoforge-1.21.1/src/main/java/net/openslabs/elevatorslabs/client/gui/ElevatorOptionsScreen.java',
    'd:/openslabs Elevator/forge-1.20.1/src/main/java/net/openslabs/elevatorslabs/client/gui/ElevatorOptionsScreen.java'
];

const replacement =         boolean hasCamo = false;
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
        };

paths.forEach(path => {
    if (fs.existsSync(path)) {
        let content = fs.readFileSync(path, 'utf8');
        
        // Replace in init()
        content = content.replace('boolean hasCamo = tile != null && tile.getCamouflagedBlock() != null;', replacement);
        
        // Replace in containerTick()
        content = content.replace('this.resetCamoButton.active = (tile.getCamouflagedBlock() != null);', replacement + '\n            this.resetCamoButton.active = hasCamo;');
        
        fs.writeFileSync(path, content, 'utf8');
        console.log(Patched );
    }
});
