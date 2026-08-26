package com.azumoo.realgeology.tools;

import com.azumoo.realgeology.worldgen.GeologicalProvincesFeature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fast, no-Minecraft-window renderer for forced-collision-belt cross-sections. */
public final class CrossSectionRenderer {
    private static final int FROM_Y = -64;
    private static final int TO_Y = 340;
    private static final int HALF_LENGTH = 4096;
    // Nine terrain-aware strips ask the real generator for full base columns.
    // Sixteen-block sampling preserves kilometre-scale folds/faults while
    // keeping the headless preview below Minecraft's watchdog threshold.
    private static final int STEP = 16;
    // Keep the output at its true cross-section scale: one horizontal block
    // occupies the same width as one vertical block. With 16-block samples
    // this deliberately produces a very wide SVG, but avoids any visual
    // squashing of folds, dips, and faults.
    private static final int CELL_X = STEP * 2;
    private static final int CELL_Y = 2;

    private static final Map<String, String> COLOURS = new LinkedHashMap<>();
    static {
        COLOURS.put("gneiss", "#8c7c70");
        COLOURS.put("conglomerate", "#aa8968");
        COLOURS.put("shale", "#4a5357");
        COLOURS.put("siltstone", "#9b806b");
        COLOURS.put("limestone", "#c7c2a1");
        COLOURS.put("dolomite", "#d3bca0");
        COLOURS.put("quartzite", "#d8c8af");
        COLOURS.put("slate", "#566270");
        COLOURS.put("phyllite", "#76906f");
        COLOURS.put("schist", "#a37d72");
        COLOURS.put("marble", "#e2ddd3");
        COLOURS.put("amphibolite", "#414b48");
        COLOURS.put("magma", "#cc5b29");
    }

    private CrossSectionRenderer() { }

    public static Path renderDefault(ServerLevel level) throws IOException {
        return render(level, level.getSeed(), new PreviewSite("origin", 0, 0));
    }

    /** Render representative mountain, lowland, and basin/ocean sites from the actual terrain generator. */
    public static List<Path> renderAtlas(ServerLevel level) throws IOException {
        long seed = level.getSeed();
        PreviewSite[] sites = representativeSites(level);
        return List.of(
                render(level, seed, sites[0]),
                render(level, seed, sites[1]),
                render(level, seed, sites[2])
        );
    }

    private static Path render(ServerLevel level, long seed, PreviewSite site) throws IOException {
        Path outputDirectory = Path.of(System.getProperty("realgeology.preview_output", "build/cross-sections"));
        Path output = outputDirectory.resolve("terrain-geology-" + site.name() + "-" + seed + ".svg");
        List<PanelData> panels = List.of(
                samplePanel(level, seed, site.x(), site.z(), 1, 0, "east-west"),
                samplePanel(level, seed, site.x(), site.z(), 0, 1, "north-south"),
                samplePanel(level, seed, site.x(), site.z(), 1, 1, "diagonal")
        );
        Files.createDirectories(output.getParent());
        // The true-scale export is the accurate measurement view. The second
        // view intentionally exaggerates vertical relief so folded strata are
        // easy to assess at a glance without losing the original reference.
        Files.writeString(output, svg(seed, site, panels, CELL_X, "true-scale horizontal/vertical axes"), StandardCharsets.UTF_8);
        Files.writeString(output.resolveSibling(output.getFileName().toString().replace(".svg", "-exaggerated-16x.svg")),
                svg(seed, site, panels, CELL_Y, "16× vertical exaggeration (visual interpretation view)"), StandardCharsets.UTF_8);
        Files.writeString(output.resolveSibling(output.getFileName().toString().replace(".svg", ".json")),
                dataJson(seed, site, panels), StandardCharsets.UTF_8);
        return output.toAbsolutePath();
    }

