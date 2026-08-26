# Cross-section renderer

This is a fast geometry preview for the **forced collision-belt** model. It samples the same fold-ribbon, fault-block, formation thickness, and thin-interbed logic used by Real Geology, without launching Minecraft or generating chunks.

From `geology-overhaul`, run:

```bash
./gradlew runGeologyPreview
```

It boots a short-lived headless NeoForge server, writes three SVGs into `build/cross-sections/`, then stops itself. It surveys the current seed and selects a mountain, gentle lowland, and basin/ocean origin. Each output has 8,192-block east–west, north–south, and diagonal strips from Y -64 to 340.

The renderer samples the normal generator's base terrain column, preserving air/sky, water, grass/dirt, sand, and lava. It intentionally excludes trees, caves, structures, and mobs. Beneath the natural surface mantle it overlays the forced collision-belt geological model, making it suitable for judging whether folds and faults relate sensibly to mountains, plains, and basins. It samples every 16 horizontal blocks but draws them at a 4× vertically exaggerated display scale; this is stated so structural slopes are not mistaken for true-scale slopes.

Open the resulting SVG in a browser or image viewer. This is a structural preview, so it intentionally excludes terrain, caves, ores, soil, structures, and surface vegetation. It lets us tune the rock geometry first; Minecraft remains the final terrain-integrated check.
