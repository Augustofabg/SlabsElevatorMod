const fs = require('fs');

const paths = [
    'd:/openslabs Elevator/fabric-1.21.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java',
    'd:/openslabs Elevator/neoforge-1.21.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java',
    'd:/openslabs Elevator/forge-1.20.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java'
];

const replacement =     private static final java.util.Map<Block, SlabBlock> SLAB_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    @Nullable
    public static SlabBlock findCorrespondingSlab(Block block) {
        if (block == null) return null;
        if (block instanceof ElevatorSlabBlock || block instanceof ElevatorBlock) {
            return null;
        }
        if (block instanceof SlabBlock slab) {
            return slab;
        }
        if (SLAB_CACHE.containsKey(block)) {
            return SLAB_CACHE.get(block);
        }

        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        if (key == null) {
            return null;
        }

        String namespace = key.getNamespace();
        String path = key.getPath();

        List<String> candidates = new ArrayList<>();
        // Direct suffix: e.g. stone -> stone_slab
        candidates.add(path + "_slab");

        if (path.endsWith("s")) {
            candidates.add(path.substring(0, path.length() - 1) + "_slab");
        }
        if (path.endsWith("_planks")) {
            candidates.add(path.substring(0, path.length() - 7) + "_slab");
        }
        if (path.endsWith("_block")) {
            candidates.add(path.substring(0, path.length() - 6) + "_slab");
        }
        candidates.add(path.replace("bricks", "brick") + "_slab");
        candidates.add(path.replace("tiles", "tile") + "_slab");

        for (String candidate : candidates) {
            ResourceLocation candidateKey = ResourceLocation.fromNamespaceAndPath(namespace, candidate);
            if (BuiltInRegistries.BLOCK.containsKey(candidateKey)) {
                Block b = BuiltInRegistries.BLOCK.get(candidateKey);
                if (b instanceof SlabBlock slab) {
                    SLAB_CACHE.put(block, slab);
                    return slab;
                }
            }
        }
        
        SLAB_CACHE.put(block, null); // Cache failures too to prevent constant lookups
        return null;
    };

paths.forEach(path => {
    if (fs.existsSync(path)) {
        let content = fs.readFileSync(path, 'utf8');
        
        // Find the method start to the end of the method
        const methodStart = content.indexOf('    public static SlabBlock findCorrespondingSlab(Block block) {');
        if (methodStart !== -1) {
            // Find the end of the method
            let methodEnd = content.indexOf('    public boolean setCamouflage(', methodStart);
            if (methodEnd === -1) {
                methodEnd = content.indexOf('    public void setCamouflage(', methodStart);
            }
            if (methodEnd !== -1) {
                // Find the @Nullable above it
                const nullableStart = content.lastIndexOf('    @Nullable', methodStart);
                if (nullableStart !== -1) {
                    content = content.substring(0, nullableStart) + replacement + '\n\n' + content.substring(methodEnd);
                    fs.writeFileSync(path, content, 'utf8');
                    console.log('Patched ' + path);
                }
            }
        }
    }
});
