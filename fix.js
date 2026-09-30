const fs = require('fs');
let content = fs.readFileSync('d:/openslabs Elevator/fabric-1.21.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java', 'utf8');
content = content.replace('SLAB_CACHE.put(block, null);\n        return null;\n    }\n}return null;\n    }\n}', 'SLAB_CACHE.put(block, null);\n        return null;\n    }\n}');
content = content.replace('     *   oak_planks -> oak_slab, stone_bricks -> stone_brick_slab, deepslate_tiles -> deepslate_    private static final java.util.Map<Block, SlabBlock> SLAB_CACHE = new java.util.concurrent.ConcurrentHashMap<>();', '     *   oak_planks -> oak_slab, stone_bricks -> stone_brick_slab, deepslate_tiles -> deepslate_tile_slab).\n     */\n    private static final java.util.Map<Block, SlabBlock> SLAB_CACHE = new java.util.concurrent.ConcurrentHashMap<>();');
fs.writeFileSync('d:/openslabs Elevator/fabric-1.21.1/src/main/java/net/openslabs/elevatorslabs/block/ElevatorSlabBlock.java', content);
