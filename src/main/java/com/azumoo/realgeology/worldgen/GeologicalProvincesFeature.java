package com.azumoo.realgeology.worldgen;

import com.azumoo.realgeology.RealGeology;
import com.azumoo.realgeology.RealGeologyConfig;
import com.azumoo.realgeology.block.HostedOreBlock;
import com.azumoo.realgeology.compat.GameCompat;
import com.azumoo.realgeology.compat.RockPalette;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.concurrent.atomic.AtomicBoolean;

/** Seeded, chunk-order-independent provinces and folded strata. */
public final class GeologicalProvincesFeature extends Feature<NoneFeatureConfiguration> {
    /*
     * These are complete, ordered rock successions rather than the old short
     * arrays which repeated every few blocks.  Thickness varies smoothly over
     * hundreds of blocks, so a bed is a real formation with a finite top and
     * base, not a decorative horizontal stripe.
     */
    private static final StratigraphicColumn SEDIMENTARY_COLUMN = new StratigraphicColumn(
            "gneiss",
            new StratigraphicUnit("conglomerate", 10, 24),
            new StratigraphicUnit("shale", 22, 46),
            new StratigraphicUnit("siltstone", 14, 32),
            new StratigraphicUnit("limestone", 10, 30),
            new StratigraphicUnit("shale", 18, 42),
            new StratigraphicUnit("dolomite", 8, 26),
            new StratigraphicUnit("siltstone", 14, 34),
            new StratigraphicUnit("conglomerate", 8, 22)
    );
    // A younger, mostly flat cover deposited after the older basin was folded
    // and eroded. Its base is an unconformity, not another repeating cycle.
    private static final StratigraphicColumn SEDIMENTARY_COVER_COLUMN = new StratigraphicColumn(
            "conglomerate",
            new StratigraphicUnit("siltstone", 12, 30),
            new StratigraphicUnit("limestone", 8, 24),
            new StratigraphicUnit("shale", 16, 38)
    );
    // These are depositional facies, selected from Terralith's generated
    // biome tags. They deliberately reuse the established GeoStrata rock
    // palette until a missing rock type is added as a controlled RG block.
    private static final StratigraphicColumn MARINE_COLUMN = new StratigraphicColumn(
            "gneiss",
            new StratigraphicUnit("shale", 24, 52),
            new StratigraphicUnit("siltstone", 12, 30),
            new StratigraphicUnit("limestone", 14, 38),
            new StratigraphicUnit("dolomite", 8, 24),
            new StratigraphicUnit("shale", 18, 40)
    );
    private static final StratigraphicColumn ALLUVIAL_COLUMN = new StratigraphicColumn(
            "gneiss",
            new StratigraphicUnit("conglomerate", 12, 30),
            new StratigraphicUnit("siltstone", 18, 42),
            new StratigraphicUnit("shale", 10, 26),
            new StratigraphicUnit("conglomerate", 8, 22)
    );
    private static final StratigraphicColumn ARID_BASIN_COLUMN = new StratigraphicColumn(
            "gneiss",
            new StratigraphicUnit("siltstone", 18, 44),
            new StratigraphicUnit("dolomite", 12, 34),
            new StratigraphicUnit("limestone", 8, 22),
            new StratigraphicUnit("shale", 14, 30),
            new StratigraphicUnit("conglomerate", 8, 24)
    );
    // A passive margin is a sediment wedge: coarse nearshore material below,
    // then a thickening offshore mud/carbonate succession. We use the existing
    // GeoStrata palette until sandstone and mudstone become controlled blocks.
    private static final StratigraphicColumn PASSIVE_MARGIN_COLUMN = new StratigraphicColumn(
            "gneiss",
            new StratigraphicUnit("conglomerate", 8, 22),
            new StratigraphicUnit("siltstone", 20, 48),
            new StratigraphicUnit("shale", 24, 56),
            new StratigraphicUnit("limestone", 10, 30),
            new StratigraphicUnit("shale", 20, 48)
    );
    private static final StratigraphicColumn MOUNTAIN_COLUMN = new StratigraphicColumn(
            "gneiss",
            new StratigraphicUnit("amphibolite", 14, 34),
            new StratigraphicUnit("gneiss", 20, 48),
            new StratigraphicUnit("quartzite", 10, 30),
            new StratigraphicUnit("schist", 18, 42),
            new StratigraphicUnit("marble", 8, 26),
            new StratigraphicUnit("phyllite", 14, 34),
            new StratigraphicUnit("slate", 12, 30),
            // The upper mountain succession must extend through real mountain
            // heights. Otherwise Minecraft terrain above the finite stack
            // would be filled forever with one youngest rock and conceal the
            // folded sequence below.
            new StratigraphicUnit("quartzite", 12, 32),
            new StratigraphicUnit("schist", 18, 40),
            new StratigraphicUnit("amphibolite", 12, 30),
            new StratigraphicUnit("gneiss", 16, 38),
            new StratigraphicUnit("marble", 8, 24),
            new StratigraphicUnit("phyllite", 12, 28)
    );
    // Outer parts of a collision belt can preserve a folded sedimentary cover
    // rather than being metamorphic core everywhere. These are the layers that
    // make folds readable as shale/siltstone/carbonate packages in a cutaway.
    private static final StratigraphicColumn FOLDED_SEDIMENTARY_COLUMN = new StratigraphicColumn(
            "gneiss",
            new StratigraphicUnit("conglomerate", 10, 24),
            new StratigraphicUnit("shale", 18, 40),
            new StratigraphicUnit("siltstone", 12, 30),
            new StratigraphicUnit("limestone", 10, 28),
            new StratigraphicUnit("shale", 16, 38),
            new StratigraphicUnit("dolomite", 8, 24),
            new StratigraphicUnit("siltstone", 12, 30),
            // A finite, thick continental shelf/basin package rather than an
            // endlessly repeated top bed. Its upper formations make the fold
            // anatomy visible right through mountain crests.
            new StratigraphicUnit("conglomerate", 8, 20),
            new StratigraphicUnit("siltstone", 14, 34),
            new StratigraphicUnit("shale", 16, 36),
            new StratigraphicUnit("limestone", 10, 28),
            new StratigraphicUnit("dolomite", 8, 22),
            new StratigraphicUnit("siltstone", 14, 32)
    );
    private static final StratigraphicColumn PLUTONIC_COLUMN = new StratigraphicColumn(
            "gneiss",
            new StratigraphicUnit("amphibolite", 12, 30),
            new StratigraphicUnit("diorite", 18, 42),
            new StratigraphicUnit("gabbro", 16, 38),
            new StratigraphicUnit("granite", 26, 60),
            new StratigraphicUnit("pegmatite", 5, 18)
    );
    private static final StratigraphicColumn VOLCANIC_COLUMN = new StratigraphicColumn(
            "gabbro",
            new StratigraphicUnit("basalt", 18, 42),
            new StratigraphicUnit("tuff", 8, 24),
            new StratigraphicUnit("rhyolite", 12, 34),
            new StratigraphicUnit("basalt", 16, 40),
            new StratigraphicUnit("scoria", 5, 16),
            new StratigraphicUnit("basaltic_glass", 4, 12)
    );
    private static final AtomicBoolean FIRST_PLACEMENT = new AtomicBoolean();
    private static final TagKey<Biome> DRY = biomeTag("is_dry");
    private static final TagKey<Biome> WET = biomeTag("is_wet");
    private static final TagKey<Biome> HOT = biomeTag("is_hot");
    private static final TagKey<Biome> COLD = biomeTag("is_cold");
    private static final TagKey<Biome> MOUNTAIN = biomeTag("is_mountain");
    private static final TagKey<Biome> HILL = biomeTag("is_hill");
    private static final TagKey<Biome> PLATEAU = biomeTag("is_plateau");
    private static final TagKey<Biome> AQUATIC = biomeTag("is_aquatic");
    private static final TagKey<Biome> RIVER = biomeTag("is_river");
    private static final TagKey<Biome> BEACH = biomeTag("is_beach");
    private static final TagKey<Biome> BADLANDS = biomeTag("is_badlands");
    private static final TagKey<Biome> DEEP_OCEAN = GameCompat.biomeTag("minecraft", "is_deep_ocean");
    private static final TagKey<Biome> TERRALITH_HIGHLANDS = GameCompat.biomeTag("terralith", "highlands");
    private static final TagKey<Biome> TERRALITH_CLIFFS = GameCompat.biomeTag("terralith", "cliffs");
    private static final TagKey<Biome> TERRALITH_VOLCANIC = GameCompat.biomeTag("terralith", "volcanic");
    public GeologicalProvincesFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level(); long seed = level.getSeed(); BlockPos o = ctx.origin();
        RealGeologyConfig.WorldgenDebugMode debugMode = RealGeologyConfig.worldgenDebugMode();
        int minX = o.getX() & ~15, minZ = o.getZ() & ~15, minY = Math.max(GameCompat.minY(level), -64);
        if (FIRST_PLACEMENT.compareAndSet(false, true)) {
            RealGeology.LOGGER.info("Real Geology is generating provinces and folded strata in newly created chunks");
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < minX + 16; x++) for (int z = minZ; z < minZ + 16; z++) {
            int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
            boolean debugCutaway = debugMode.cutsAt(x, z);
            Environment environment = environment(level, x, z, top, pos);
            Province p = RealGeologyConfig.forceCollisionBelt()
                    ? Province.MOUNTAIN : adaptProvince(province(seed, x, z), environment);
            GeologicalColumn column = geologicalColumn(seed, p, environment, x, z);
            int thermalBaseThickness = thermalBaseThickness(seed, p, environment, x, z);
            int thermalBaseTop = thermalBaseTopY(minY, thermalBaseThickness, column.structure());
            for (int y = minY; y <= top; y++) {
                pos.set(x, y, z);
                BlockState existing = level.getBlockState(pos);
                // Recent seafloor sediment is loose and depth-sorted. Apply it
                // before the rock replacement pass so it can replace vanilla's
                // repetitive sand blanket while the lithified shale/siltstone
                // succession remains directly below it.
                BlockState marineSediment = marineFloorSediment(seed, environment, x, y, z, top, existing);
                if (marineSediment != null) {
                    level.setBlock(pos, marineSediment, 2);
                    continue;
                }
                if (!replaceable(existing)) continue;
                // Minecraft's world floor is bedrock, so there is no legal
                // space "below" it. Model the hot lower crust immediately
                // above it: a thin, mostly solid magma boundary with rare,
                // sealed lava pockets in its lowest non-bedrock layer. This
                // keeps caves playable while rewarding very deep excavation.
                if (!debugCutaway && y > minY && y <= thermalBaseTop) {
                    boolean lavaPocket = level.getBlockState(pos.below()).is(Blocks.BEDROCK)
                            && noise(seed + 197L, x / 18d, z / 18d) > 1d - RealGeologyConfig.thermalBaseLavaPocketChance();
                    level.setBlock(pos, lavaPocket ? Blocks.LAVA.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                    continue;
                }
                if (debugMode.stripsFluidsAt(x, z) && isFluid(existing)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    continue;
                }
                // A section is an inspection cut, not an instruction to
                // erase the lower crust. Keep the thermal base in the trench
                // itself so its changing thickness is visible without mining
                // into the side wall. Half-cut removes everything on X < 0.
                if (debugCutaway && (debugMode == RealGeologyConfig.WorldgenDebugMode.SECTION
                        || debugMode == RealGeologyConfig.WorldgenDebugMode.HALF_CUT)) {
                    if (debugMode == RealGeologyConfig.WorldgenDebugMode.SECTION && y > minY && y <= thermalBaseTop) {
                        level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                    continue;
                }

                // Terralith's Overworld mantle and thermal caves use the Nether-only
                // blackstone family as their lava-wall material.  In this pack those
                // are treated as cooled mafic lava: real basalt at cave level and
                // diabase in the deepest, slowly cooled parts of the intrusion.
                // Magma blocks remain in place; they represent the genuinely hot
                // surface around a lava pocket, not the cave wall itself.
                String volcanicReplacement = volcanicReplacement(existing, y);
                if (volcanicReplacement != null) {
                    level.setBlock(pos, rock(volcanicReplacement), 2);
                    continue;
                }
                // A magmatic system is a finite plumbing network, not a
                // world-wide lava noise field.  Only the small liquid core of
                // a young volcanic reservoir remains lava; its chamber rim,
                // feeder and sills are solid intrusive rock.
                BlockState liveMelt = column.magmatism().liveMeltAt(y);
                if (liveMelt != null && !debugMode.stripsFluidsAt(x, z)) {
                    level.setBlock(pos, liveMelt, 2);
                    continue;
                }
                String intrusiveRock = column.magmatism().solidIntrusionAt(y);
                if (intrusiveRock != null) {
                    level.setBlock(pos, rock(intrusiveRock), 2);
                    continue;
                }
                String host = host(seed, column, x, y, z);
                // Custom deposit placement (kimberlite pipes, hosted ores, etc.)
                // is deferred until after the structural-geology beta. Vanilla
                // ore features remain active in the meantime.
                String ore = deposit(seed, column, host, x, y, z);
                // Calcite is a mineral within carbonate rocks, not another
                // interchangeable regional bedrock. It therefore appears as
                // sparse shallow-platform cement, cave-wall spar, and a few
                // hydrothermal vein/fracture fillings inside limestone,
                // dolomite, or marble after ore placement has had priority.
                BlockState calcite = ore == null ? calciteFormation(level, seed, column, host, x, y, z, pos) : null;
                if (debugCutaway && debugMode == RealGeologyConfig.WorldgenDebugMode.ORES && ore == null) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                } else {
                    level.setBlock(pos, ore != null ? ore(host, ore) : calcite != null ? calcite : rock(host), 2);
                }
            }
            if (debugCutaway) {
                pos.set(x, top + 1, z);
                if (naturalLooseSurface(level.getBlockState(pos))) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            } else {
                placeSurfaceOutcrop(level, seed, column, x, z, top, environment, pos);
            }
        }
        return true;
    }

