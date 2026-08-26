package com.azumoo.realgeology.compat;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Resolves stratigraphic rock names to block states. Uses GeoStrata when present;
 * otherwise maps to a small vanilla palette so 26.2 can compile and boot without GeoStrata.
 */
public final class RockPalette {
    private RockPalette() { }

    public static BlockState rockState(String rockName) {
        BlockState geo = GameCompat.blockState("geostrata", rockName);
        if (geo != null && geo.getBlock() != Blocks.AIR) {
            return geo;
        }
        return vanillaFallback(rockName).defaultBlockState();
    }

    public static BlockState hostedOreState(String host, String ore) {
        BlockState geo = GameCompat.blockState("geostrata", host + "_" + ore + "_ore");
        if (geo != null && geo.getBlock() != Blocks.AIR) {
            return geo;
        }
        return vanillaFallback(host).defaultBlockState();
    }

    public static BlockState namedState(String id, String fallbackRock) {
        BlockState state = GameCompat.blockState(id);
        if (state != null && state.getBlock() != Blocks.AIR) {
            return state;
        }
        return rockState(fallbackRock);
    }

    private static Block vanillaFallback(String rockName) {
        return switch (rockName) {
            case "shale", "siltstone", "phyllite", "slate", "mudstone" -> Blocks.TUFF;
            case "limestone", "dolomite", "marble", "chalk" -> Blocks.CALCITE;
            case "sandstone", "conglomerate", "arkose" -> Blocks.SANDSTONE;
            case "gneiss", "schist", "hornfels", "quartzite", "novaculite" -> Blocks.DEEPSLATE;
            case "granite", "diorite", "pegmatite" -> Blocks.GRANITE;
            case "gabbro", "basalt", "diabase", "amphibolite", "peridotite" -> Blocks.BASALT;
            case "rhyolite", "scoria", "tuff" -> Blocks.TUFF;
            case "basaltic_glass" -> Blocks.OBSIDIAN;
            case "rock_salt", "halite" -> GameCompat.haliteVanillaRock();
            default -> Blocks.STONE;
        };
    }
}
