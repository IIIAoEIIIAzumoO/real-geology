package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeology;
import com.azumoo.realgeology.RealGeologyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Chooses a dry, generated-terrain spawn for the disposable fold test only. */
public final class TestWorldLandSpawn {
    private static final int SEA_LEVEL = 63;
    private static final int STEP = 64;
    private static final int MAX_RADIUS = 4096;

    private TestWorldLandSpawn() { }

    public static void chooseLandSpawn(ServerStartedEvent event) {
        if (!RealGeologyConfig.forceCollisionBelt()) return;
        ServerLevel level = event.getServer().overworld();
        BlockPos current = level.getSharedSpawnPos();
        if (isUsableLand(level, current.getX(), current.getZ())) return;

        for (int radius = 0; radius <= MAX_RADIUS; radius += STEP) {
            for (int offset = -radius; offset <= radius; offset += STEP) {
                BlockPos found = firstLand(level,
                        offset, -radius,
                        offset, radius,
                        -radius, offset,
                        radius, offset);
                if (found != null) {
                    level.setDefaultSpawnPos(found, 0f);
                    RealGeology.LOGGER.info("Real Geology fold test moved ocean spawn to dry land at {}, {}, {}",
                            found.getX(), found.getY(), found.getZ());
                    return;
                }
            }
        }
        RealGeology.LOGGER.warn("Real Geology fold test could not find land within {} blocks; keeping vanilla spawn", MAX_RADIUS);
    }

    private static BlockPos firstLand(ServerLevel level, int... coordinates) {
        for (int i = 0; i < coordinates.length; i += 2) {
            int x = coordinates[i], z = coordinates[i + 1];
            int terrainY = level.getChunkSource().getGenerator().getBaseHeight(x, z,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level, level.getChunkSource().randomState());
            // Keep the test spawn on clear land rather than a shoreline,
            // extreme summit, cave opening, or small low island.
            if (terrainY >= SEA_LEVEL + 7 && terrainY <= 180) return new BlockPos(x, terrainY + 1, z);
        }
        return null;
    }

    private static boolean isUsableLand(ServerLevel level, int x, int z) {
        int terrainY = level.getChunkSource().getGenerator().getBaseHeight(x, z,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level, level.getChunkSource().randomState());
        return terrainY >= SEA_LEVEL + 7 && terrainY <= 180;
    }
}
