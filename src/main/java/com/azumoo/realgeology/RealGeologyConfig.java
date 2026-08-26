package com.azumoo.realgeology;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/** Small, deliberately opt-in settings for disposable new-world inspection. */
public final class RealGeologyConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<String> WORLDGEN_DEBUG_MODE;
    private static final ModConfigSpec.BooleanValue FORCE_COLLISION_BELT;
    private static final ModConfigSpec.IntValue THERMAL_BASE_MAGMA_THICKNESS;
    private static final ModConfigSpec.DoubleValue THERMAL_BASE_LAVA_POCKET_CHANCE;
    private static final ModConfigSpec.BooleanValue VARIABLE_TECTONIC_BASE;
    private static final ModConfigSpec.IntValue TECTONIC_BASE_MIN_THICKNESS;
    private static final ModConfigSpec.IntValue TECTONIC_BASE_MAX_THICKNESS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("debug");
        WORLDGEN_DEBUG_MODE = builder
                .comment("off = normal generation; section = repeated 50 m geology cutaways; ores = repeated ore-only cutaways;",
                        "half_cut = remove all terrain and fluids on X < 0, keeping X >= 0 for a single vertical cut-face screenshot.",
                        "Section and ores modes make a full-height 50 m-wide corridor that continues as far as generated chunks do.",
                        "Use only for a newly created disposable test world. Worldgen debug changes are permanent in generated chunks.")
                .defineInList("worldgen_mode", "off", List.of("off", "section", "ores", "half_cut"));
        FORCE_COLLISION_BELT = builder
                .comment("For a disposable structural test only: treat all new Overworld terrain as a collision belt.",
                        "This makes the fold transform visible immediately. Leave false for normal terrain-guided generation.")
                .define("force_collision_belt", false);
        builder.pop();
        builder.push("thermal_base");
        THERMAL_BASE_MAGMA_THICKNESS = builder
                .comment("Number of blocks directly above bottom bedrock replaced with solid magma blocks in new chunks.",
                        "Lava pockets remain sealed inside this layer unless a player mines into them.")
                .defineInRange("magma_thickness", 4, 0, 12);
        THERMAL_BASE_LAVA_POCKET_CHANCE = builder
                .comment("Chance for a column of the lowest thermal-base layer to be a sealed lava pocket instead of magma.",
                        "Set to 0.0 for a completely solid magma boundary.")
                .defineInRange("lava_pocket_chance", 0.055d, 0d, 0.30d);
        VARIABLE_TECTONIC_BASE = builder
                .comment("Experimental: vary the exposed hot asthenosphere boundary by plate setting, instead of using a uniform thickness.",
                        "Young ocean/rift settings expose more hot material; old continents and mountain roots expose less.",
                        "Leave false for ordinary worlds. This does not add a liquid lava ocean.")
                .define("variable_tectonic_base", false);
        TECTONIC_BASE_MIN_THICKNESS = builder
                .comment("Minimum exposed hot-asthenosphere thickness beneath thick continental/mountain roots when variable_tectonic_base is enabled.")
                .defineInRange("tectonic_base_min_thickness", 4, 4, 64);
        TECTONIC_BASE_MAX_THICKNESS = builder
                .comment("Maximum exposed hot-asthenosphere thickness beneath young oceanic/rift lithosphere when variable_tectonic_base is enabled.")
                .defineInRange("tectonic_base_max_thickness", 50, 4, 64);
        builder.pop();
        SPEC = builder.build();
    }

    private RealGeologyConfig() { }

    public static WorldgenDebugMode worldgenDebugMode() {
        return WorldgenDebugMode.fromConfig(WORLDGEN_DEBUG_MODE.get());
    }

    public static boolean forceCollisionBelt() {
        return FORCE_COLLISION_BELT.get();
    }

    public static int thermalBaseMagmaThickness() {
        return THERMAL_BASE_MAGMA_THICKNESS.get();
    }

    public static double thermalBaseLavaPocketChance() {
        return THERMAL_BASE_LAVA_POCKET_CHANCE.get();
    }

    public static boolean variableTectonicBase() {
        return VARIABLE_TECTONIC_BASE.get();
    }

    public static int tectonicBaseMinThickness() {
        return TECTONIC_BASE_MIN_THICKNESS.get();
    }

    public static int tectonicBaseMaxThickness() {
        return TECTONIC_BASE_MAX_THICKNESS.get();
    }

    public enum WorldgenDebugMode {
        OFF, SECTION, ORES, HALF_CUT;

        public static WorldgenDebugMode fromConfig(String value) {
            return switch (value) {
                case "section" -> SECTION;
                case "ores" -> ORES;
                case "half_cut" -> HALF_CUT;
                default -> OFF;
            };
        }

        public boolean isActive() {
            return this != OFF;
        }

        /** Columns removed to air (and stripped of fluids) for debug inspection. */
        public boolean cutsAt(int x, int z) {
            return switch (this) {
                case OFF -> false;
                case HALF_CUT -> x < 0;
                case SECTION, ORES -> Math.floorMod(x + 25, 512) < 50 || Math.floorMod(z + 25, 512) < 50;
            };
        }

        /** Columns where fluids should not remain — removed volume plus one block on the kept side. */
        public boolean stripsFluidsAt(int x, int z) {
            if (cutsAt(x, z)) return true;
            if (!isActive()) return false;
            return cutsAt(x - 1, z) || cutsAt(x + 1, z) || cutsAt(x, z - 1) || cutsAt(x, z + 1);
        }
    }
}