    private static boolean replaceable(BlockState s) {
        Block b = s.getBlock();
        if (b == Blocks.STONE || b == Blocks.DEEPSLATE || b == Blocks.GRANITE || b == Blocks.DIORITE || b == Blocks.ANDESITE || b == Blocks.TUFF
                || b == Blocks.BLACKSTONE || b == Blocks.BASALT || b == Blocks.SMOOTH_BASALT) return true;
        return GameCompat.blockNamespace(b).equals("geostrata") && !GameCompat.blockPath(b).contains("_ore");
    }

    private static boolean isFluid(BlockState state) {
        return state.getFluidState().isSource() || !state.getFluidState().isEmpty();
    }
    private static String volcanicReplacement(BlockState state, int y) {
        Block block = state.getBlock();
        if (block != Blocks.BLACKSTONE && block != Blocks.BASALT && block != Blocks.SMOOTH_BASALT) return null;
        return y < -12 ? "diabase" : "basalt";
    }

    /**
     * Modern seabed veneer above the already lithified marine succession.
     * Wave-agitated shores retain gravel/sand; quieter shelf water accumulates
     * mud and clay; deep basins receive mostly fine mud. This is intentionally
     * only a few blocks thick, leaving marine shale/siltstone visible below
     * rather than replacing the crust with loose material.
     */
    private static BlockState marineFloorSediment(long seed, Environment environment, int x, int y, int z,
                                                  int oceanFloorY, BlockState existing) {
        if (!environment.underwater() || environment.river()) return null;
        Block block = existing.getBlock();
        boolean naturalSubstrate = replaceable(existing) || block == Blocks.SAND || block == Blocks.RED_SAND
                || block == Blocks.GRAVEL || block == Blocks.CLAY || block == Blocks.MUD || block == Blocks.DIRT;
        if (!naturalSubstrate) return null;
        int depth = environment.waterDepth();
        int thickness = depth <= 7 ? 3 : depth <= 22 ? 5 : depth <= 46 ? 7 : 9;
        thickness += noise(seed + 431L, x / 190d, z / 190d) > .44d ? 1 : 0;
        int belowFloor = oceanFloorY - y;
        if (belowFloor < 0 || belowFloor >= thickness) return null;
        double grain = noise(seed + 433L, x / 58d, z / 58d);
        double bar = noise(seed + 437L, x / 260d, z / 260d);
        if (depth <= 7 || environment.beach()) {
            // High-energy swash zone: mixed sand and gravel, with broader
            // gravel patches where wave action is strongest.
            return (grain > .26d || bar > .48d) ? Blocks.GRAVEL.defaultBlockState() : Blocks.SAND.defaultBlockState();
        }
        if (depth <= 22) {
            // Inner shelf: a mobile sandy cap over settling silt/mud.
            if (belowFloor <= 1 && (grain > -.16d || bar > .28d)) return Blocks.SAND.defaultBlockState();
            return grain > .38d ? Blocks.CLAY.defaultBlockState() : Blocks.MUD.defaultBlockState();
        }
        if (depth <= 46) {
            // Outer shelf / upper slope: fine sediment dominates; occasional
            // turbid sand arrives as a thin event bed.
            if (belowFloor == 0 && grain > .67d) return Blocks.SAND.defaultBlockState();
            return grain > .20d ? Blocks.CLAY.defaultBlockState() : Blocks.MUD.defaultBlockState();
        }
        // Deep basin: pelagic and hemipelagic mud, with clay-rich patches.
        return grain > -.05d ? Blocks.CLAY.defaultBlockState() : Blocks.MUD.defaultBlockState();
    }

    /**
     * Expose the actual host rock only where terrain is naturally steep or
     * rugged. Flat grassland, beaches, water, and player/structure blocks are
     * left alone; cliffs and mountain shoulders gain readable outcrops.
     */
    private static void placeSurfaceOutcrop(WorldGenLevel level, long seed, GeologicalColumn column, int x, int z, int terrainY, Environment environment, BlockPos.MutableBlockPos pos) {
        if (terrainY <= GameCompat.minY(level) || environment.underwater()) return;
        pos.set(x, terrainY, z);
        BlockState surface = level.getBlockState(pos);
        if (!naturalLooseSurface(surface)) return;
        double exposure = noise(seed + 211L, x / 145d, z / 145d);
        boolean outcrop = switch (column.province()) {
            case MOUNTAIN -> environment.relief() >= 2 && exposure > -.38d;
            case VOLCANIC -> environment.relief() >= 1 && exposure > -.08d;
            case PLUTONIC -> environment.relief() >= 2 && exposure > -.18d;
            case SEDIMENTARY -> environment.relief() >= 4 && exposure > .18d;
        };
        // Weathering mantles cover rock more thoroughly in flat, wet terrain;
        // dry plateaux and cold mountain shoulders leave more bedrock exposed.
        if (environment.wet() && !environment.rugged()) outcrop = false;
        if ((environment.dry() || environment.cold()) && environment.relief() >= 2) outcrop |= exposure > -.26d;
        if (outcrop) level.setBlock(pos, rock(host(seed, column, x, terrainY, z)), 2);
    }

    private static boolean naturalLooseSurface(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT
                || block == Blocks.ROOTED_DIRT || block == Blocks.GRAVEL || block == Blocks.SAND
                || block == Blocks.RED_SAND || block == Blocks.TERRACOTTA;
    }

    /**
     * Terralith publishes these common biome categories as `c:` tags. Reading
     * the tags here is safe with or without Terralith: absent tags simply
     * evaluate false, leaving the seed-based geology usable in a plain pack.
     */
    private static TagKey<Biome> biomeTag(String path) {
        return GameCompat.biomeTag("c", path);
    }

