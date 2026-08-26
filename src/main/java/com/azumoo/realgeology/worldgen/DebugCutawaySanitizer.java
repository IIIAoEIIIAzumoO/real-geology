package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeologyConfig;
import com.azumoo.realgeology.compat.GameCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clears debug cutaway volumes and adjacent fluid columns after all other
 * worldgen features (trees, snow, grass, structures) have run.
 */
public final class DebugCutawaySanitizer {
    private DebugCutawaySanitizer() { }

    /** Sanitize the 16×16 columns of the chunk containing {@code origin}. */
    public static void sanitizeChunk(LevelAccessor level, BlockPos origin, int blockFlags) {
        RealGeologyConfig.WorldgenDebugMode mode = RealGeologyConfig.worldgenDebugMode();
        if (!mode.isActive()) return;
        int minX = origin.getX() & ~15;
        int minZ = origin.getZ() & ~15;
        sanitizeRegion(level, minX, minZ, minX + 15, minZ + 15, blockFlags);
    }

    /** Sanitize every column in an inclusive block-coordinate rectangle. */
    public static void sanitizeRegion(LevelAccessor level, int minX, int minZ, int maxX, int maxZ, int blockFlags) {
        RealGeologyConfig.WorldgenDebugMode mode = RealGeologyConfig.worldgenDebugMode();
        if (!mode.isActive()) return;
        int minY = GameCompat.minY(level);
        int maxY = level.getMaxBuildHeight() - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean cut = mode.cutsAt(x, z);
                boolean stripFluids = mode.stripsFluidsAt(x, z);
                if (!cut && !stripFluids) continue;
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (cut) {
                        if (!shouldClear(state.getBlock(), mode)) continue;
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), blockFlags);
                    } else if (isFluid(state)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), blockFlags);
                    }
                }
            }
        }
    }

    static boolean shouldClear(Block block, RealGeologyConfig.WorldgenDebugMode mode) {
        if (block == Blocks.BEDROCK) return false;
        return mode != RealGeologyConfig.WorldgenDebugMode.ORES || !isOre(block);
    }

    static boolean isOre(Block block) {
        String path = GameCompat.blockPath(block);
        return path.endsWith("_ore") || path.contains("_ore_");
    }

    static boolean isFluid(BlockState state) {
        return !state.getFluidState().isEmpty();
    }
}
