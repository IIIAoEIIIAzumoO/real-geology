# Contributing to Real Geology

Thank you for interest in this project. Real Geology is early beta software: the folding/strata generator works, but cave coupling, owned rock blocks, and pack integration still need design and testing.

## Before you start

1. Read [README.md](README.md) for install constraints (**new world only**).
2. Read [ROADMAP.md](ROADMAP.md) for planned work — pick an item there or open an issue first for larger changes.
3. Skim [GEOLOGY-DESIGN-NOTES.md](GEOLOGY-DESIGN-NOTES.md) for generation ownership rules (Real Geology owns Overworld rock/ore placement in supported packs).

## Dev environment

Requirements:

- JDK 21
- NeoForge 1.21.1 (see `gradle.properties`)

```bash
# Drop runtime test deps into libs/ (not committed):
#   geostrata-1.2.0-1.21.1-NEOFORGE.jar
#   architectury-13.0.11-neoforge.jar  (GeoStrata dependency)

./gradlew build
./gradlew :neoforge-1.21.1:runServer          # minimal GeoStrata integration test
./gradlew runGeologyPreview  # exports SVG cross-sections to build/cross-sections/
```

Regenerate ore overlay textures after editing source art:

```bash
./generate-hosted-ore-textures.sh
```

## What we merge

- Bug fixes with a seed/world repro or a cross-section export
- Worldgen changes that preserve chunk-border continuity (same seed + coordinates → same column)
- Documentation and config clarifications
- Small, focused features that match [ROADMAP.md](ROADMAP.md)

## What needs discussion first

- New required mod dependencies
- Copying textures or blocks from other mods
- Breaking changes to ore IDs or `c:tags` compatibility
- Large refactors of `GeologicalProvincesFeature.java` without tests or preview exports

## Testing expectations

For worldgen changes:

1. Build with `./gradlew build`
2. Create a **disposable** test world (never your main save)
3. Optionally set `debug.force_collision_belt = true` and `debug.worldgen_mode = "section"` in `config/realgeology-common.toml` before world creation to inspect folds
4. Attach `build/cross-sections/*.svg` or describe seed + coordinates in the PR

## Code style

Match the existing Java in this repo: explicit geological naming, minimal abstraction, comments only where the geology is non-obvious.

## License

Contributions are accepted under the [MIT License](LICENSE). You must not submit third-party assets (textures, jars, modpack files) unless you have rights to distribute them.