    private static Environment environment(WorldGenLevel level, int x, int z, int terrainY, BlockPos.MutableBlockPos pos) {
        int worldSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        int west = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x - 1, z) - 1;
        int east = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x + 1, z) - 1;
        int north = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z - 1) - 1;
        int south = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z + 1) - 1;
        // A 32-block cross is a cheap low-pass terrain sample. It gives the
        // geology mountain-scale coupling without imprinting each individual
        // cliff block or surface noise into the deep strata.
        int broadWest = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x - 32, z) - 1;
        int broadEast = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x + 32, z) - 1;
        int broadNorth = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z - 32) - 1;
        int broadSouth = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z + 32) - 1;
        int broadElevation = (terrainY * 2 + broadWest + broadEast + broadNorth + broadSouth) / 6 - 63;
        int relief = Math.max(Math.max(Math.abs(terrainY - west), Math.abs(terrainY - east)),
                Math.max(Math.abs(terrainY - north), Math.abs(terrainY - south)));
        pos.set(x, Math.max(GameCompat.minY(level), terrainY), z);
        var biome = level.getBiome(pos);
        String id = biome.unwrapKey().map(GameCompat::biomePath).orElse("");
        boolean aquatic = biome.is(AQUATIC) || worldSurfaceY != terrainY;
        boolean river = biome.is(RIVER);
        boolean beach = biome.is(BEACH);
        boolean mountain = biome.is(MOUNTAIN) || biome.is(HILL) || biome.is(PLATEAU)
                || biome.is(TERRALITH_HIGHLANDS) || biome.is(TERRALITH_CLIFFS);
        boolean volcanic = biome.is(TERRALITH_VOLCANIC) || id.contains("volcanic") || id.contains("caldera") || id.contains("yellowstone");
        boolean deepOcean = biome.is(DEEP_OCEAN);
        int elevation = terrainY - 63;
        TerrainSetting setting;
        // Continentalness makes the broad land/ocean framework, but terrain
        // tags and relief decide whether that continental crust is a quiet
        // basin, an eroded collision belt, or a volcanic region.
        if (aquatic && deepOcean) setting = TerrainSetting.OCEAN_BASIN;
        else if (aquatic || beach) setting = TerrainSetting.PASSIVE_MARGIN;
        else if (volcanic) setting = TerrainSetting.VOLCANIC_ARC;
        else if (mountain && (relief >= 2 || elevation > 38)) setting = TerrainSetting.COLLISION_BELT;
        else if (river || biome.is(WET) && elevation < 26) setting = TerrainSetting.CONTINENTAL_BASIN;
        else setting = TerrainSetting.STABLE_INTERIOR;
        return new Environment(
                biome.is(DRY) || biome.is(BADLANDS), biome.is(WET), biome.is(HOT), biome.is(COLD),
                mountain, aquatic, river, beach, volcanic, relief, worldSurfaceY != terrainY,
                Math.max(0, worldSurfaceY - terrainY), elevation, broadElevation, setting
        );
    }

    /** Let terrain/climate constrain the tectonic field, never replace it. */
    private static Province adaptProvince(Province tectonic, Environment environment) {
        // The terrain generator owns visible landform. The seeded plate field
        // supplies hidden orientation and history, but may no longer invent a
        // mountain belt under an ordinary plain or a deep marine basin.
        return switch (environment.setting()) {
            case OCEAN_BASIN, PASSIVE_MARGIN, CONTINENTAL_BASIN -> Province.SEDIMENTARY;
            case COLLISION_BELT -> Province.MOUNTAIN;
            case VOLCANIC_ARC -> Province.VOLCANIC;
            case STABLE_INTERIOR -> tectonic == Province.MOUNTAIN ? Province.PLUTONIC : tectonic;
        };
    }

    private static Province province(long s, int x, int z) {
        PlateSample plate = plateAt(s, x, z);
        // Plate margins are where the dramatic geology belongs. The margin
        // style is stable for a given pair of plates, so a mountain belt or
        // volcanic/rift belt continues for a long distance instead of fading
        // in and out as a simple noise threshold would.
        if (plate.boundaryDistance() < .105d) {
            int firstX = plate.primaryCellX(), firstZ = plate.primaryCellZ();
            int secondX = plate.secondaryCellX(), secondZ = plate.secondaryCellZ();
            // A margin must have one identity from both sides; sort the two
            // cells before hashing so it cannot become a mountain on one side
            // and a volcanic belt on the other.
            if (firstX > secondX || (firstX == secondX && firstZ > secondZ)) {
                int swapX = firstX, swapZ = firstZ;
                firstX = secondX; firstZ = secondZ;
                secondX = swapX; secondZ = swapZ;
            }
            int boundaryStyle = (int) (hash(s + 17L, firstX * 37 + secondX * 13,
                    firstZ * 37 + secondZ * 13) * 3d);
            return switch (boundaryStyle) {
                case 0 -> Province.MOUNTAIN; // convergent / fold-and-thrust belt
                case 1 -> Province.VOLCANIC; // rift or volcanic-arc belt
                default -> Province.SEDIMENTARY; // subsiding margin basin
            };
        }
        return switch (plate.primaryType()) {
            case 0, 3 -> Province.SEDIMENTARY;
            case 1 -> Province.PLUTONIC;
            default -> Province.VOLCANIC;
        };
    }

    /** A low-cost, jittered Voronoi plate map with ~2.4 km wide plates. */
    private static PlateSample plateAt(long seed, int x, int z) {
        final double plateSize = 2400d;
        int cellX = (int) Math.floor(x / plateSize);
        int cellZ = (int) Math.floor(z / plateSize);
        double nearest = Double.MAX_VALUE, second = Double.MAX_VALUE;
        int nearestX = 0, nearestZ = 0, secondX = 0, secondZ = 0, nearestType = 0;
        for (int cx = cellX - 1; cx <= cellX + 1; cx++) for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
            double px = (cx + .5d + (hash(seed + 101L, cx, cz) - .5d) * .68d) * plateSize;
            double pz = (cz + .5d + (hash(seed + 103L, cx, cz) - .5d) * .68d) * plateSize;
            double dx = x - px, dz = z - pz, distanceSquared = dx * dx + dz * dz;
            if (distanceSquared < nearest) {
                second = nearest; secondX = nearestX; secondZ = nearestZ;
                nearest = distanceSquared; nearestX = cx; nearestZ = cz;
                nearestType = (int) (hash(seed + 107L, cx, cz) * 4d);
            } else if (distanceSquared < second) {
                second = distanceSquared; secondX = cx; secondZ = cz;
            }
        }
        double boundaryDistance = (Math.sqrt(second) - Math.sqrt(nearest)) / plateSize;
        return new PlateSample(nearestX, nearestZ, secondX, secondZ, nearestType, boundaryDistance);
    }
    /** Precompute all X/Z-only geology once per column, not once per block. */
    private static GeologicalColumn geologicalColumn(long s, Province p, Environment environment, int x, int z) {
        // Two independent faults make narrow cross-cutting diabase dikes rather than a checkerboard.
        double dikeA = Math.abs(Math.sin((x * .83 + z * .29) / 185d + noise(s + 47, x / 800d, z / 800d)));
        double dikeB = Math.abs(Math.sin((x * -.35 + z * .94) / 330d + noise(s + 53, x / 700d, z / 700d)));
        FaultSample sedimentFault = p == Province.SEDIMENTARY ? sedimentFault(s + 43L, environment, x, z) : FaultSample.NONE;
        StructuralTransform structure = structuralTransform(s, p, environment, x, z);
        // The forced inspection belt is specifically a folded-sediment test:
        // do not randomly replace half of it with metamorphic core. In normal
        // collision belts, sedimentary nappes are common but crystalline cores
        // still occur as broad, coherent domains.
        boolean foldedSedimentary = p == Province.MOUNTAIN && (RealGeologyConfig.forceCollisionBelt()
                || hash(s + 79L, (int) Math.floor(x / 1800d), (int) Math.floor(z / 1800d)) > .28d);
        StratigraphicColumn column = switch (p) {
            case SEDIMENTARY -> environment.setting() == TerrainSetting.OCEAN_BASIN ? MARINE_COLUMN
                    : environment.setting() == TerrainSetting.PASSIVE_MARGIN ? PASSIVE_MARGIN_COLUMN
                    : environment.river() || environment.wet() ? ALLUVIAL_COLUMN
                    : environment.dry() || environment.hot() ? ARID_BASIN_COLUMN : SEDIMENTARY_COLUMN;
            case MOUNTAIN -> foldedSedimentary ? FOLDED_SEDIMENTARY_COLUMN : MOUNTAIN_COLUMN;
            case PLUTONIC -> PLUTONIC_COLUMN;
            case VOLCANIC -> VOLCANIC_COLUMN;
        };
        // An unconformity is a time break, not a visual stripe: old beds are
        // folded/faulted below it, eroded, then younger cover starts again
        // with its own flatter coordinate system. Basins preserve thicker
        // cover; passive margins drape a little more strongly toward the sea.
        int unconformityY = switch (environment.setting()) {
            case OCEAN_BASIN -> 14 + (int) Math.round(noise(s + 67L, x / 780d, z / 780d) * 9d);
            case PASSIVE_MARGIN -> 20 + (int) Math.round(noise(s + 67L, x / 700d, z / 700d) * 12d);
            case CONTINENTAL_BASIN -> 28 + (int) Math.round(noise(s + 67L, x / 620d, z / 620d) * 14d);
            // A quiet pocket within a mountain belt can retain young valley
            // fill above eroded folded basement. The surrounding steep ground
            // remains exposed because no younger cover is assigned there.
            case COLLISION_BELT -> environment.relief() <= 1 && environment.elevation() < 34
                    ? 10 + (int) Math.round(noise(s + 67L, x / 520d, z / 520d) * 10d) : Integer.MAX_VALUE;
            default -> Integer.MAX_VALUE;
        };
        StratigraphicSample youngerCover = unconformityY != Integer.MAX_VALUE
                ? SEDIMENTARY_COVER_COLUMN.sampleFromBase(s, 19, x, z, unconformityY, coverThicknessScale(environment), null, FaultSample.NONE)
                : null;
        StratigraphicSample footwallStratigraphy = column.sample(s, p.ordinal(), x, z,
                formationThicknessScale(environment), environment.setting(), sedimentFault);
        // Thrusts prefer mechanically weak horizons. Select a real local
        // shale/siltstone (or weak metamorphic equivalent) from the restored
        // stratigraphy, then let the structural transform carry that horizon
        // through folds and mountain uplift.
        structure = structure.withWeakDetachment(preferredDetachmentHorizon(footwallStratigraphy));
        // A thrust sheet samples a formation package from its pre-transport
        // source position. This makes the hanging wall genuinely different
        // from the footwall instead of merely offsetting the same local bands.
        int sourceX = structure.thrust().sourceX(x);
        int sourceZ = structure.thrust().sourceZ(z);
        StratigraphicSample transportedStratigraphy = structure.thrust().enabled()
                ? column.sample(s, p.ordinal(), sourceX, sourceZ, formationThicknessScale(environment), environment.setting(), sedimentFault)
                : null;
        PlateSample plate = plateAt(s, x, z);
        double regionalMetamorphism = p == Province.MOUNTAIN
                ? .30d + Math.max(0d, .16d - plate.boundaryDistance()) * 3d
                        + (noise(s + 73L, x / 520d, z / 520d) + 1d) * .08d
                : 0d;
        return new GeologicalColumn(
                p,
                structure,
                footwallStratigraphy,
                transportedStratigraphy,
                Math.min(dikeA, dikeB),
                intrusionStrength(s + 71L, x, z),
                MagmaticSystem.at(s + 311L, p, environment.setting(), x, z),
                unconformityY,
                youngerCover,
                regionalMetamorphism,
                sedimentFault,
                foldedSedimentary,
                environment
        );
    }

    /**
     * Development-only sampling entry point for the cross-section renderer.
     * It deliberately uses the same finite fold ribbons, fault-block logic,
     * formation-thickness variation and interbeds as a forced collision-belt
     * test world, but does not need a running Minecraft level or generate any
     * chunks. It therefore gives rapid, reproducible previews while tuning.
     */
    public static String debugForcedCollisionRock(long seed, int x, int y, int z, int terrainY) {
        // The preview receives the actual terrain surface from Minecraft's
        // generator, so folded rock below a mountain can share the same broad
        // uplift rather than behaving like a flat column with extra material
        // added on top.
        int elevation = terrainY - 63;
        Environment testEnvironment = new Environment(false, false, false, false,
                elevation > 26, false, false, false, false, 4, false, 0, elevation, elevation, TerrainSetting.COLLISION_BELT);
        StructuralTransform structure = structuralTransform(seed, Province.MOUNTAIN, testEnvironment, x, z);
        StratigraphicSample succession = FOLDED_SEDIMENTARY_COLUMN.sample(seed, Province.MOUNTAIN.ordinal(), x, z,
                formationThicknessScale(testEnvironment), TerrainSetting.COLLISION_BELT, FaultSample.NONE);
        structure = structure.withWeakDetachment(preferredDetachmentHorizon(succession));
        StratigraphicSample transported = structure.thrust().enabled()
                ? FOLDED_SEDIMENTARY_COLUMN.sample(seed, Province.MOUNTAIN.ordinal(),
                structure.thrust().sourceX(x), structure.thrust().sourceZ(z),
                formationThicknessScale(testEnvironment), TerrainSetting.COLLISION_BELT, FaultSample.NONE)
                : null;
        GeologicalColumn column = new GeologicalColumn(Province.MOUNTAIN, structure, succession, transported, 1d, -1d,
                MagmaticSystem.at(seed + 311L, Province.MOUNTAIN, TerrainSetting.COLLISION_BELT, x, z),
                Integer.MAX_VALUE, null, 0d, FaultSample.NONE, true, testEnvironment);
        // Mirror the enabled variable lower crust used for the inspection
        // build. This keeps the stand-alone preview honest: the thermal base
        // is part of a section, not something hidden beneath its lower edge.
        if (y > -64 && y <= thermalBaseTopY(-64, debugThermalBaseThickness(seed, x, z), structure)) return "magma";
        if (column.magmatism().liveMeltAt(y) != null) return "lava";
        String intrusive = column.magmatism().solidIntrusionAt(y);
        if (intrusive != null) return intrusive;
        return thinInterbed(seed, column, column.countryRockAt(y), x, y, z);
    }

    private static int debugThermalBaseThickness(long seed, int x, int z) {
        PlateSample plate = plateAt(seed, x, z);
        Province underlyingProvince = province(seed, x, z);
        TerrainSetting proxySetting = switch (underlyingProvince) {
            case MOUNTAIN -> TerrainSetting.COLLISION_BELT;
            case VOLCANIC -> TerrainSetting.VOLCANIC_ARC;
            case PLUTONIC -> TerrainSetting.STABLE_INTERIOR;
            case SEDIMENTARY -> plate.primaryType() == 0 ? TerrainSetting.OCEAN_BASIN : TerrainSetting.CONTINENTAL_BASIN;
        };
        return thermalAsthenosphereThickness(seed, proxySetting, plate, x, z, 4, 50);
    }

    private static double formationThicknessScale(Environment environment) {
        return switch (environment.setting()) {
            case OCEAN_BASIN -> 1.38d;
            case PASSIVE_MARGIN -> 1.20d;
            case CONTINENTAL_BASIN -> 1.16d;
            case STABLE_INTERIOR -> .92d;
            case COLLISION_BELT -> .82d;
            case VOLCANIC_ARC -> .90d;
        };
    }

    private static double coverThicknessScale(Environment environment) {
        return switch (environment.setting()) {
            case OCEAN_BASIN -> 1.30d;
            case PASSIVE_MARGIN -> 1.18d;
            case CONTINENTAL_BASIN -> 1.08d;
            case COLLISION_BELT -> .72d;
            default -> 1d;
        };
    }

    /**
     * Optional plate-dependent exposed asthenosphere thickness. It is off by
     * default because it is an inspection proxy, not a literal mantle model.
     */
    private static int thermalBaseThickness(long seed, Province province, Environment environment, int x, int z) {
        if (!RealGeologyConfig.variableTectonicBase()) return RealGeologyConfig.thermalBaseMagmaThickness();
        int minimum = Math.min(RealGeologyConfig.tectonicBaseMinThickness(), RealGeologyConfig.tectonicBaseMaxThickness());
        int maximum = Math.max(RealGeologyConfig.tectonicBaseMinThickness(), RealGeologyConfig.tectonicBaseMaxThickness());
        PlateSample plate = plateAt(seed, x, z);
        return thermalAsthenosphereThickness(seed, environment.setting(), plate, x, z, minimum, maximum);
    }

    /**
     * The exposed hot-base proxy follows a restrained part of the same broad
     * structural field as the lower crust.  It is not a second copy of every
     * thin bed: large uplifts raise the hot boundary, large sags lower it, and
     * a nearby fault can carry the boundary with the displaced block.
     */
    private static int thermalBaseTopY(int minimumY, int baseThickness, StructuralTransform structure) {
        int probeY = minimumY + Math.max(12, baseThickness + 10);
        double physicalDisplacement = probeY - structure.referenceY(probeY);
        int curvatureFollow = (int) Math.round(Math.max(-16d, Math.min(16d, physicalDisplacement * .42d)));
        return minimumY + Math.max(2, Math.min(66, baseThickness + curvatureFollow));
    }

    /**
     * Inverse of lithosphere/root thickness. Thick old continental crust and
     * collision roots push the hot asthenosphere down; young oceanic/rift
     * lithosphere is thin, so hot material is closer to the crust.
     */
    private static int thermalAsthenosphereThickness(long seed, TerrainSetting setting,
                                                     PlateSample plate, int x, int z, int minimum, int maximum) {
        // Blend the two plate interiors through a shared boundary value. This
        // prevents a Voronoi plate edge from becoming a square, vertical step
        // in the lower lithosphere.
        int secondaryType = (int) (hash(seed + 107L, plate.secondaryCellX(), plate.secondaryCellZ()) * 4d);
        double boundaryDistance = Math.max(0d, plate.boundaryDistance());
        double interiorBlend = smoothstep(Math.min(1d, boundaryDistance / .18d));
        double primaryHeat = plateInteriorHeat(plate.primaryType());
        double secondaryHeat = plateInteriorHeat(secondaryType);
        double fraction = lerp((primaryHeat + secondaryHeat) * .5d, primaryHeat, interiorBlend);
        Province margin = marginProvince(seed, plate);
        double marginHeat = switch (margin) {
            case MOUNTAIN -> .045d;  // thick collisional root
            case VOLCANIC -> .88d;   // hot, thin lithosphere / rift or arc
            case SEDIMENTARY -> .44d;
            case PLUTONIC -> .19d;
        };
        double marginBlend = 1d - smoothstep(Math.min(1d, boundaryDistance / .15d));
        fraction = lerp(fraction, marginHeat, marginBlend);
        double longWavelength = (noise(seed + 233L, x / 2100d, z / 2100d) + 1d) * .5d;
        // Terrain/climate setting offers a small, continuous-scale bias. It
        // never defines the base by itself, so a biome border cannot form a
        // false tectonic wall.
        double settingBias = switch (setting) {
            case OCEAN_BASIN -> .08d;
            case PASSIVE_MARGIN -> .03d;
            case CONTINENTAL_BASIN -> -.02d;
            case STABLE_INTERIOR -> -.04d;
            case COLLISION_BELT -> -.05d;
            case VOLCANIC_ARC -> .08d;
        };
        // Broad 2–4 km relief in the boundary is geological; hard random
        // plate-cell steps are not.
        fraction += settingBias + (longWavelength - .5d) * .14d;
        return minimum + (int) Math.round((maximum - minimum) * Math.max(0d, Math.min(1d, fraction)));
    }

    private static double plateInteriorHeat(int plateType) {
        return switch (plateType) {
            case 2 -> .68d; // thin, hot volcanic/oceanic-style lithosphere
            case 0, 3 -> .29d;
            default -> .18d; // old plutonic/continental-style interior
        };
    }

    private static Province marginProvince(long seed, PlateSample plate) {
        if (plate.boundaryDistance() >= .105d) return switch (plate.primaryType()) {
            case 0, 3 -> Province.SEDIMENTARY;
            case 1 -> Province.PLUTONIC;
            default -> Province.VOLCANIC;
        };
        int firstX = plate.primaryCellX(), firstZ = plate.primaryCellZ();
        int secondX = plate.secondaryCellX(), secondZ = plate.secondaryCellZ();
        if (firstX > secondX || (firstX == secondX && firstZ > secondZ)) {
            int swapX = firstX, swapZ = firstZ;
            firstX = secondX; firstZ = secondZ;
            secondX = swapX; secondZ = swapZ;
        }
        return switch ((int) (hash(seed + 17L, firstX * 37 + secondX * 13, firstZ * 37 + secondZ * 13) * 3d)) {
            case 0 -> Province.MOUNTAIN;
            case 1 -> Province.VOLCANIC;
            default -> Province.SEDIMENTARY;
        };
    }

    private static double smoothstep(double value) {
        return value * value * (3d - 2d * value);
    }

    private static double lerp(double from, double to, double amount) {
        return from + (to - from) * amount;
    }

    private static String host(long seed, GeologicalColumn column, int x, int y, int z) {
        String countryRock = regionalMetamorphicRock(column, column.countryRockAt(y), y);
        // The forced collision-belt mode is an anatomy test, not a realistic
        // full mountain province. Suppress unrelated intrusions so they cannot
        // masquerade as artificial pillars inside the folded sediment package.
        boolean structuralInspection = RealGeologyConfig.forceCollisionBelt();
        // Hot fluids circulate through the fractured halo and upper feeder of
        // a cooling intrusion. This is a narrow alteration rim: it changes
        // susceptible carbonate/muddy hosts without recolouring a whole
        // volcanic province into a random metamorphic mix.
        double hydrothermal = column.magmatism().hydrothermalStrength(y);
        if (!structuralInspection && hydrothermal > .64d && y < 82) {
            return contactRock(countryRock);
        }
        // A dike is magma emplaced into a fracture. It cuts the older folded
        // sequence, but a later unconformity/cover can truncate it instead of
        // allowing it to unrealistically cut every youngest surface bed.
        if (!structuralInspection && !column.hasYoungerCover(y) && (column.province() == Province.VOLCANIC || column.province() == Province.PLUTONIC
                || column.province() == Province.MOUNTAIN) && column.dikeDistance() < .010d && y < 92) return "diabase";
        // The narrow thermal halo around a dike bakes carbonate and muddy beds
        // without turning an entire province into a random metamorphic mix.
        if (!structuralInspection && !column.hasYoungerCover(y) && column.dikeDistance() < .027d && y < 92) return contactRock(countryRock);
        double batholith = column.batholithStrength() - Math.max(0, y - 10) / 210d;
        if (!structuralInspection && (column.province() == Province.PLUTONIC || column.province() == Province.MOUNTAIN) && batholith > .58) {
            return batholith > .77 ? "granite" : "diorite";
        }
        if (!structuralInspection && (column.province() == Province.PLUTONIC || column.province() == Province.MOUNTAIN) && batholith > .51 && y < 38) {
            return "pegmatite";
        }
        // The wider aureole at an intrusion edge is where contact metamorphism
        // belongs: marble from carbonates and hornfels from fine sediment.
        if (!structuralInspection && (column.province() == Province.PLUTONIC || column.province() == Province.MOUNTAIN) && batholith > .47) {
            return contactRock(countryRock);
        }
        return thinInterbed(seed, column, countryRock, x, y, z);
    }

    /**
     * Thin interbeds are real sedimentary event beds, not miniature complete
     * formations. Each is 2–4 blocks thick, follows the same deformed bedding
     * as its host, and pinches laterally instead of becoming a global stripe.
     */
    private static String thinInterbed(long seed, GeologicalColumn column, String countryRock, int x, int y, int z) {
        if ((column.province() != Province.SEDIMENTARY && !column.foldedSedimentary()) || column.hasYoungerCover(y)) return countryRock;
        BedPosition bed = column.bedAt(y);
        if (bed.index() < 0 || bed.thickness() < 10) return countryRock;
        double lateralPresence = noise(seed + 271L + bed.index() * 19L, x / 360d, z / 360d);
        // The centre varies only slowly across the world, so an interbed is a
        // sheet/lens thousands of blocks long rather than a speckled texture.
        int centre = bed.bottom() + 3 + (int) Math.round((bed.thickness() - 6)
                * (.18d + (noise(seed + 277L + bed.index() * 23L, x / 520d, z / 520d) + 1d) * .31d));
        int thickness = 2 + (int) Math.floor((noise(seed + 281L + bed.index() * 29L,
                x / 240d, z / 240d) + 1d) * 1.45d);
        int bottom = centre - thickness / 2;
        if (bed.stratigraphicY() < bottom || bed.stratigraphicY() >= bottom + thickness || lateralPresence < -.16d) return countryRock;
        return switch (countryRock) {
            case "shale" -> column.environment().setting() == TerrainSetting.OCEAN_BASIN ? "limestone" : "siltstone";
            case "siltstone" -> "shale";
            case "limestone", "dolomite" -> "shale";
            case "conglomerate" -> "siltstone";
            default -> countryRock;
        };
    }

    /** Regional pressure/temperature progression inside a fold belt. */
    private static String regionalMetamorphicRock(GeologicalColumn column, String rock, int y) {
        if (column.province() != Province.MOUNTAIN || column.foldedSedimentary()) return rock;
        double grade = Math.max(0d, Math.min(1d, column.regionalMetamorphism() + Math.max(0, 54 - y) / 145d));
        return switch (rock) {
            case "shale", "slate" -> grade < .43d ? "slate" : grade < .64d ? "phyllite" : grade < .84d ? "schist" : "gneiss";
            case "phyllite" -> grade < .61d ? "phyllite" : grade < .83d ? "schist" : "gneiss";
            case "schist" -> grade < .79d ? "schist" : "gneiss";
            case "siltstone", "conglomerate" -> grade < .66d ? rock : "quartzite";
            case "limestone", "dolomite" -> "marble";
            case "basalt", "tuff", "scoria" -> "amphibolite";
            default -> rock;
        };
    }

    private static String contactRock(String countryRock) {
        return switch (countryRock) {
            case "limestone", "dolomite" -> "marble";
            case "shale", "siltstone", "conglomerate" -> "hornfels";
            case "basalt", "tuff", "scoria" -> "amphibolite";
            default -> countryRock;
        };
    }

    /**
     * Calcite is placed only where a carbonate setting gives it a plausible
     * origin: shallow-water cement/reef-like bands, spar lining a carbonate
     * cave, or a narrow hydrothermal fracture halo. The vanilla calcite block
     * is intentionally retained as the visible mineral specimen.
     */
    private static BlockState calciteFormation(WorldGenLevel level, long seed, GeologicalColumn column,
                                               String host, int x, int y, int z, BlockPos.MutableBlockPos pos) {
        boolean carbonate = host.equals("limestone") || host.equals("dolomite") || host.equals("marble");
        if (!carbonate || y < -52 || y > 104) return null;
        double fine = noise(seed + 443L, x / 23d, z / 23d);
        BedPosition bed = column.bedAt(y);
        boolean marineCarbonate = column.province() == Province.SEDIMENTARY
                && (column.environment().setting() == TerrainSetting.PASSIVE_MARGIN
                || column.environment().setting() == TerrainSetting.OCEAN_BASIN);
        // Local carbonate-platform cement / thin crystalline beds. These
        // occupy only selected portions of a limestone/dolomite formation,
        // rather than turning every carbonate bed into pure calcite.
        if (marineCarbonate && (bed.rock().equals("limestone") || bed.rock().equals("dolomite"))
                && noise(seed + 445L, x / 760d, z / 760d) > .31d
                && inFormationSeam(seed + 447L, bed, x, z, .82d) && fine > .26d) {
            return Blocks.CALCITE.defaultBlockState();
        }
        // Mineralising fluid around a cooling intrusion can precipitate calcite
        // in open fractures. Ores take priority above, so this becomes visible
        // gangue/vein material rather than deleting valuable mineralization.
        if (column.magmatism().hydrothermalStrength(y) > .86d && fine > .52d) {
            return Blocks.CALCITE.defaultBlockState();
        }
        // Cave spar only coats carbonate walls/ceilings near an existing void;
        // it does not create a solid cube lattice in ordinary bedrock.
        if (y < 86 && fine > .61d && carbonateCavityFace(level, pos)) {
            return Blocks.CALCITE.defaultBlockState();
        }
        return null;
    }

    private static boolean carbonateCavityFace(WorldGenLevel level, BlockPos.MutableBlockPos pos) {
        return caveFluidOrAir(level.getBlockState(pos.above())) || caveFluidOrAir(level.getBlockState(pos.below()))
                || caveFluidOrAir(level.getBlockState(pos.north())) || caveFluidOrAir(level.getBlockState(pos.south()))
                || caveFluidOrAir(level.getBlockState(pos.east())) || caveFluidOrAir(level.getBlockState(pos.west()));
    }

    private static boolean caveFluidOrAir(BlockState state) {
        return state.isAir() || state.getFluidState().isSource();
    }

    /**
     * Discrete, jittered intrusive centres.  Unlike a single noise threshold,
     * these make finite irregular plutons/batholiths with a meaningful rim for
     * contact metamorphism and pegmatitic mineralisation.
     */
    private static double intrusionStrength(long seed, int x, int z) {
        final double spacing = 1850d;
        int cellX = (int) Math.floor(x / spacing);
        int cellZ = (int) Math.floor(z / spacing);
        double strongest = -1d;
        for (int cx = cellX - 1; cx <= cellX + 1; cx++) for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
            double centreX = (cx + .5d + (hash(seed + 3L, cx, cz) - .5d) * .70d) * spacing;
            double centreZ = (cz + .5d + (hash(seed + 5L, cx, cz) - .5d) * .70d) * spacing;
            double radius = 270d + hash(seed + 7L, cx, cz) * 430d;
            double dx = x - centreX, dz = z - centreZ;
            double ellipticalDistance = Math.sqrt(dx * dx + dz * dz) / radius;
            double irregularity = noise(seed + 11L, x / 190d, z / 190d) * .17d;
            strongest = Math.max(strongest, 1d - ellipticalDistance + irregularity);
        }
        return strongest;
    }

    private static String deposit(long s, GeologicalColumn column, String host, int x, int y, int z) {
        // 0.21 beta: strata and provinces only — vanilla ore features handle spawning.
        return null;
        /*
        Province p = column.province();
        double fine = noise(s + 101, x / 19d, z / 19d);
        BedPosition nativeBed = column.bedAt(y);

        // Sedimentary seams lie inside selected shale beds.  Their height is
        // measured from the actual formation base, so a seam follows folding
        // and faulting instead of appearing as isolated generic coal blobs.
        if (p == Province.SEDIMENTARY && column.environment().wet() && nativeBed.index() == 4 && y > 36 && y < 96
                && nativeBed.rock().equals("shale") && inCoalSeam(s + 103L, column, nativeBed, x, z, 1.20d) && fine > .42) return "lignite";
        if (p == Province.SEDIMENTARY && (column.environment().wet() || column.environment().river()) && nativeBed.index() == 1 && y > -16 && y < 82
                && nativeBed.rock().equals("shale") && inCoalSeam(s + 105L, column, nativeBed, x, z, 1.35d) && fine > .14) return "coal";

        // A pipe is its own host body. Do this before intrusion-related
        // copper/gold logic so a kimberlite pipe does not become a second
        // porphyry stockwork merely because it crosses a plutonic province.
        if (host.equals("kimberlite")) {
            return noise(s + 183L, x / 20d, z / 20d) > .34d ? "diamond" : null;
        }

        // Mineralising fluids are tied to one cooling reservoir and its feeder
        // network. The broad low-grade shell is a porphyry-style Cu halo; the
        // tighter upper feeder carries a sparse Au-Ag vein system; shallow hot
        // volcanic centres can retain sulphur. This replaces independent
        // random vein noise with discoverable, connected districts.
        double hydrothermal = column.magmatism().hydrothermalStrength(y);
        if (hydrothermal > .72d && y > -34 && y < 86 && fine > -.18d) {
            double grade = hydrothermal + noise(s + 311L, x / 31d, z / 31d) * .16d;
            if (column.magmatism().upperFeederAt(y) && grade > .88d) {
                if (y > 8 && column.province() == Province.VOLCANIC && noise(s + 313L, x / 42d, z / 42d) > .28d) return "sulfur";
                return noise(s + 317L, x / 58d, z / 58d) > .38d ? "gold" : "silver";
            }
            if (grade > .79d) return noise(s + 319L, x / 48d, z / 48d) > .46d ? "copper" : "galena";
            if (grade > .73d) return "copper";
        }

        // Bauxite forms as a broad, shallow lateritic blanket in hot wet
        // terrain. It is deliberately not a deep, isolated ore blob: exposed
        // occurrences can support a surface mine while buried ones are still
        // discoverable below the weathering mantle.
        if (p == Province.SEDIMENTARY && column.environment().hot() && column.environment().wet()
                && y > 42 && y < 98 && (nativeBed.rock().equals("siltstone") || nativeBed.rock().equals("shale"))
                && inFormationSeam(s + 107L, nativeBed, x, z, 2.25d)
                && noise(s + 109L, x / 280d, z / 280d) > .16d && fine > -.16d) return "bauxite";
        // Saltpeter is a deliberately rare proxy for evaporitic minerals until
        // a proper rock-salt/gypsum block set is added to Real Geology.
        if (p == Province.SEDIMENTARY && column.environment().dry() && y > 4 && y < 54 && noise(s + 109, x / 64d, z / 64d) > .84) return "saltpeter";

        // Banded iron formation: a thin hematite/magnetite-rich band within the oldest
        // metamorphic formation, not a global repeating ore layer.
        if (p == Province.MOUNTAIN && nativeBed.index() == 0 && y < 52
                && inFormationSeam(s + 113L, nativeBed, x, z, 1.55d) && fine > .28) return "iron";

        // Volcanic-hosted iron oxide: a rare magnetite-rich hydrothermal body
        // along a volcanic fracture, not ordinary iron sprinkled in basalt.
        // Iron-oxide deposits can be hosted by volcanic/volcaniclastic sequences.
        if (p == Province.VOLCANIC && y > -18 && y < 84
                && (host.equals("rhyolite") || host.equals("tuff") || host.equals("basalt"))
                && volcanicMagnetiteBody(s + 115L, x, y, z) && fine > -.24d) return "iron";

        // Porphyry Cu-Au: a large altered intrusive stock cut by dense tiny
        // veinlets. This makes a broad, low-density target that can reach the
        // surface (open-pit style) instead of a single narrow generic vein.
        double stockwork = stockworkVeinDistance(s + 121L, x, y, z);
        if ((p == Province.PLUTONIC || p == Province.MOUNTAIN) && column.batholithStrength() > .54d
                && y > -30 && y < 92 && stockwork < .070d && fine > -.12d) {
            if (column.batholithStrength() > .76d && y < 44 && noise(s + 123L, x / 64d, z / 64d) > .42d) return "gold";
            return "copper";
        }

        // Lapis lazuli is principally lazurite in metamorphosed carbonate
        // rocks. Emerald is beryl in rare pegmatite/contact settings. These
        // retain vanilla item compatibility while finally giving both gems a
        // geological host and a deliberately small search area.
        if (p == Province.MOUNTAIN && host.equals("marble") && y > -12 && y < 58
                && inFormationSeam(s + 125L, nativeBed, x, z, 1.05d) && fine > .50d) return "lapis";
        if ((host.equals("pegmatite") || (host.equals("schist") && column.batholithStrength() > .47d))
                && y < 52 && noise(s + 126L, x / 96d, z / 96d) > .925d && fine > .18d) return "emerald";

        // MVT Pb-Zn-Ag: shallow, stratabound replacement/lenses in platform
        // dolostone and limestone. They are unrelated to intrusions and occur
        // in broad districts, with dense basinal brine reaching the host along
        // faults, breccias and formation boundaries.
        boolean carbonateHost = nativeBed.rock().equals("limestone") || nativeBed.rock().equals("dolomite");
        if (p == Province.SEDIMENTARY && carbonateHost && y < 78
                && mvtDistrict(s + 127L, x, z) && inFormationSeam(s + 129L, nativeBed, x, z, 2.55d)
                && fine > -.20d) {
            double sulphide = noise(s + 131L, x / 55d, z / 55d);
            if (sulphide > .58d) return "zinc";
            if (sulphide > .20d) return "galena";
            return "lead";
        }

        // Clastic-dominated (SEDEX-like) Pb-Zn-Ag layers occupy selected shale
        // and siltstone beds in a subsiding basin. Their shape follows bedding
        // and pinches/swells instead of following cave geometry.
        boolean clasticHost = nativeBed.rock().equals("shale") || nativeBed.rock().equals("siltstone");
        if (p == Province.SEDIMENTARY && clasticHost && nativeBed.index() == 1 && y < 68
                && noise(s + 133L, x / 720d, z / 720d) > .48d
                && inFormationSeam(s + 137L, nativeBed, x, z, 1.85d) && fine > -.06d) {
            double sulphide = noise(s + 139L, x / 52d, z / 52d);
            if (sulphide > .62d) return "silver";
            if (sulphide > .24d) return "zinc";
            return "galena";
        }

        // Narrow polymetallic fissures remain for mountain and plutonic belts.
        // They are steep warped planes, not the universal source of every ore.
        double veinDistance = hydrothermalVeinDistance(s + 131L, x, y, z);
        if ((p == Province.MOUNTAIN || p == Province.PLUTONIC) && veinDistance < 1.30d && y < 58 && fine > .08) {
            if (y < 12 && noise(s + 141L, x / 140d, z / 140d) > .34d) return "gold";
            return noise(s + 143L, x / 80d, z / 80d) > .46d ? "silver" : "copper";
        }

        // Pegmatites are coarse intrusive bodies: tin, lithium and uranium prefer them.
        if ((host.equals("granite") || host.equals("pegmatite")) && y < 38 && noise(s + 151, x / 74d, z / 74d) > .80) return "uranium";
        if (host.equals("pegmatite") && y < 66 && noise(s + 157, x / 50d, z / 50d) > .76) return "tin";
        if (host.equals("pegmatite") && y < 56 && noise(s + 159, x / 58d, z / 58d) > .80) return "lithium";
        // Mafic cumulate lenses: nickel/osmium sit in thin, laterally broad
        // layers near the base of gabbro/peridotite bodies rather than as
        // scattered cubes throughout every dark rock.
        if ((nativeBed.rock().equals("gabbro") || nativeBed.rock().equals("peridotite")) && y < 48
                && inFormationSeam(s + 163L, nativeBed, x, z, 1.65d) && noise(s + 167L, x / 330d, z / 330d) > .10d
                && fine > -.12d) return noise(s + 169L, x / 71d, z / 71d) > .68d ? "osmium" : "nickel";
        if ((host.equals("limestone") || host.equals("dolomite")) && y < 70 && noise(s + 171, x / 66d, z / 66d) > .87) return "fluorite";
        if (p == Province.VOLCANIC && y > 8 && y < 74 && noise(s + 173, x / 55d, z / 55d) > .84) return "sulfur";

        // Redstone deliberately remains a gameplay mineral: it has no real
        // world equivalent. It is restricted to warm volcanic/hydrothermal
        // zones rather than pretending to be a universal natural metal ore.
        if (p == Province.VOLCANIC && y < 24 && noise(s + 187L, x / 78d, z / 78d) > .89d && fine > .20d) return "redstone";
        return null;
        */
    }

    /** Narrow deep kimberlite pipes, independent of the other vein systems. */
    private static boolean diamondPipe(long seed, Province province, int x, int y, int z) {
        if (province != Province.PLUTONIC || y < -50 || y > 24) return false;
        double pipeX = noise(seed + 179L, x / 130d, z / 130d);
        double pipeZ = noise(seed + 181L, z / 130d, x / 130d);
        return pipeX > .895d && pipeZ > .895d;
    }

    /** A pinch-and-swell seam confined to one named sedimentary formation. */
    private static boolean inFormationSeam(long seed, BedPosition bed, int x, int z, double halfThickness) {
        double placement = (noise(seed, x / 300d, z / 300d) + 1d) * .5d;
        int centre = bed.bottom() + 2 + (int) Math.round((bed.thickness() - 4) * (.22d + placement * .56d));
        double pinch = halfThickness * (.72d + (noise(seed + 7L, x / 90d, z / 90d) + 1d) * .19d);
        return Math.abs(bed.stratigraphicY() - centre) <= pinch;
    }

    /**
     * Coal is a sedimentary body in its own right. Growth-fault basins can
     * thicken one side while thinning or locally removing the seam on the
     * other; rare palaeochannels cut it without affecting unrelated beds.
     */
    private static boolean inCoalSeam(long seed, GeologicalColumn column, BedPosition bed, int x, int z, double halfThickness) {
        double placement = (noise(seed, x / 300d, z / 300d) + 1d) * .5d;
        int centre = bed.bottom() + 2 + (int) Math.round((bed.thickness() - 4) * (.22d + placement * .56d));
        double pinch = halfThickness * (.72d + (noise(seed + 7L, x / 90d, z / 90d) + 1d) * .19d);
        FaultSample fault = column.sedimentFault();
        if (fault.synDepositional()) {
            // Growth strata become more pronounced upward through the basin.
            // A raised mire may preserve a thick continuous seam while its
            // subsiding counterpart becomes thin and discontinuous.
            pinch *= fault.coalThicknessFactor();
            if (fault.coreDistance() < 72d && noise(seed + 11L, x / 38d, z / 38d) > .04d) return false;
        }
        // A low-sinuosity palaeochannel is a rare, narrow erosional cut. The
        // surrounding shale remains as the channel-fill proxy in this version.
        boolean channelDistrict = noise(seed + 13L, x / 520d, z / 520d) > .48d;
        double channel = Math.abs(Math.sin((x * .61d + z * .79d) / 104d + noise(seed + 17L, x / 360d, z / 360d)));
        if (channelDistrict && channel < .019d) return false;
        return Math.abs(bed.stratigraphicY() - centre) <= pinch;
    }

    /** A finite magnetite-rich volcanic/hydrothermal lens, elongated along a fracture. */
    private static boolean volcanicMagnetiteBody(long seed, int x, int y, int z) {
        double district = noise(seed, x / 1050d, z / 1050d);
        if (district < .43d) return false;
        double fracture = Math.abs(Math.sin((x * .46d - z * .89d) / 132d + noise(seed + 3L, x / 520d, z / 520d)));
        double verticalLens = noise(seed + 5L, (x + y * .28d) / 64d, (z - y * .17d) / 64d);
        return fracture < .030d && verticalLens > -.10d;
    }

    /** Intersecting fine veinlets in a large altered intrusive stock. */
    private static double stockworkVeinDistance(long seed, int x, int y, int z) {
        double warpA = noise(seed + 3L, x / 130d, z / 130d) * .42d;
        double warpB = noise(seed + 5L, x / 160d, z / 160d) * .37d;
        double first = Math.abs(Math.sin((x * .73d + z * .31d + y * .24d) / 10d + warpA));
        double second = Math.abs(Math.sin((x * -.28d + z * .81d - y * .19d) / 13d + warpB));
        return Math.min(first, second);
    }

    /** Broad MVT district with smaller replacement bodies inside it. */
    private static boolean mvtDistrict(long seed, int x, int z) {
        double regional = noise(seed, x / 1050d, z / 1050d);
        double local = noise(seed + 7L, x / 165d, z / 165d);
        return regional > .17d && local > -.28d;
    }

    /** Distance to a family of steep, locally warped hydrothermal vein planes. */
    private static double hydrothermalVeinDistance(long seed, int x, int y, int z) {
        double angle = .48d + (noise(seed + 3L, x / 2100d, z / 2100d) + 1d) * .36d;
        double across = x * Math.cos(angle) + z * Math.sin(angle);
        double warp = 58d * noise(seed + 5L, x / 210d, z / 210d);
        double planeCoordinate = across + warp + y * .68d;
        double spacing = 760d + 110d * noise(seed + 11L, x / 1800d, z / 1800d);
        return Math.abs(planeCoordinate - Math.rint(planeCoordinate / spacing) * spacing);
    }
    private static BlockState ore(String host, String ore) {
        // One canonical block per material. Other mods consume the shared c:
        // raw-material / ingot tags, so this avoids duplicate worldgen without
        // taking away any machine or recipe compatibility.
        // Industrial mods retain their recipes and machines through common
        // material tags, but never supply the terrain block. This makes every
        // non-vanilla mineral visually inherit the actual GeoStrata host.
        Block hosted = RealGeology.hostedOre(ore);
        if (hosted != null) return hosted.defaultBlockState().setValue(HostedOreBlock.HOST, HostedOreBlock.Host.fromRock(host));
        Block geo = GameCompat.block("geostrata", host + "_" + ore + "_ore");
        if (geo != null && geo != Blocks.AIR) return geo.defaultBlockState();
        return rock(host);
    }
    private static BlockState rock(String n) {
        if (n.equals("kimberlite")) return RealGeology.KIMBERLITE.get().defaultBlockState();
        return RockPalette.rockState(n);
    }
    private static BlockState named(String id, String fallback) {
        return RockPalette.namedState(id, fallback);
    }
    private static BlockState firstExisting(String fallback, String... ids) {
        for (String id : ids) {
            Block block = GameCompat.block(id);
            if (block != null && block != Blocks.AIR) return block.defaultBlockState();
        }
        return rock(fallback);
    }

    /**
     * Converts a generated world Y into the undeformed stratigraphic Y. Most
     * settings use a rigid vertical displacement. Collision belts use a
     * depth-dependent transform, so a horizontal stack can become a tight,
     * asymmetric, or locally overturned fold rather than only a wavy heightmap.
     */
    private static StructuralTransform structuralTransform(long seed, Province province, Environment environment, int x, int z) {
        if (province != Province.MOUNTAIN) {
            return StructuralTransform.rigid(structuralOffset(seed, province, environment, x, z));
        }
        double regional = noise(seed + 23L, x / 1700d, z / 1700d);
        double axisAngle = .35d + (noise(seed + 29L, x / 2400d, z / 2400d) + 1d) * .42d;
        double across = x * Math.cos(axisAngle) + z * Math.sin(axisAngle);
        double along = -x * Math.sin(axisAngle) + z * Math.cos(axisAngle);
        double warpedAcross = across + 125d * noise(seed + 31L, x / 650d, z / 650d);
        int family = (int) (hash(seed + 59L, (int) Math.floor(across / 2200d), (int) Math.floor(along / 2200d)) * 4d);
        // Couple deep structure to broad mountain relief.  This is deliberately
        // not a one-block-for-one-block copy of the surface: it is a broad,
        // rooted antiform.  Its displacement dies out into the lower crust, so
        // the mountain is filled by bent strata instead of moving one entire
        // geological column upwards like an elevator.
        double terrainUplift = mountainRootUplift(environment);
        double amplitude = 34d + (regional + 1d) * 8d + terrainUplift * .18d;
        double plunge = Math.sin(along / 540d + regional) * 6d;
        // This collision-belt pass uses ductile folds and low-angle thrust
        // sheets only. The old steep brittle fault field read as repeated
        // 90-degree walls in a Minecraft cross-section.
        FaultSystem lateFault = FaultSystem.NONE;
        ThrustSheet thrust = ThrustSheet.mountain(seed + 89L, warpedAcross, along, axisAngle);
        return new StructuralTransform(FoldRibbon.create(FoldFamily.values()[family], seed, warpedAcross, along, amplitude),
                warpedAcross, plunge, terrainUplift, lateFault, thrust);
    }

    /** Broad crustal uplift beneath collision-belt mountains, capped for playability. */
    private static double mountainRootUplift(Environment environment) {
        if (environment.setting() != TerrainSetting.COLLISION_BELT) return 0d;
        double elevationAboveLowland = Math.max(0d, environment.broadElevation() - 14d);
        double ruggedness = Math.min(14d, Math.max(0, environment.relief() - 1) * 2.5d);
        return Math.min(82d, elevationAboveLowland * .70d + ruggedness);
    }

    /**
     * Continuous tectonic structure shared by every formation in a column.
     * The coordinate field has broad, changing fold axes; faults are discrete
     * vertical offsets.  Both are based solely on absolute coordinates, so
     * neighbouring chunks always meet exactly.
     */
    private static double structuralOffset(long seed, Province province, Environment environment, int x, int z) {
        double regional = noise(seed + 23L, x / 1700d, z / 1700d);
        double axisAngle = .35d + (noise(seed + 29L, x / 2400d, z / 2400d) + 1d) * .42d;
        double across = x * Math.cos(axisAngle) + z * Math.sin(axisAngle);
        double along = -x * Math.sin(axisAngle) + z * Math.cos(axisAngle);
        // A slow warp stops fold crests and faults reading as mathematical,
        // ruler-straight lines while preserving long coherent structures.
        double warpedAcross = across + 125d * noise(seed + 31L, x / 650d, z / 650d);
        double broadFold = Math.sin(warpedAcross / 240d + regional * 1.7d);
        double secondaryFold = Math.sin(warpedAcross / 79d + along / 1100d) * .28d;
        double plunge = Math.sin(along / 540d + regional) * 5d;

        return switch (province) {
            case MOUNTAIN -> mountainStructure(seed, regional, warpedAcross, across, along, broadFold, secondaryFold, plunge);
            // Basins have gentler warps and normal-fault blocks, rather than
            // mountain-scale folds. The low regional term produces broad sag.
            case SEDIMENTARY -> sedimentaryStructure(seed, environment, regional, broadFold, across, along)
                    + sedimentFault(seed + 43L, environment, x, z).structuralThrow();
            case PLUTONIC -> regional * 8d + broadFold * 3d;
            case VOLCANIC -> regional * 9d + broadFold * 5d;
        };
    }

    /**
     * A belt chooses one coherent fold family, rather than endlessly repeated
     * perfect sine waves. The long axis slowly plunges, so map-view bands can
     * close into real-looking U/V patterns when erosion intersects them.
     */
    private static double mountainStructure(long seed, double regional, double warpedAcross, double across, double along,
                                            double broadFold, double secondaryFold, double plunge) {
        int family = (int) (hash(seed + 59L, (int) Math.floor(across / 2200d), (int) Math.floor(along / 2200d)) * 3d);
        // These amplitudes and wavelengths are limited to collision belts,
        // where compressive tectonics really does produce kilometre-scale
        // anticline/syncline trains.  Quiet plains keep their gentle drape.
        double amplitude = 36d + (regional + 1d) * 7d;
        double wavelength = 86d + (hash(seed + 67L, (int) Math.floor(across / 2200d),
                (int) Math.floor(along / 2200d)) + 1d) * 24d;
        return switch (family) {
            // Broad anticlines and synclines, with a subordinate fold that
            // keeps neighbouring crests from being mechanically identical.
            case 0 -> amplitude * (Math.sin(warpedAcross / wavelength + regional * 1.7d)
                    + .24d * Math.sin(warpedAcross / (wavelength * .43d) + along / 920d)) + plunge * 1.35d;
            // Asymmetric, tighter fold-and-thrust terrain: harmonic content
            // gives one steep limb and one gentler limb without discontinuous
            // artificial stripes.
            case 1 -> amplitude * (Math.sin(warpedAcross / (wavelength * .86d) + regional * 1.7d)
                    + .34d * Math.sin(warpedAcross / (wavelength * .34d) + along / 820d)
                    + secondaryFold * .22d) + plunge * 1.35d;
            // Monocline: a regional flexure/step with a small superimposed
            // fold, appropriate at the edge of an uplifted block.
            default -> amplitude * .82d * Math.tanh(warpedAcross / 67d)
                    + amplitude * .21d * Math.sin(warpedAcross / 70d + along / 1120d) + plunge * 1.2d;
        };
    }

    /** Quiet settings drape; basins sag; no flat plain gets mountain folds. */
    private static double sedimentaryStructure(long seed, Environment environment, double regional,
                                               double broadFold, double across, double along) {
        return switch (environment.setting()) {
            case OCEAN_BASIN -> regional * 9d + broadFold * 1.6d;
            // A small, smooth elevation-linked component makes coastal beds
            // gently rise landward, while the random field keeps the margin
            // from tracing every tiny bump in the modern surface.
            case PASSIVE_MARGIN -> regional * 10d + broadFold * 2.3d - environment.elevation() * .11d;
            case CONTINENTAL_BASIN -> regional * 16d + broadFold * 3.4d
                    - 7d * noise(seed + 61L, across / 1500d, along / 1500d);
            default -> regional * 8d + broadFold * 2d;
        };
    }

    /**
     * Alternating fault blocks: every spacing interval is offset against its
     * neighbour. This gives visible normal/thrust fault throws without a
     * coordinate-dependent drift that would become absurd far from spawn.
     */
    private static double faultOffset(long seed, double across, double along, double spacing, double throwBlocks) {
        double warped = across + 95d * Math.sin(along / 760d + seed * .00000001d);
        int faultBlock = (int) Math.floor(warped / spacing);
        return Math.floorMod(faultBlock, 2) == 0 ? throwBlocks * .5d : -throwBlocks * .5d;
    }

    /**
     * One persistent basin fault per paired block.  A post-depositional fault
     * offsets an already formed column; a growth fault also changes thickness
     * during deposition.  The latter is confined to wet/riverine continental
     * basins, where coal-bearing sediment can plausibly accumulate.
     */
    private static FaultSample sedimentFault(long seed, Environment environment, int x, int z) {
        double axisAngle = .35d + (noise(seed - 14L, x / 2400d, z / 2400d) + 1d) * .42d;
        double across = x * Math.cos(axisAngle) + z * Math.sin(axisAngle);
        double along = -x * Math.sin(axisAngle) + z * Math.cos(axisAngle);
        double halfSpacing = 1320d;
        double warped = across + 95d * Math.sin(along / 760d + seed * .00000001d);
        int pair = (int) Math.floor(warped / (halfSpacing * 2d));
        double withinPair = warped - pair * halfSpacing * 2d;
        double side = withinPair < halfSpacing ? -1d : 1d;
        double coreDistance = Math.abs(withinPair - halfSpacing);
        boolean eligibleGrowthBasin = environment.setting() == TerrainSetting.CONTINENTAL_BASIN
                || environment.wet() || environment.river();
        boolean synDepositional = eligibleGrowthBasin
                && hash(seed + 7L, pair, (int) Math.floor(along / 1500d)) > .56d;
        double throwBlocks = environment.setting() == TerrainSetting.CONTINENTAL_BASIN ? 15d : 7d;
        // Active growth faults commonly still have a small vertical separation,
        // but much less of a clean rigid offset than a later brittle fault.
        // Growth continues to alter bed thickness across a basin, but the
        // rigid block offset is disabled here: it made a vertical Minecraft
        // wall rather than a readable sedimentary transition.
        double structuralThrow = 0d;
        double growthFactor = synDepositional ? (side < 0d ? .76d : 1.30d) : 1d;
        double coalThicknessFactor = synDepositional ? (side < 0d ? .58d : 1.42d) : 1d;
        return new FaultSample(synDepositional, structuralThrow, growthFactor, coalThicknessFactor, coreDistance);
    }
    private static boolean nearLayer(double value, int interval, double halfThickness) {
        return Math.abs(value - Math.rint(value / interval) * interval) < halfThickness;
    }
    private static double noise(long s, double x, double z) {
        int ix=(int)Math.floor(x), iz=(int)Math.floor(z); double fx=x-ix, fz=z-iz;
        double a=hash(s,ix,iz), b=hash(s,ix+1,iz), c=hash(s,ix,iz+1), d=hash(s,ix+1,iz+1); fx=fx*fx*(3-2*fx); fz=fz*fz*(3-2*fz);
        return (a+(b-a)*fx+(c+(d-c)*fx-a-(b-a)*fx)*fz)*2-1;
    }
    private static double hash(long s, int x, int z) { long n=s^(x*341873128712L)^(z*132897987541L); n=(n^(n>>>30))*0xbf58476d1ce4e5b9L; n=(n^(n>>>27))*0x94d049bb133111ebL; return ((n^(n>>>31))&0x1fffffffffffffL)/(double)0x1fffffffffffffL; }
    /** One finite formation in an ordered stratigraphic succession. */
    private record StratigraphicUnit(String rock, int minimumThickness, int maximumThickness) {
        private int thickness(long seed, int settingSalt, int unitIndex, int x, int z) {
            // Bed thickness changes slowly enough to remain recognisable while
            // avoiding rulers-straight, globally uniform Minecraft layers.
            double regional = (noise(seed + 301L + settingSalt * 97L + unitIndex * 31L, x / 560d, z / 560d) + 1d) * .5d;
            double local = (noise(seed + 337L + settingSalt * 71L + unitIndex * 43L, x / 180d, z / 180d) + 1d) * .5d;
            double blend = regional * .72d + local * .28d;
            return minimumThickness + (int) Math.round((maximumThickness - minimumThickness) * blend);
        }
    }

    /**
     * A finite, oldest-to-youngest column.  Below it lies crystalline basement;
     * above its youngest unit the last depositional/volcanic unit persists until
     * later phases add erosion surfaces and surface regolith.
     */
    private record StratigraphicColumn(String basement, StratigraphicUnit... units) {
        private StratigraphicSample sample(long seed, int settingSalt, int x, int z, double thicknessScale, TerrainSetting setting,
                                           FaultSample fault) {
            int basementRoof = -46 + (int) Math.round(noise(seed + 281L + settingSalt * 59L, x / 720d, z / 720d) * 10d);
            return sampleFromBase(seed, settingSalt, x, z, basementRoof, thicknessScale, setting, fault);
        }

        /** Sample a younger sequence from a supplied erosional base. */
        private StratigraphicSample sampleFromBase(long seed, int settingSalt, int x, int z, int basementRoof,
                                                   double thicknessScale, TerrainSetting setting, FaultSample fault) {
            int top = basementRoof;
            int[] tops = new int[units.length];
            String[] rocks = new String[units.length];
            for (int i = 0; i < units.length; i++) {
                StratigraphicUnit unit = units[i];
                double localScale = thicknessScale;
                // Lateral facies variation: coast and basin beds pinch and
                // swell over hundreds of metres. A unit never vanishes into a
                // one-block artefact, but thin onlap wedges can taper to a
                // readable three-block feather edge.
                if (setting == TerrainSetting.PASSIVE_MARGIN) {
                    double wedge = .18d + (noise(seed + 389L + i * 23L, x / 980d, z / 980d) + 1d) * .46d;
                    localScale *= i == 0 || i == units.length - 1 ? wedge : .58d + wedge * .72d;
                } else if (setting == TerrainSetting.CONTINENTAL_BASIN && unit.rock().equals("conglomerate")) {
                    localScale *= .22d + (noise(seed + 397L + i * 29L, x / 430d, z / 430d) + 1d) * .48d;
                }
                if (fault.synDepositional()) {
                    // In a growth basin the contrast grows during continued
                    // sedimentation.  Older beds record a smaller difference;
                    // younger beds thicken or thin more strongly across the fault.
                    double ageProgress = units.length <= 1 ? 1d : i / (double) (units.length - 1);
                    localScale *= 1d + (fault.growthFactor() - 1d) * (.32d + ageProgress * .68d);
                }
                top += Math.max(3, (int) Math.round(unit.thickness(seed, settingSalt, i, x, z) * localScale));
                tops[i] = top;
                rocks[i] = unit.rock;
            }
            return new StratigraphicSample(basement, basementRoof, tops, rocks);
        }
    }

    private record StratigraphicSample(String basement, int basementRoof, int[] formationTops, String[] rocks) {
        private String rockAt(int stratigraphicY) {
            if (stratigraphicY <= basementRoof) return basement;
            for (int i = 0; i < formationTops.length; i++) {
                if (stratigraphicY <= formationTops[i]) return rocks[i];
            }
            return rocks[rocks.length - 1];
        }

        private BedPosition bedAt(int stratigraphicY) {
            if (stratigraphicY <= basementRoof) {
                return new BedPosition(basement, -1, Integer.MIN_VALUE / 2, basementRoof, stratigraphicY);
            }
            int bottom = basementRoof + 1;
            for (int i = 0; i < formationTops.length; i++) {
                if (stratigraphicY <= formationTops[i]) {
                    return new BedPosition(rocks[i], i, bottom, formationTops[i], stratigraphicY);
                }
                bottom = formationTops[i] + 1;
            }
            return new BedPosition(rocks[rocks.length - 1], rocks.length - 1, bottom, Integer.MAX_VALUE / 2, stratigraphicY);
        }
    }

    /**
     * Choose a mechanically weak detachment horizon from a local restored
     * column. Shale and siltstone are favoured; slate/phyllite can serve in a
     * metamorphic belt. A conservative deep fallback keeps crystalline columns
     * valid without pretending every rock has a convenient glide horizon.
     */
    private static int preferredDetachmentHorizon(StratigraphicSample stratigraphy) {
        int selected = -38;
        double bestScore = -Double.MAX_VALUE;
        for (int y = -58; y <= 42; y++) {
            BedPosition bed = stratigraphy.bedAt(y);
            double weakness = switch (bed.rock()) {
                case "shale" -> 4d;
                case "siltstone" -> 3d;
                case "slate" -> 2.2d;
                case "phyllite" -> 1.6d;
                default -> 0d;
            };
            if (weakness == 0d) continue;
            // Prefer a substantial lower-crustal/lowermost cover horizon over
            // a thin weathering bed near the surface.
            double depthPreference = 42d - Math.abs(y + 27d) * .48d;
            double thicknessBonus = Math.min(16d, Math.max(0, bed.thickness())) * .32d;
            double score = weakness * 15d + depthPreference + thicknessBonus;
            if (score > bestScore) {
                bestScore = score;
                selected = y;
            }
        }
        return selected;
    }

    private record BedPosition(String rock, int index, int bottom, int top, int stratigraphicY) {
        private int thickness() { return top - bottom + 1; }
    }

    private record GeologicalColumn(
            Province province,
            StructuralTransform structure,
            StratigraphicSample stratigraphy,
            StratigraphicSample transportedStratigraphy,
            double dikeDistance,
            double batholithStrength,
            MagmaticSystem magmatism,
            int unconformityY,
            StratigraphicSample youngerCover,
            double regionalMetamorphism,
            FaultSample sedimentFault,
            boolean foldedSedimentary,
            Environment environment
    ) {
        private boolean hasYoungerCover(int y) {
            return youngerCover != null && y >= unconformityY;
        }

        private String countryRockAt(int y) {
            if (hasYoungerCover(y)) {
                // A thin basal conglomerate records the erosional break before
                // younger, comparatively flat sediment is deposited above it.
                if (y <= unconformityY + 1) return "conglomerate";
                return youngerCover.rockAt(y);
            }
            StratigraphicSample source = structure.thrust().isHangingWall(structure.axisCoordinate(), y)
                    && transportedStratigraphy != null ? transportedStratigraphy : stratigraphy;
            return source.rockAt(structure.referenceY(y));
        }

        private BedPosition bedAt(int y) {
            if (hasYoungerCover(y)) return youngerCover.bedAt(y);
            StratigraphicSample source = structure.thrust().isHangingWall(structure.axisCoordinate(), y)
                    && transportedStratigraphy != null ? transportedStratigraphy : stratigraphy;
            return source.bedAt(structure.referenceY(y));
        }
    }

    /**
     * A small, persistent crustal magma plumbing system sampled once per X/Z
     * column. Most of it is already solidified intrusive rock. Active lava is
     * restricted to the deep core of rare young volcanic reservoirs, while the
     * surrounding cracked cooling halo is where mineralising fluids occur.
     */
    private record MagmaticSystem(boolean enabled, boolean live, double localX, double localZ,
                                  double chamberY, double radius, double halfHeight,
                                  double feederAngle, double sillY) {
        private static final MagmaticSystem NONE = new MagmaticSystem(false, false, 0d, 0d,
                0d, 1d, 1d, 0d, 0d);

        private static MagmaticSystem at(long seed, Province province, TerrainSetting setting, int x, int z) {
            // Broadly spaced centres make discrete volcanic/intrusive districts
            // rather than a repeated grid of tiny lava pockets.
            final double spacing = 2600d;
            int cellX = (int) Math.floor(x / spacing);
            int cellZ = (int) Math.floor(z / spacing);
            double bestScore = -Double.MAX_VALUE;
            MagmaticSystem best = NONE;
            for (int cx = cellX - 1; cx <= cellX + 1; cx++) for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
                double occurrence = hash(seed + 3L, cx, cz);
                // Volcanic arcs/rifts are most likely to have young plumbing;
                // plutonic belts preserve fewer, already frozen systems. A
                // collision belt gets rare deep underplating, never a routine
                // lava chamber beneath every mountain.
                double threshold = switch (province) {
                    case VOLCANIC -> .36d;
                    case PLUTONIC -> .77d;
                    case MOUNTAIN -> .90d;
                    default -> 1.1d;
                };
                if (occurrence < threshold) continue;
                double centreX = (cx + .5d + (hash(seed + 5L, cx, cz) - .5d) * .66d) * spacing;
                double centreZ = (cz + .5d + (hash(seed + 7L, cx, cz) - .5d) * .66d) * spacing;
                double localX = x - centreX, localZ = z - centreZ;
                double radius = 54d + hash(seed + 11L, cx, cz) * 62d;
                double horizontal = Math.sqrt(localX * localX + localZ * localZ) / radius;
                // Keep the closest valid centre. A score rather than a raw
                // distance permits a slightly larger centre to win naturally.
                double score = 1d - horizontal + hash(seed + 13L, cx, cz) * .07d;
                if (score < bestScore || horizontal > 2.75d) continue;
                double chamberY = -38d + hash(seed + 17L, cx, cz) * 28d;
                double halfHeight = 9d + hash(seed + 19L, cx, cz) * 10d;
                double feederAngle = hash(seed + 23L, cx, cz) * Math.PI * 2d;
                double sillY = chamberY + halfHeight * (1.15d + hash(seed + 29L, cx, cz) * .75d);
                boolean live = province == Province.VOLCANIC && setting == TerrainSetting.VOLCANIC_ARC
                        && hash(seed + 31L, cx, cz) > .56d;
                bestScore = score;
                best = new MagmaticSystem(true, live, localX, localZ, chamberY, radius, halfHeight, feederAngle, sillY);
            }
            return best;
        }

        private double chamberMetric(int y) {
            if (!enabled) return Double.POSITIVE_INFINITY;
            double horizontal = Math.sqrt(localX * localX + localZ * localZ) / radius;
            double vertical = (y - chamberY) / halfHeight;
            return Math.sqrt(horizontal * horizontal + vertical * vertical);
        }

        private double feederDistance(int y) {
            // Feeder dikes lean gently through the crust. They are not an
            // artificial vertical pillar, and their offset is continuous at
            // all chunk borders.
            double rise = Math.max(0d, y - chamberY);
            double feederX = Math.cos(feederAngle) * rise * .13d;
            double feederZ = Math.sin(feederAngle) * rise * .13d;
            return Math.sqrt(Math.pow(localX - feederX, 2d) + Math.pow(localZ - feederZ, 2d));
        }

        private BlockState liveMeltAt(int y) {
            // A small, sealed liquid core: real magma stores commonly grade
            // outward into crystal mush and solid country rock, not a huge
            // empty molten sphere.
            if (!live || chamberMetric(y) > .34d || y > chamberY + halfHeight * .42d) return null;
            return Blocks.LAVA.defaultBlockState();
        }

        private String solidIntrusionAt(int y) {
            if (!enabled) return null;
            double chamber = chamberMetric(y);
            if (chamber <= 1d) return y < chamberY ? "gabbro" : "diabase";
            // A concordant sill spreads laterally from the upper reservoir.
            // It is thin, discontinuous at its outer edge, and is already
            // solidified, so players meet a geological intrusion rather than
            // a gamey horizontal lava lake.
            double horizontal = Math.sqrt(localX * localX + localZ * localZ) / radius;
            double undulation = noise((long) (feederAngle * 100000d) + 47L, localX / 70d, localZ / 70d) * 1.4d;
            if (horizontal > .62d && horizontal < 2.35d && Math.abs(y - (sillY + undulation)) <= 2d) return "diabase";
            // Above the chamber, the conduit is usually frozen. The small
            // width leaves it discoverable without filling every cave wall.
            if (y >= chamberY - 2d && y <= 88d && feederDistance(y) <= 2.4d) return "diabase";
            return null;
        }

        private double hydrothermalStrength(int y) {
            if (!enabled || y < chamberY - halfHeight * 1.35d || y > 94d) return 0d;
            double chamber = chamberMetric(y);
            // A fractured, cooling chamber margin has the broadest low-grade
            // fluid halo; the upper feeder gives a narrower high-grade path.
            double chamberHalo = chamber < .92d || chamber > 1.62d ? 0d : 1d - Math.abs(chamber - 1.25d) / .37d;
            double feederHalo = y >= chamberY && feederDistance(y) < 15d
                    ? 1d - feederDistance(y) / 15d : 0d;
            double sillHalo = Math.sqrt(localX * localX + localZ * localZ) / radius < 2.6d
                    && Math.abs(y - sillY) < 9d ? 1d - Math.abs(y - sillY) / 9d : 0d;
            return Math.max(chamberHalo * .82d, Math.max(feederHalo, sillHalo * .58d));
        }

        private boolean upperFeederAt(int y) {
            return enabled && y > chamberY + halfHeight * .5d && feederDistance(y) < 4.2d;
        }
    }

    private enum FoldFamily { OPEN, CHEVRON, ISOCLINAL, RECUMBENT }

    /**
     * This maps a world column back through a finite folded ribbon. Instead of
     * a height-field oscillator, each bed is a normal offset from a parametric
     * centreline. The centreline may carry a horizontal component, which lets
     * layers steepen, overturn, and become locally recumbent.
     */
    private record StructuralTransform(FoldRibbon ribbon, double axisCoordinate, double baseOffset,
                                       double mountainRootFold, FaultSystem fault, ThrustSheet thrust) {
        private static StructuralTransform rigid(double offset) {
            return new StructuralTransform(null, 0d, offset, 0d, FaultSystem.NONE, ThrustSheet.NONE);
        }

        private int referenceY(int y) {
            return (int) Math.floor(unthrustedReferenceY(y) + fault.throwAt(y) + thrust.throwAt(axisCoordinate, y));
        }

        /** Restored coordinate before the transported thrust panel is applied. */
        private double unthrustedReferenceY(double y) {
            double unfoldedY = ribbon == null ? y : ribbon.unfoldedY(axisCoordinate, y);
            return unfoldedY + baseOffset - mountainFoldAt((int) Math.round(y));
        }

        /**
         * Finds the present-day Y of a named restored bedding horizon. The
         * short Newton-style solve lets a detachment follow the same folds and
         * mountain root as the beds it rides on, rather than remaining flat.
         */
        private double worldYForRestored(double restoredY) {
            double y = restoredY - baseOffset;
            for (int i = 0; i < 5; i++) {
                double here = unthrustedReferenceY(y);
                double derivative = (unthrustedReferenceY(y + 1d) - unthrustedReferenceY(y - 1d)) * .5d;
                if (Math.abs(derivative) < .08d) break;
                y -= Math.max(-28d, Math.min(28d, (here - restoredY) / derivative));
            }
            return y;
        }

        private StructuralTransform withWeakDetachment(int restoredHorizonY) {
            if (!thrust.enabled()) return this;
            return new StructuralTransform(ribbon, axisCoordinate, baseOffset, mountainRootFold, fault,
                    thrust.withDetachmentHorizon(restoredHorizonY, this));
        }

        /**
         * Mountain-scale anticline rooted low in the crust. At the hot base it
         * is zero; through the crust it smoothly bends bedding upward, reaching
         * its full relief only near the exposed rock. This deliberately avoids
         * the old rigid "raise every bed by N blocks" behaviour.
         */
        private double mountainFoldAt(int y) {
            if (mountainRootFold <= 0d) return 0d;
            // A 230-block root gives the fold room to turn over gradually. A
            // shorter root looked like a sharp vertical strain band in section.
            double throughCrust = smoothstep(Math.max(0d, Math.min(1d, (y + 62d) / 230d)));
            return mountainRootFold * throughCrust;
        }
    }

    /** A finite fold packet in cross-section, solved by nearest-point iteration. */
    private record FoldRibbon(FoldFamily family, double centre, double halfWidth, double verticalAmplitude,
                              double horizontalAmplitude, double phaseShift, double phaseCycles) {
        private static FoldRibbon create(FoldFamily family, long seed, double axisCoordinate, double along, double amplitude) {
            final double spacing = 1950d;
            int cell = (int) Math.floor(axisCoordinate / spacing);
            int alongCell = (int) Math.floor(along / 1950d);
            double centre = (cell + .5d + (hash(seed + 67L, cell, alongCell) - .5d) * .30d) * spacing;
            // Packets overlap much of a compressional belt, not just a rare
            // isolated strip. Flat ground remains between them, so it still
            // reads as a finite structural event rather than an oscillator.
            double halfWidth = 680d + hash(seed + 71L, cell, (int) Math.floor(along / 1950d)) * 150d;
            double phaseShift = (hash(seed + 73L, cell, (int) Math.floor(along / 1950d)) - .5d) * .48d;
            // Most collision-belt packets are broad, open folds. Some are
            // genuinely high-strain zones: the rock is compressed into many
            // tighter folds across the same real-world width. This changes the
            // actual world geometry, not merely the aspect ratio of a preview.
            double strain = hash(seed + 75L, cell, alongCell);
            double phaseCycles;
            double amplitudeScale;
            if (strain > .80d) {
                // Rare, intensely shortened fold trains. The centreline is
                // still sampled as a finite-thickness rock package, so its
                // wavelength must remain wide enough that neighbouring beds
                // cannot geometrically overlap. More extreme shortening is
                // represented by separate thrust sheets below.
                phaseCycles = 3.45d + hash(seed + 77L, cell, alongCell) * .35d;
                amplitudeScale = .78d;
            } else if (strain > .42d) {
                // Intermediate shortening creates visibly compressed but still
                // easily explorable anticline/syncline trains.
                phaseCycles = 2.35d + hash(seed + 79L, cell, alongCell) * 1.05d;
                amplitudeScale = .94d;
            } else {
                phaseCycles = 1.65d;
                amplitudeScale = 1d;
            }
            // Tight chevrons have curvature discontinuities and recumbent
            // curves can approach a reversed limb. Both are valid structures,
            // but a one-block, finite-thickness solver cannot safely pack
            // several of them into one short packet. Keep those expressive
            // styles for broad packets; use a smooth tight train where strain
            // is high, with thrust sheets supplying the separate overlap.
            FoldFamily effectiveFamily = phaseCycles > 3.4d ? FoldFamily.OPEN : family;
            double horizontalAmplitude = switch (effectiveFamily) {
                case OPEN -> 0d;
                case CHEVRON -> amplitude * .42d;
                // When horizontal displacement approaches the fold width, the
                // centreline can turn back: parallel close limbs are isoclinal.
                // The old values permitted the centreline to cross itself.
                // That is not representable by one block per X/Y location:
                // nearest-point inversion then jumps between limbs and makes
                // the noisy, tangled bands seen in the preview. These values
                // still make very steep, close limbs while keeping x(t)
                // monotonic and every formation in the correct order.
                case ISOCLINAL -> halfWidth * .14d;
                // A larger horizontal component and lower vertical relief form
                // the low-angle, lying-over geometry of a recumbent fold.
                case RECUMBENT -> halfWidth * .17d;
            };
            // More cycles means stronger curve gradients. Reduce lateral
            // displacement by the same ratio so x(t) stays monotonic and the
            // nearest-point solver cannot jump between folded limbs.
            horizontalAmplitude *= Math.min(1d, 1.65d / phaseCycles);
            double verticalAmplitude = (effectiveFamily == FoldFamily.RECUMBENT ? amplitude * .68d : amplitude) * amplitudeScale;
            return new FoldRibbon(effectiveFamily, centre, halfWidth, verticalAmplitude, horizontalAmplitude, phaseShift, phaseCycles);
        }

        private double unfoldedY(double worldAcross, double worldY) {
            // Invert the parametric centreline locally. This is only five cheap
            // iterations and is deterministic at chunk boundaries.
            double t = worldAcross;
            for (int i = 0; i < 5; i++) {
                CurvePoint point = pointAt(t);
                CurvePoint before = pointAt(t - 1d);
                CurvePoint after = pointAt(t + 1d);
                double tangentX = (after.x() - before.x()) * .5d;
                double tangentY = (after.y() - before.y()) * .5d;
                double lengthSquared = tangentX * tangentX + tangentY * tangentY;
                if (lengthSquared < .0001d) break;
                double correction = ((worldAcross - point.x()) * tangentX + (worldY - point.y()) * tangentY) / lengthSquared;
                t += Math.max(-96d, Math.min(96d, correction));
            }
            CurvePoint point = pointAt(t);
            CurvePoint before = pointAt(t - 1d);
            CurvePoint after = pointAt(t + 1d);
            double tangentX = (after.x() - before.x()) * .5d;
            double tangentY = (after.y() - before.y()) * .5d;
            double length = Math.sqrt(tangentX * tangentX + tangentY * tangentY);
            if (length < .0001d) return worldY;
            // Signed normal distance is the restored stratigraphic Y. In an
            // undeformed area it reduces exactly to the normal world Y.
            return (worldAcross - point.x()) * (-tangentY / length) + (worldY - point.y()) * (tangentX / length);
        }

        private CurvePoint pointAt(double t) {
            double q = (t - centre) / halfWidth;
            if (Math.abs(q) >= 1d) return new CurvePoint(t, 0d);
            // Smoothly fades the finite folded packet into ordinary flat beds.
            double envelope = Math.cos(q * Math.PI * .5d);
            envelope *= envelope;
            double phase = q * Math.PI * phaseCycles + phaseShift;
            double vertical = switch (family) {
                case OPEN -> verticalAmplitude * envelope * Math.sin(phase);
                case CHEVRON -> verticalAmplitude * envelope * (2d / Math.PI) * Math.asin(Math.sin(phase));
                case ISOCLINAL -> verticalAmplitude * envelope * (.76d * Math.sin(phase) + .18d * Math.sin(phase * 2d));
                case RECUMBENT -> verticalAmplitude * envelope * (.55d * Math.sin(phase) + .34d * Math.sin(phase * 2d));
            };
            double horizontal = horizontalAmplitude * envelope * Math.sin(phase);
            return new CurvePoint(t + horizontal, vertical);
        }
    }

    private record CurvePoint(double x, double y) { }

    /**
     * A thin-skinned fold-and-thrust package. Rather than one isolated wedge,
     * several low-angle splays branch from a deep detachment. Their ramps and
     * flats form an imbricate fan: exactly the stacked, repeated sedimentary
     * packages visible in real mountain-front cross-sections.
     */
    private record ThrustSheet(double centre, double halfWidth, double detachmentReferenceY, double foldedDetachmentY, double rampRise,
                               double transport, int sourceShiftX, int sourceShiftZ,
                               int splayCount, double splaySpacing, boolean enabled) {
        private static final ThrustSheet NONE = new ThrustSheet(0d, 0d, 0d, 0d, 0d, 0d, 0, 0, 0, 0d, false);

        private static ThrustSheet mountain(long seed, double across, double along, double axisAngle) {
            final double spacing = 4700d;
            int cell = (int) Math.floor(across / spacing);
            int alongCell = (int) Math.floor(along / spacing);
            boolean enabled = hash(seed + 3L, cell, alongCell) > .46d;
            double centre = (cell + .5d + (hash(seed + 5L, cell, alongCell) - .5d) * .34d) * spacing;
            double halfWidth = 610d + hash(seed + 7L, cell, alongCell) * 320d;
            // 4–8 degree ramps provide the flat-ramp-flat geometry of a
            // thrust belt, never the old near-vertical break walls.
            double rampRise = .07d + hash(seed + 11L, cell, alongCell) * .07d;
            double detachmentReferenceY = -52d + hash(seed + 13L, cell, alongCell) * 26d;
            // Each belt has one to three splays. The deepest detachment stays
            // broadly coherent; younger splays step upward and forward.
            int splayCount = 1 + (int) Math.floor(hash(seed + 15L, cell, alongCell) * 3d);
            double splaySpacing = 225d + hash(seed + 16L, cell, alongCell) * 135d;
            // This displacement exposes older strata in the hanging wall
            // above younger footwall formations.
            double transport = 48d + hash(seed + 17L, cell, alongCell) * 34d;
            int sourceDistance = 620 + (int) Math.round(hash(seed + 19L, cell, alongCell) * 620d);
            // Across is normal to the fold axis, the expected transport
            // direction for the simple 2-D thrust-section approximation.
            int sourceShiftX = (int) Math.round(Math.cos(axisAngle) * sourceDistance);
            int sourceShiftZ = (int) Math.round(Math.sin(axisAngle) * sourceDistance);
            return new ThrustSheet(centre, halfWidth, detachmentReferenceY, detachmentReferenceY, rampRise, transport,
                    sourceShiftX, sourceShiftZ, splayCount, splaySpacing, enabled);
        }

        /** Cache the present folded position once per X/Z column, not per block. */
        private ThrustSheet withDetachmentHorizon(int restoredHorizonY, StructuralTransform structure) {
            return new ThrustSheet(centre, halfWidth, restoredHorizonY, structure.worldYForRestored(restoredHorizonY), rampRise, transport,
                    sourceShiftX, sourceShiftZ, splayCount, splaySpacing, enabled);
        }

        private boolean isHangingWall(double across, int y) {
            return splayAt(across, y) > 0;
        }

        /** Number of transported thrust panels above this point (0–3). */
        private int splayAt(double across, int y) {
            if (!enabled) return 0;
            double local = across - centre;
            int active = 0;
            for (int splay = 0; splay < splayCount; splay++) {
                // The rear/root boundary steepens only at depth and leans
                // forward upward. It is therefore a buried ramp, not a
                // rectangular sheet edge in the exposed section.
                double rootLimit = -halfWidth - splay * splaySpacing * .22d + (y + 64d) * (4.6d + splay * .35d);
                if (local < rootLimit) continue;
                if (y >= detachmentAt(local, splay)) active = splay + 1;
            }
            return active;
        }

        /** One smooth flat-ramp-flat detachment surface for an imbricate splay. */
        private double detachmentAt(double local, int splay) {
            double rampStart = -halfWidth * .76d + splay * splaySpacing;
            double rampRun = 190d + splay * 44d;
            double rampHeight = 17d + splay * 8d;
            double progress = Math.max(0d, Math.min(1d, (local - rampStart) / rampRun));
            // Beyond the main ramp, the contact continues to climb gently to
            // the foreland instead of ending as a vertical wall.
            double forelandClimb = Math.max(0d, local - (rampStart + rampRun)) * rampRise * .19d;
            // The selected shale/siltstone horizon has been restored into its
            // present folded location once for this column. Each later splay
            // steps up within the same weak package.
            return foldedDetachmentY + splay * 7d + rampHeight * smoothstep(progress) + forelandClimb;
        }

        private double throwAt(double across, int y) {
            int splay = splayAt(across, y);
            if (splay == 0) return 0d;
            double local = across - centre;
            // Fade only at the buried root of the active splay. The jump
            // across the low-angle fault itself remains sharp, as it should.
            int index = splay - 1;
            double rootLimit = -halfWidth - index * splaySpacing * .22d + (y + 64d) * (4.6d + index * .35d);
            double taper = smoothstep(Math.max(0d, Math.min(1d, (local - rootLimit) / 96d)));
            // Negative restored-Y lifts the translated package. Later splays
            // stack additional older panels instead of copying one lone wedge.
            return -transport * (.62d + index * .46d) * taper;
        }

        private int sourceX(int x) { return x - sourceShiftX; }
        private int sourceZ(int z) { return z - sourceShiftZ; }
    }

    /**
     * Sparse dipping faults, with occasional explicitly shaped horst/graben
     * blocks. A dropped graben has faults that converge downward (a funnel);
     * an uplifted horst has faults that diverge downward. The pair is much
     * rarer than a single break, preventing the old repeating wall pattern.
     */
    private record FaultSystem(long seed, double warpedAcross, double along, double spacing) {
        private static final FaultSystem NONE = new FaultSystem(0L, 0d, 0d, Double.POSITIVE_INFINITY);

        private static FaultSystem mountain(long seed, double warpedAcross, double along) {
            // A single fixed coordinate lattice avoids a hidden parameter seam
            // every few kilometres. Each *actual* fault cell selects its own
            // dip, throw, and style below, so the only discontinuities are the
            // geological breaks themselves.
            return new FaultSystem(seed, warpedAcross, along, 5100d);
        }

        private double throwAt(int y) {
            if (this == NONE) return 0d;
            double coordinate = warpedAcross + 68d * Math.sin(along / 760d);
            long cell = (long) Math.floor(coordinate / spacing);
            int alongCell = (int) Math.floor(along / 3300d);
            // 0.22–0.52 horizontal blocks per vertical block: approximately
            // 63–77 degrees from horizontal, common for steep brittle faults.
            double dip = .22d + hash(seed + 5L, (int) cell, alongCell) * .30d;
            double throwBlocks = 8d + hash(seed + 13L, (int) cell, alongCell) * 10d;
            boolean paired = hash(seed + 19L, (int) cell, alongCell) > .48d;
            boolean centralDown = hash(seed + 23L, (int) cell, alongCell) > .5d;
            double pairedHalfWidth = 180d + hash(seed + 29L, (int) cell, alongCell) * 250d;
            if (paired) {
                // Use one centre per large fault province. At y=96 both fault
                // traces bracket the central block; below that level their
                // dips define the funnel/anti-funnel geometry.
                double centre = (cell + .5d) * spacing;
                double local = coordinate - centre;
                double depth = 96d - y;
                double inward = centralDown ? 1d : -1d;
                double leftFault = -pairedHalfWidth + inward * depth * dip;
                double rightFault = pairedHalfWidth - inward * depth * dip;
                double low = Math.min(leftFault, rightFault);
                double high = Math.max(leftFault, rightFault);
                if (local > low && local < high) {
                    // Positive restored-Y moves a bed downward in world space:
                    // that is a graben. A negative value makes the central
                    // block a horst.
                    return centralDown ? throwBlocks * .5d : -throwBlocks * .5d;
                }
                return 0d;
            }
            double faultCoordinate = coordinate + y * dip;
            long faultCell = (long) Math.floor(faultCoordinate / spacing);
            // A single dipping plane separates two large blocks. Alternating
            // the baseline prevents displacement from accumulating forever.
            return Math.floorMod(faultCell, 2L) == 0L ? -throwBlocks * .5d : throwBlocks * .5d;
        }
    }

    private record FaultSample(boolean synDepositional, double structuralThrow, double growthFactor,
                               double coalThicknessFactor, double coreDistance) {
        private static final FaultSample NONE = new FaultSample(false, 0d, 1d, 1d, Double.POSITIVE_INFINITY);
    }

    private record PlateSample(
            int primaryCellX,
            int primaryCellZ,
            int secondaryCellX,
            int secondaryCellZ,
            int primaryType,
            double boundaryDistance
    ) { }

    /** The local climate/terrain inputs supplied by the active Terralith world. */
    private record Environment(
            boolean dry, boolean wet, boolean hot, boolean cold, boolean mountain,
            boolean aquatic, boolean river, boolean beach, boolean volcanic, int relief, boolean underwater,
            int waterDepth, int elevation, int broadElevation, TerrainSetting setting
    ) {
        private boolean rugged() { return mountain || relief >= 3; }
    }

    /** The terrain-guided geological setting, selected before rock placement. */
    private enum TerrainSetting {
        OCEAN_BASIN, PASSIVE_MARGIN, CONTINENTAL_BASIN, STABLE_INTERIOR, COLLISION_BELT, VOLCANIC_ARC
    }

    private enum Province { SEDIMENTARY, MOUNTAIN, PLUTONIC, VOLCANIC }
}
