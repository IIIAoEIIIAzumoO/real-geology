package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeology;
import com.azumoo.realgeology.compat.GameCompat;
import com.azumoo.realgeology.RealGeologyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Chooses a dry, generated-terrain spawn for disposable debug worlds. */
public final class TestWorldLandSpawn {
    private static final int SEA_LEVEL = 63;
    private static final int STEP = 64;
    private static final int MAX_RADIUS = 4096;
    private static final float FACE_WEST_YAW = 90f;

    private static final int CUT_SPAWN_X_MIN = 100;
    private static final int CUT_SPAWN_X_MAX = 500;
    private static final int CUT_SPAWN_Z_MIN = -256;
    private static final int CUT_SPAWN_Z_MAX = 256;
    private static final int CUT_SPAWN_STEP = 32;
    private static final int CUT_SPAWN_FALLBACK_X = 128;

    private static final TagKey<Biome> MOUNTAIN = biomeTag("is_mountain");
    private static final TagKey<Biome> HILL = biomeTag("is_hill");
    private static final TagKey<Biome> PLATEAU = biomeTag("is_plateau");
    private static final TagKey<Biome> AQUATIC = biomeTag("is_aquatic");
    private static final TagKey<Biome> RIVER = biomeTag("is_river");
    private static final TagKey<Biome> BEACH = biomeTag("is_beach");
    private static final TagKey<Biome> TERRALITH_HIGHLANDS = GameCompat.biomeTag("terralith", "highlands");
    private static final TagKey<Biome> TERRALITH_CLIFFS = GameCompat.biomeTag("terralith", "cliffs");

    private TestWorldLandSpawn() { }

    public static void chooseLandSpawn(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        if (RealGeologyConfig.shouldRelocateSpawnNearCut()) {
            chooseHighlandNearCutSpawn(level);
            return;
        }
        if (!RealGeologyConfig.forceCollisionBelt()) return;

        BlockPos current = GameCompat.sharedSpawnPos(level);
        if (isUsableLand(level, current.getX(), current.getZ())) return;

        for (int radius = 0; radius <= MAX_RADIUS; radius += STEP) {
            for (int offset = -radius; offset <= radius; offset += STEP) {
                BlockPos found = firstLand(level,
                        offset, -radius,
                        offset, radius,
                        -radius, offset,
                        radius, offset);
                if (found != null) {
                    GameCompat.setSharedSpawn(level, found, 0f);
                    RealGeology.LOGGER.info("Real Geology fold test moved ocean spawn to dry land at {}, {}, {}",
                            found.getX(), found.getY(), found.getZ());
                    return;
                }
            }
        }
        RealGeology.LOGGER.warn("Real Geology fold test could not find land within {} blocks; keeping vanilla spawn", MAX_RADIUS);
    }

    /** Place spawn on elevated continental land near the cut plane, facing west. */
    private static void chooseHighlandNearCutSpawn(ServerLevel level) {
        RealGeologyConfig.WorldgenDebugMode mode = RealGeologyConfig.worldgenDebugMode();
        BlockPos best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int x = CUT_SPAWN_X_MIN; x <= CUT_SPAWN_X_MAX; x += CUT_SPAWN_STEP) {
            for (int z = CUT_SPAWN_Z_MIN; z <= CUT_SPAWN_Z_MAX; z += CUT_SPAWN_STEP) {
                if (mode.cutsAt(x, z)) continue;
                double score = highlandScore(level, x, z);
                if (score > bestScore) {
                    bestScore = score;
                    best = landPos(level, x, z);
                }
            }
        }

        if (best == null || bestScore < 0d) {
            int fallbackZ = 0;
            for (int z = -128; z <= 128; z += 32) {
                if (isUsableLand(level, CUT_SPAWN_FALLBACK_X, z)) {
                    fallbackZ = z;
                    break;
                }
            }
            best = landPos(level, CUT_SPAWN_FALLBACK_X, fallbackZ);
            if (best == null) {
                RealGeology.LOGGER.warn("Real Geology cut spawn could not find land near X={}; keeping vanilla spawn",
                        CUT_SPAWN_FALLBACK_X);
                return;
            }
            RealGeology.LOGGER.info("Real Geology cut spawn using fallback at {}, {}, {} (facing west)",
                    best.getX(), best.getY(), best.getZ());
        } else {
            RealGeology.LOGGER.info("Real Geology cut spawn moved to highland at {}, {}, {} (score {}, facing west)",
                    best.getX(), best.getY(), best.getZ(), bestScore);
        }
        GameCompat.setSharedSpawn(level, best, FACE_WEST_YAW);
    }

    private static BlockPos landPos(ServerLevel level, int x, int z) {
        int terrainY = terrainHeight(level, x, z);
        if (!isUsableTerrainY(terrainY)) return null;
        return new BlockPos(x, terrainY + 1, z);
    }

    private static double highlandScore(ServerLevel level, int x, int z) {
        int terrainY = terrainHeight(level, x, z);
        if (!isUsableTerrainY(terrainY)) return -1d;

        int west = terrainHeight(level, x - 1, z);
        int east = terrainHeight(level, x + 1, z);
        int north = terrainHeight(level, x, z - 1);
        int south = terrainHeight(level, x, z + 1);
        int relief = Math.max(Math.max(Math.abs(terrainY - west), Math.abs(terrainY - east)),
                Math.max(Math.abs(terrainY - north), Math.abs(terrainY - south)));
        int elevation = terrainY - SEA_LEVEL;

        Holder<Biome> biome = level.getBiome(new BlockPos(x, terrainY, z));
        double score = elevation * 2.5d + relief * 4d;
        if (biome.is(MOUNTAIN) || biome.is(HILL) || biome.is(PLATEAU)) score += 40d;
        if (biome.is(TERRALITH_HIGHLANDS) || biome.is(TERRALITH_CLIFFS)) score += 30d;
        if (biome.is(AQUATIC) || biome.is(BEACH)) score -= 120d;
        if (biome.is(RIVER)) score -= 25d;
        if (elevation >= 35) score += 20d;
        if (relief >= 2) score += 15d;
        return score;
    }

    private static BlockPos firstLand(ServerLevel level, int... coordinates) {
        for (int i = 0; i < coordinates.length; i += 2) {
            int x = coordinates[i], z = coordinates[i + 1];
            BlockPos found = landPos(level, x, z);
            if (found != null) return found;
        }
        return null;
    }

    private static boolean isUsableLand(ServerLevel level, int x, int z) {
        return isUsableTerrainY(terrainHeight(level, x, z));
    }

    private static boolean isUsableTerrainY(int terrainY) {
        return terrainY >= SEA_LEVEL + 7 && terrainY <= 180;
    }

    private static int terrainHeight(ServerLevel level, int x, int z) {
        return level.getChunkSource().getGenerator().getBaseHeight(x, z,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level, level.getChunkSource().randomState());
    }

    private static TagKey<Biome> biomeTag(String path) {
        return GameCompat.biomeTag("c", path);
    }
}
