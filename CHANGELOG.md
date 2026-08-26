# Changelog

## 0.21.0-beta.2 — 2026-08-26

Beta republish: **vanilla ore spawning** while custom deposits finish development.

### Added

- Debug worldgen mode `half_cut`: removes terrain and fluids on **X < 0**, keeping **X >= 0** for vertical cut-face screenshots
- Fluid stripping in all active debug modes (`section`, `ores`, `half_cut`) — no flowing waterfalls off cut faces
- Test instance `--shaders` flag: optional Sodium NeoForge + Iris NeoForge for publish QA screenshots (not in release JAR)

### Changed

- Removed the biome modifier that stripped vanilla `minecraft:ore_*` placed features
- Disabled Real Geology custom deposit placement in worldgen (hosted ore blocks remain registered but do not generate)

### Notes

- Folded strata, provinces, faults, and intrusions are unchanged
- Custom deposit shapes (porphyry stockworks, kimberlite pipes, MVT districts, etc.) are planned for the next update

## 0.21.0-beta.1 — 2026-08-26

First public beta. Focus: **folded strata and structural geology**.

### Added

- Seed-stable geological provinces tied to terrain/climate (basin, margin, collision belt, pluton, volcanic arc)
- Finite stratigraphic columns with smoothly varying bed thickness
- Collision-belt fold families: anticline/syncline, asymmetric fold-and-thrust, monocline
- Post-depositional and syn-depositional faults, angular unconformities, cross-cutting dikes
- Deposit-specific ore shapes (porphyry stockworks, MVT districts, kimberlite pipes, banded iron, and more)
- Host-aware ores with mineral-accurate display names and `c:ores/*` compatibility
- Debug section/ore raster modes and `/realgeology debug` commands
- Gradle cross-section export (`runGeologyPreview`)

### Requirements

- NeoForge 21.1.x for Minecraft 1.21.1
- GeoStrata 1.2+ (required — rock block library)

### Known limitations

- **New worlds only** — existing chunks are not rewritten
- Caves do not yet follow lithology (karst pass planned — see ROADMAP.md)
- GeoStrata must be installed separately; Real Geology does not ship rock textures
- Best tested with Terralith terrain; vanilla worlds work but provinces are less varied
