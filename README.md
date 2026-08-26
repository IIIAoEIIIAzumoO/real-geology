# Real Geology

**NeoForge · beta**

Real Geology replaces generic underground stone with seed-stable geological provinces: folded sedimentary strata, metamorphic mountain belts, plutons, volcanic arcs, faults, and deposit shapes that behave like real geology rather than a repeating block list.

## Supported versions

**Supported: Minecraft Java 1.21.1 only** for this public beta (NeoForge 21.1.x + GeoStrata 1.2+).

| Minecraft | NeoForge | Status | GeoStrata | Artifact |
|-----------|----------|--------|-----------|----------|
| **1.21.1** | 21.1.x | **Supported beta** | Required | `neoforge-1.21.1/build/libs/realgeology-0.21.0-beta.2.jar` |
| **26.2** | 26.2.x | **Experimental compile** — not published to Modrinth | Not available — vanilla rock fallback | `neoforge-26.2/build/libs/realgeology-26.2-0.21.0-beta.2.jar` |

See [PORTING-26.md](PORTING-26.md) for the 26.2 port status, blockers, and build commands.

**NeoForge 1.21.1 · beta** (primary release target)

Real Geology replaces generic underground stone with seed-stable geological provinces: folded sedimentary strata, metamorphic mountain belts, plutons, volcanic arcs, faults, and deposit shapes that behave like real geology rather than a repeating block list.

The current beta focus is **structural geology** — anticlines, synclines, fold-and-thrust belts, unconformities, and cross-cutting intrusions — all deterministic from world seed and coordinates so columns meet cleanly across chunk borders.

> Minecraft as Notch intended, in the sense of *place*: underground should feel like a landscape you could read on a geologic map, not three grey blocks and ore sprinkled at random.

## What you get today

- Terrain-guided provinces: ocean basin, passive margin, continental basin, collision belt, plutonic province, volcanic arc
- Finite stratigraphic columns with smoothly varying bed thickness
- Fold families in mountain belts, plus faults, dikes, contact aureoles, and angular unconformities
- **Vanilla ore spawning** — coal, iron, copper, gold, diamond, and the rest use Minecraft's default worldgen for this beta
- Hosted ore blocks and deposit logic are in the repo for the next release (porphyry stockworks, MVT districts, kimberlite pipes, and more)
- Debug modes to inspect folds in disposable test worlds

See [MATERIAL-CATALOG.md](MATERIAL-CATALOG.md) for the full ore mapping and [ROADMAP.md](ROADMAP.md) for what comes next (karst caves, owned rock blocks, surface hydrology).

## Requirements

| Mod | Role |
|-----|------|
| **NeoForge 21.1.x** | Loader |
| **[GeoStrata 1.2+](https://modrinth.com/mod/geostrata)** | Rock block library (shale, gneiss, limestone, …) |

Real Geology **does not ship rock textures**. It arranges GeoStrata's named rocks into formations. This mod owns world generation and structural geology; ore placement uses vanilla Minecraft rules until the custom deposit pass ships.

Terralith/Geophilic are optional but recommended: Real Geology reads biome and terrain tags when present to choose sedimentary facies and province type.

## Install (beta testers)

1. Install NeoForge 1.21.1, **GeoStrata 1.2+**, and `realgeology-0.21.0-beta.2.jar`.
2. Use a **copy** of your instance or a fresh profile — not your main survival world.
3. Create a **brand-new world**. Existing terrain is never rewritten; only newly generated chunks use Real Geology.
4. Explore underground or use debug modes (below).

## Important warnings

- **Experimental beta** — worldgen will change between releases.
- **New world only** for serious testing.
- **GeoStrata required** — there is no standalone rock pack yet (see [ROADMAP.md](ROADMAP.md)).

### Known issues (post-beta, not blockers)

Aug 2026 playtesting found rough province transitions at some biome edges and debug-section artifacts. Gameplay is stable enough for beta testers; fixes are tracked for after launch. Details: [docs/KNOWN-ISSUES-PLATE-GEN.md](docs/KNOWN-ISSUES-PLATE-GEN.md).

## Debug: inspect folds without WorldEdit

In `config/realgeology-common.toml` **before** creating a test world:

```toml
[debug]
worldgen_mode = "section"          # air trenches every 512 m — exposes strata
force_collision_belt = true        # every column uses the fold belt model
```

Delete the test world when done. For a temporary slice in a normal world:

```
/realgeology debug section
/realgeology debug clear
```

Export SVG cross-sections from Gradle:

```bash
./gradlew runGeologyPreview
# → build/cross-sections/*.svg
```

## Building from source

```bash
# 1.21.1 (primary) — place GeoStrata (+ Architectury) JARs in libs/ — see libs/README.md
./gradlew :neoforge-1.21.1:build

# 26.2 (experimental) — requires JDK 25; no GeoStrata yet
./gradlew :neoforge-26.2:build

# Both targets
./gradlew build
```

Dev server with GeoStrata (1.21.1):

```bash
./gradlew :neoforge-1.21.1:runServer
```

Contributors: read [CONTRIBUTING.md](CONTRIBUTING.md). Release maintainers: [PUBLISHING.md](PUBLISHING.md) and [docs/PUBLISH-CHECKLIST.md](docs/PUBLISH-CHECKLIST.md).

## License

Real Geology source code: [MIT](LICENSE).

GeoStrata is a separate mod with its own license. Do not redistribute GeoStrata assets with this project.

## Vision (longer term)

- **Karst caves** following soft soluble rock between hard beds
- **Owned sedimentary blocks** (sandstone, mudstone, evaporites) so Real Geology can eventually run without GeoStrata
- **Surface hydrology** — salt lakes, glaciers, peat (documented in the design notes)
- **Dr Stone-style progression** — realistic resource chains built on top of accurate geology (future, needs contributors)

If this direction interests you, open an issue or PR. The generator is the hard part; there's plenty of room to help without burning tokens on boilerplate.

## Support development

Real Geology is free and open source (MIT). If a beta world saves you time or you want karst caves sooner:

- [GitHub Issues](https://github.com/AzumoO/real-geology/issues) — bug reports and feature discussion
- [Ko-fi](https://ko-fi.com/azumoo) — optional tip jar (update the link if your handle differs)
- Star the repo — helps other players find it

Contributors welcome: [CONTRIBUTING.md](CONTRIBUTING.md)
