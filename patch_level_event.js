const fs = require('fs');

const paths = [
    'd:/openslabs Elevator/fabric-1.21.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java',
    'd:/openslabs Elevator/neoforge-1.21.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java',
    'd:/openslabs Elevator/forge-1.20.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java'
];

paths.forEach(path => {
    if (fs.existsSync(path)) {
        let content = fs.readFileSync(path, 'utf8');
        
        let brokeTopStr = BlockState brokenVisualState = brokeTop ? elevatorBe.getCamouflagedTopState() : elevatorBe.getCamouflagedBottomState();
                    if (brokenVisualState == null || brokenVisualState.isAir()) {
                        brokenVisualState = state;
                    }
                    level.levelEvent(player, 2001, pos, Block.getId(brokenVisualState));;
                    
        let brokeTopStrForge = BlockState brokenVisualState = brokeTop ? elevatorBe.getCamouflagedTopState() : elevatorBe.getCamouflagedBottomState();
                    if (brokenVisualState == null || brokenVisualState.isAir()) {
                        brokenVisualState = state;
                    }
                    level.levelEvent(player, 2001, pos, Block.getId(brokenVisualState));;
        
        // NeoForge uses getCamouflagedTop() / getCamouflagedBottom()
        if (path.includes('neoforge') || path.includes('forge')) {
            brokeTopStrForge = BlockState brokenVisualState = brokeTop ? elevatorBe.getCamouflagedTop() : elevatorBe.getCamouflagedBottom();
                    if (brokenVisualState == null || brokenVisualState.isAir()) {
                        brokenVisualState = state;
                    }
                    level.levelEvent(player, 2001, pos, Block.getId(brokenVisualState));;
        }

        content = content.replace('level.levelEvent(player, 2001, pos, Block.getId(state));', path.includes('fabric') ? brokeTopStr : brokeTopStrForge);
        fs.writeFileSync(path, content, 'utf8');
        console.log('Patched ' + path);
    }
});
