const fs = require('fs');

const paths = [
    'd:/openslabs Elevator/neoforge-1.21.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java',
    'd:/openslabs Elevator/forge-1.20.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java'
];

paths.forEach(path => {
    if (fs.existsSync(path)) {
        let content = fs.readFileSync(path, 'utf8');
        
        let replacement =     private static final java.util.Map<Block, SlabBlock> SLAB_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    @Nullable
    public static SlabBlock findCorrespondingSlab(Block block) {
        if (block == null) return null;
        if (block instanceof ElevatorSlabBlock || block instanceof ElevatorBlockBase) {
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
        candidates.add(path + "_slab");

        if (path.endsWith("_planks")) {
            candidates.add(path.replace("_planks", "_slab"));
        }
        if (path.endsWith("_bricks")) {
            candidates.add(path.substring(0, path.length() - 1) + "_slab");
        }
        if (path.endsWith("_tiles")) {
            candidates.add(path.substring(0, path.length() - 1) + "_slab");
        }
        if (path.endsWith("_block")) {
            candidates.add(path.substring(0, path.length() - 6) + "_slab");
        }

        for (String candidate : candidates) {
            ResourceLocation candidateLoc = ResourceLocation.fromNamespaceAndPath(namespace, candidate);
            if (BuiltInRegistries.BLOCK.containsKey(candidateLoc)) {
                Block candidateBlock = BuiltInRegistries.BLOCK.get(candidateLoc);
                if (candidateBlock instanceof SlabBlock foundSlab
                        && !(candidateBlock instanceof ElevatorSlabBlock)
                        && !(candidateBlock instanceof ElevatorBlockBase)) {
                    SLAB_CACHE.put(block, foundSlab);
                    return foundSlab;
                }
            }
        }
        SLAB_CACHE.put(block, null);
        return null;
    }
};
        // For forge, it's ElevatorBlock instead of ElevatorBlockBase
        if (path.includes('forge-1.20.1')) {
            replacement = replacement.replace(/ElevatorBlockBase/g, 'ElevatorBlock');
            // Also forge uses ForgeRegistries.BLOCKS.getKey
            replacement = replacement.replace(/BuiltInRegistries\.BLOCK/g, 'net.minecraftforge.registries.ForgeRegistries.BLOCKS');
            replacement = replacement.replace(/ResourceLocation\.fromNamespaceAndPath/g, 'new ResourceLocation');
        }

        // Find the start
        const startStr = '    @Nullable\n    public static SlabBlock findCorrespondingSlab(Block block) {';
        const startIdx = content.lastIndexOf(startStr);
        if (startIdx !== -1) {
            const endIdx = content.lastIndexOf('}');
            content = content.substring(0, startIdx) + replacement + '\n}\n';
            fs.writeFileSync(path, content, 'utf8');
            console.log('Patched ' + path);
        }
    }
});