    private static String svg(long seed, PreviewSite site, List<PanelData> panels, int cellX, String scaleLabel) {
        int width = (HALF_LENGTH * 2 / STEP) * cellX;
        int height = (TO_Y - FROM_Y + 1) * CELL_Y;
        int panelGap = 66;
        int totalHeight = height * 3 + panelGap * 3 + 50;
        StringBuilder svg = new StringBuilder(1_600_000);
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width)
                .append("\" height=\"").append(totalHeight).append("\" viewBox=\"0 0 ")
                .append(width).append(' ').append(totalHeight).append("\">\n")
                .append("<rect width=\"100%\" height=\"100%\" fill=\"#172027\"/>")
                .append("<style>text{font-family:system-ui,sans-serif;fill:#e8edf0}.muted{fill:#aebcc5}</style>\n")
                .append("<text x=\"12\" y=\"25\" font-size=\"19\">Real Geology — terrain-aware forced collision-belt preview</text>")
                .append("<text x=\"12\" y=\"45\" class=\"muted\" font-size=\"13\">seed ").append(seed)
                .append(" · ").append(site.name()).append(" at X ").append(site.x()).append(", Z ").append(site.z())
                .append(" · normal terrain/surface rules + geology overlay · ").append(scaleLabel).append(" · Y ")
                .append(FROM_Y).append(" to ").append(TO_Y).append("</text>\n");
        panel(svg, panels.get(0), 50, width, height, cellX);
        panel(svg, panels.get(1), 50 + height + panelGap, width, height, cellX);
        panel(svg, panels.get(2), 50 + (height + panelGap) * 2, width, height, cellX);
        legend(svg, 12, 50 + (height + panelGap) * 3 - 12);
        svg.append("</svg>\n");
        return svg.toString();
    }

    private static void panel(StringBuilder svg, PanelData panel, int top, int width, int height, int cellX) {
        svg.append("<g transform=\"translate(0 ").append(top).append(")\">")
                .append("<rect width=\"").append(width).append("\" height=\"").append(height)
                .append("\" fill=\"#28343a\" stroke=\"#9bb0bb\"/>");
        // Adjacent samples in a bed normally have the same colour. Write a
        // single horizontal run instead of one SVG element per block: previews
        // stay compact and open quickly even at four kilometres wide.
        int sampleCount = HALF_LENGTH * 2 / STEP;
        for (int y = FROM_Y; y <= TO_Y; y++) {
            String runColour = null;
            int runStart = 0;
            int xPixel = 0;
            int yIndex = y - FROM_Y;
            for (int index = 0; index < sampleCount; index++, xPixel += cellX) {
                String colour = colourFor(panel.materials()[yIndex][index]);
                if (runColour == null) {
                    runColour = colour;
                    runStart = xPixel;
                } else if (!runColour.equals(colour)) {
                    rect(svg, runStart, (TO_Y - y) * CELL_Y, xPixel - runStart, runColour);
                    runColour = colour;
                    runStart = xPixel;
                }
            }
            rect(svg, runStart, (TO_Y - y) * CELL_Y, width - runStart, runColour);
        }
        int seaY = (TO_Y - 63) * CELL_Y;
        svg.append("<path d=\"M0 ").append(seaY).append("H").append(width)
                .append("\" stroke=\"#b9e7ff\" stroke-width=\"1\" opacity=\".75\"/>")
                .append("<text x=\"10\" y=\"20\" font-size=\"15\">").append(panel.label())
                .append(" — ").append(HALF_LENGTH * 2).append(" blocks</text></g>\n");
    }

    private static void rect(StringBuilder svg, int x, int y, int width, String colour) {
        svg.append("<rect x=\"").append(x).append("\" y=\"").append(y)
                .append("\" width=\"").append(width).append("\" height=\"").append(CELL_Y)
                .append("\" fill=\"").append(colour).append("\"/>");
    }

    private static String materialAt(long seed, int x, int y, int z, int terrainY, int broadTerrainY,
                                     int oceanFloorY, BlockState terrain) {
        Block block = terrain.getBlock();
        if (y > terrainY) return "sky"; // sky above the normal terrain surface
        // Retain water only when it forms the surface-connected water column
        // above the ocean floor. Raw base columns also contain aquifers/caves;
        // those must become solid geology in this cave-free preview.
        if (block == Blocks.WATER && y > oceanFloorY && oceanFloorY < terrainY) return "water";
        if (block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || block == Blocks.PODZOL) return "grass";
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT || block == Blocks.CLAY) return "soil";
        if (block == Blocks.SAND || block == Blocks.RED_SAND || block == Blocks.SANDSTONE || block == Blocks.RED_SANDSTONE) return "sand";
        // Preserve the generator's thin natural mantle even where its exact
        // top material is unavailable through a biome surface rule.
        if (y >= terrainY - 2) return "soil";
        return GeologicalProvincesFeature.debugForcedCollisionRock(seed, x, y, z, broadTerrainY);
    }

    private static String colourFor(String material) {
        return switch (material) {
            case "sky" -> "#8ec9f0";
            case "water" -> "#4f94c4";
            case "grass" -> "#729c42";
            case "soil" -> "#76563e";
            case "sand" -> "#d6bd78";
            default -> COLOURS.getOrDefault(material, "#ff00ff");
        };
    }

    /**
     * Samples exactly the same grid used by the SVG. Keeping the raw labels,
     * instead of only colours, makes it possible to measure layer thickness,
     * fault throw, fold wavelength, and thermal-base depth without image
     * interpretation.
     */
    private static PanelData samplePanel(ServerLevel level, long seed, int originX, int originZ,
                                         int directionX, int directionZ, String label) {
        int sampleCount = HALF_LENGTH * 2 / STEP;
        int[] x = new int[sampleCount];
        int[] z = new int[sampleCount];
        int[] surface = new int[sampleCount];
        int[] broadSurface = new int[sampleCount];
        int[] oceanFloor = new int[sampleCount];
        NoiseColumn[] terrainColumns = new NoiseColumn[sampleCount];
        for (int index = 0, offset = -HALF_LENGTH; offset < HALF_LENGTH; offset += STEP, index++) {
            x[index] = originX + offset * directionX;
            z[index] = originZ + offset * directionZ;
            terrainColumns[index] = level.getChunkSource().getGenerator().getBaseColumn(x[index], z[index],
                    level, level.getChunkSource().randomState());
            surface[index] = level.getChunkSource().getGenerator().getBaseHeight(x[index], z[index],
                    Heightmap.Types.WORLD_SURFACE_WG, level, level.getChunkSource().randomState()) - 1;
            oceanFloor[index] = level.getChunkSource().getGenerator().getBaseHeight(x[index], z[index],
                    Heightmap.Types.OCEAN_FLOOR_WG, level, level.getChunkSource().randomState()) - 1;
        }
        // Match the low-pass uplift driver used by world generation. A five
        // sample window at 16-block steps approximates the in-world 32-block
        // cross while remaining independent of cut direction.
        for (int index = 0; index < sampleCount; index++) {
            int total = surface[index] * 2;
            int weight = 2;
            for (int offset = -2; offset <= 2; offset++) if (offset != 0) {
                int neighbour = index + offset;
                if (neighbour >= 0 && neighbour < sampleCount) { total += surface[neighbour]; weight++; }
            }
            broadSurface[index] = total / weight;
        }
        String[][] materials = new String[TO_Y - FROM_Y + 1][sampleCount];
        for (int y = FROM_Y; y <= TO_Y; y++) for (int index = 0; index < sampleCount; index++) {
            materials[y - FROM_Y][index] = materialAt(seed, x[index], y, z[index], surface[index], broadSurface[index], oceanFloor[index],
                    terrainColumns[index].getBlock(y));
        }
        return new PanelData(label, directionX, directionZ, x, z, surface, broadSurface, oceanFloor, materials);
    }

    /**
     * A deliberately dependency-free JSON export. Material rows are RLE
     * encoded from top to bottom: [material-name, number-of-samples]. The
     * sample coordinates and surface/ocean floor arrays allow an analysis
     * tool to reconstruct every run in world coordinates.
     */
    private static String dataJson(long seed, PreviewSite site, List<PanelData> panels) {
        StringBuilder json = new StringBuilder(1_100_000);
        json.append("{\n  \"schema\": \"realgeology-cross-section-v1\",")
                .append("\n  \"seed\": ").append(seed).append(',')
                .append("\n  \"site\": {\"name\": \"").append(site.name()).append("\", \"x\": ")
                .append(site.x()).append(", \"z\": ").append(site.z()).append("},")
                .append("\n  \"sampling\": {\"horizontal_step_blocks\": ").append(STEP)
                .append(", \"vertical_step_blocks\": 1, \"y_min\": ").append(FROM_Y)
                .append(", \"y_max\": ").append(TO_Y).append("},")
                .append("\n  \"panels\": [");
        for (int panelIndex = 0; panelIndex < panels.size(); panelIndex++) {
            PanelData panel = panels.get(panelIndex);
            if (panelIndex > 0) json.append(',');
            json.append("\n    {\"label\": \"").append(panel.label()).append("\", \"direction\": [")
                    .append(panel.directionX()).append(',').append(panel.directionZ()).append("], \"columns\": [");
            for (int index = 0; index < panel.x().length; index++) {
                if (index > 0) json.append(',');
                json.append('[').append(panel.x()[index]).append(',').append(panel.z()[index]).append(',')
                        .append(panel.surface()[index]).append(',').append(panel.broadSurface()[index]).append(',')
                        .append(panel.oceanFloor()[index]).append(']');
            }
            json.append("], \"layers_rle\": [");
            for (int y = TO_Y; y >= FROM_Y; y--) {
                if (y < TO_Y) json.append(',');
                json.append("{\"y\":").append(y).append(",\"runs\":[");
                String[] row = panel.materials()[y - FROM_Y];
                int runStart = 0;
                for (int index = 1; index <= row.length; index++) {
                    if (index < row.length && row[index].equals(row[runStart])) continue;
                    if (runStart > 0) json.append(',');
                    json.append("[\"").append(row[runStart]).append("\",").append(index - runStart).append(']');
                    runStart = index;
                }
                json.append("]}");
            }
            json.append("]}");
        }
        return json.append("\n  ]\n}\n").toString();
    }

    private static PreviewSite[] representativeSites(ServerLevel level) {
        PreviewSite highest = new PreviewSite("mountain", 0, 0);
        PreviewSite lowest = new PreviewSite("basin-or-ocean", 0, 0);
        PreviewSite gentleLand = new PreviewSite("lowland", 0, 0);
        int highY = Integer.MIN_VALUE;
        int lowY = Integer.MAX_VALUE;
        int bestLandDistance = Integer.MAX_VALUE;
        // A coarse survey chooses deterministic, recognisable terrain types
        // for this seed without generating chunks or looking at vegetation.
        for (int x = -8192; x <= 8192; x += 1024) for (int z = -8192; z <= 8192; z += 1024) {
            int y = level.getChunkSource().getGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    level, level.getChunkSource().randomState()) - 1;
            if (y > highY) { highY = y; highest = new PreviewSite("mountain", x, z); }
            if (y < lowY) { lowY = y; lowest = new PreviewSite("basin-or-ocean", x, z); }
            if (y >= 64 && Math.abs(y - 76) < bestLandDistance) {
                bestLandDistance = Math.abs(y - 76);
                gentleLand = new PreviewSite("lowland", x, z);
            }
        }
        return new PreviewSite[]{highest, gentleLand, lowest};
    }

    private static void legend(StringBuilder svg, int x, int y) {
        int cursor = x;
        for (Map.Entry<String, String> entry : COLOURS.entrySet()) {
            svg.append("<rect x=\"").append(cursor).append("\" y=\"").append(y - 13)
                    .append("\" width=\"12\" height=\"12\" fill=\"").append(entry.getValue()).append("\"/>")
                    .append("<text x=\"").append(cursor + 16).append("\" y=\"").append(y - 2)
                    .append("\" font-size=\"12\">").append(entry.getKey()).append("</text>");
            cursor += 16 + entry.getKey().length() * 7 + 18;
        }
    }

    private record PreviewSite(String name, int x, int z) { }

    private record PanelData(String label, int directionX, int directionZ, int[] x, int[] z,
                             int[] surface, int[] broadSurface, int[] oceanFloor, String[][] materials) { }
}
