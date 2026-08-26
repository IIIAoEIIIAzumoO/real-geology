# Real Geology — minimal beta test instance

Isolated NeoForge **1.21.1** environment for pre-publish QA. Matches what testers get on Modrinth: Real Geology + GeoStrata (+ Architectury), vanilla overworld generation — **no Terralith**, no Modern Industry & Colonies pack.

## Current mod version

Check `gradle.properties` (`mod_version`). As of setup: **0.21.0-beta.2** — vanilla ore spawning restored; custom Real Geology deposit placement disabled in worldgen (strata/provinces unchanged).

## Test instance path

| Path | Role |
|------|------|
| `run-publish-test/` | Game directory (saves, config, logs) — gitignored except `README.md` |
| `libs/` | GeoStrata + Architectury JARs for Gradle `localRuntime` (not committed) |
| Project sources | Real Geology loaded from dev classpath (not a copied JAR during Gradle runs) |

Legacy dev folder `run-geology-minimal/` remains for older structural tests; **use `run-publish-test/` for publish beta QA.**

## Exact mod list (runtime)

| Mod | Source |
|-----|--------|
| NeoForge 21.1.248 | Gradle / NeoForge dev (Minecraft 1.21.1) |
| Real Geology `0.21.0-beta.2` (or current `mod_version`) | Project `sourceSets.main` |
| GeoStrata `1.2.0-1.21.1-NEOFORGE` | `libs/geostrata-1.2.0-1.21.1-NEOFORGE.jar` |
| Architectury `13.0.11-neoforge` | `libs/architectury-13.0.11-neoforge.jar` |

## One-time: GeoStrata JARs in `libs/`

If `libs/*.jar` are missing:

1. Download GeoStrata from [Modrinth](https://modrinth.com/mod/geostrata) (and Architectury if needed), **or**
2. Copy from your pack: `~/.minecraft/versions/Modern Industry & Colonies/mods/`

Expected filenames (see `libs/README.md`):

- `geostrata-1.2.0-1.21.1-NEOFORGE.jar`
- `architectury-13.0.11-neoforge.jar`

`./scripts/launch-test-instance.sh` can copy these automatically when `MODPACK_MODS` points at that folder.

## Launch commands

### Recommended (script)

```bash
cd minecraft-givekit-project/geology-overhaul
./scripts/launch-test-instance.sh client    # default
./scripts/launch-test-instance.sh server
./scripts/launch-test-instance.sh client --no-build   # skip compile
```

### Gradle directly

```bash
./gradlew build
./gradlew runPublishClient
./gradlew runPublishServer
```

Other Gradle runs (different game dirs):

- `runClient` — default NeoForge `run/` (may include unrelated mods if present)
- `runServer` / `runGeologyPreview` — `run-geology-minimal/`

## New world checklist

1. **New world only** — existing chunks are not rewritten.
2. Optional: apply beta config from `docs/publish/realgeology-common-beta.toml` into `run-publish-test/config/realgeology-common.toml` before first world creation.
3. Create a single-player world (client) or start server and create world via usual flow.
4. Suggested world name: `beta-vanilla-ores-<version>` for beta.2 ore verification.
5. Verify: folded strata in caves/surface, GeoStrata rock types, **vanilla** iron/coal/etc. ore generation (beta.2).
6. Debug: `/realgeology debug` (see mod docs). For section corridors, set `worldgen_mode = "section"` only on a disposable world.

## Simulating a Modrinth install (optional)

Gradle dev always loads Real Geology from sources. To stage JARs like a player install (e.g. inspect `mods/`):

```bash
./scripts/launch-test-instance.sh --stage-jars
```

This copies the built `realgeology-*.jar` plus `libs/*.jar` into `run-publish-test/mods/`. Still launch with `runPublishClient` for NeoForge; for a **standalone** `.minecraft` instance you must install NeoForge separately (not covered here).

## Git

- `run-publish-test/*` worlds and logs are ignored.
- Do not commit `libs/*.jar` (already in `.gitignore`).
