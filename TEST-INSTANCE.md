# Real Geology — minimal beta test instance

Isolated NeoForge **1.21.1** environment for pre-publish QA. Matches what testers get on Modrinth: Real Geology + GeoStrata (+ Architectury), vanilla overworld generation — **no Terralith**, no Modern Industry & Colonies pack.

## NeoForge 26.2 bare test instance (no GeoStrata)

Minecraft **26.2** / NeoForge **26.2.0.66** — Real Geology dev classpath only (`RockPalette` vanilla fallbacks). No GeoStrata or Architectury.

| Path | Role |
|------|------|
| `run-publish-test-26.2/` | Game directory — gitignored except `README.md` |
| `libs-26.2/` | Optional local JARs when GeoStrata 26.2 exists |

Gradle runs: `publishTestClient` / `publishTestServer` → `:neoforge-26.2:runPublishTestClient` / `runPublishTestServer`.

```bash
./scripts/launch-test-instance-26.2.sh
./gradlew :neoforge-26.2:build
./gradlew :neoforge-26.2:runPublishTestClient
```

Pre-seed `run-publish-test-26.2/config/realgeology-common.toml` with `worldgen_mode = "section"` before first world (same as 1.21.1 publish test). Requires **Java 25** toolchain (Gradle Foojay).


## Current mod version

Check `gradle.properties` (`mod_version`). As of setup: **0.21.0-beta.2** — vanilla ore spawning restored; custom Real Geology deposit placement disabled in worldgen (strata/provinces unchanged).

## Test instance path

| Path | Role |
|------|------|
| `run-publish-test/` | Game directory (saves, config, logs) — gitignored except `README.md` |
| `libs/` | GeoStrata + Architectury JARs for Gradle `localRuntime` (not committed) |
| Project sources | Real Geology loaded from dev classpath (not a copied JAR during Gradle runs) |

Legacy dev folder `run-geology-minimal/` remains for older structural tests; **use `run-publish-test/` for publish beta QA.**

Gradle run configs: `publishTestClient` / `publishTestServer` → tasks `runPublishTestClient` / `runPublishTestServer`.

## Exact mod list (runtime)

| Mod | Source |
|-----|--------|
| NeoForge 21.1.248 | Gradle / NeoForge dev (Minecraft 1.21.1) |
| Real Geology `0.21.0-beta.2` (or current `mod_version`) | Project `sourceSets.main` |
| GeoStrata `1.2.0-1.21.1-NEOFORGE` | `libs/geostrata-1.2.0-1.21.1-NEOFORGE.jar` |
| Architectury `13.0.11-neoforge` | `libs/architectury-13.0.11-neoforge.jar` |

Only `localRuntime` deps from `libs/` are added — no other modpack mods.

## One-time: GeoStrata JARs in `libs/`

If `libs/*.jar` are missing:

1. Download GeoStrata from [Modrinth](https://modrinth.com/mod/geostrata) (and Architectury if needed), **or**
2. Copy from your pack: `~/.minecraft/versions/Modern Industry & Colonies/mods/`

Expected filenames (see `libs/README.md`):

- `geostrata-1.2.0-1.21.1-NEOFORGE.jar`
- `architectury-13.0.11-neoforge.jar`

`./scripts/launch-test-instance.sh` copies these from `MODPACK_MODS` when `libs/` is empty, then verifies both JARs exist.

## Launch commands

### Recommended (script)

```bash
cd minecraft-givekit-project/geology-overhaul
./scripts/launch-test-instance.sh              # client (default)
./scripts/launch-test-instance.sh --server     # dedicated server
./scripts/launch-test-instance.sh client --no-build
```

### Gradle directly

```bash
./gradlew build
./gradlew runPublishTestClient
./gradlew runPublishTestServer
```

Other Gradle runs (different game dirs):

- `runClient` — default NeoForge `run/` (may include unrelated mods if present)
- `runServer` / `runGeologyPreview` — `run-geology-minimal/`

## New world only

Real Geology changes apply only to **newly generated chunks**. Do not expect retroactive fixes in an old save.

1. Use a disposable world for each config mode you test.
2. Copy config into `run-publish-test/config/realgeology-common.toml` **before** first world creation (or edit after first launch and restart).
3. Suggested world names: `beta-vanilla-ores-<version>` (normal play) or `beta-fold-debug-<version>` (section mode).

## Recommended `realgeology-common.toml`

Full template: `docs/publish/realgeology-common-beta.toml`.

### Normal beta play (vanilla ores + strata)

```toml
[debug]
worldgen_mode = "off"
force_collision_belt = false
```

Verify folded strata in caves/cliffs, GeoStrata rock types, and **vanilla** iron/coal/etc. ore generation (beta.2).

### Fold inspection (disposable world)

Permanent 50 m debug corridors in **new** chunks — do not use on a world you want to keep.

```toml
[debug]
worldgen_mode = "section"
force_collision_belt = true
```

Fly into air trenches at Y ~64 to read fold geometry. For ore-shape QA only (not beta.2 vanilla ore check):

```toml
worldgen_mode = "ores"
force_collision_belt = false
```

### Half-world cut (screenshot composition)

Single vertical cut face at **X = 0**: terrain and fluids on **X < 0** are removed; **X >= 0** keeps normal folded strata. Use a disposable world spawned near origin.

```toml
worldgen_mode = "half_cut"
force_collision_belt = true
```

Stand on the positive-X side and look west along the cut plane for layer exposure. A late `debug_cutaway_sanitizer` feature runs after trees/snow/grass and clears the removed half to air; fluids are stripped in the removed half plus a two-block buffer on the kept side (X = 0 and X = 1), with a second pass two ticks after chunk load to stop post-gen flow.

## Simulating a Modrinth install (optional)

Gradle dev always loads Real Geology from sources. To stage JARs like a player install:

```bash
./scripts/launch-test-instance.sh --stage-jars
```

Copies `realgeology-*.jar` plus `libs/*.jar` into `run-publish-test/mods/`. Still launch with `runPublishTestClient` for NeoForge.

## Screenshots / shaders

Optional — **not** part of the published mod or Modrinth dependency list. Shader mods are never listed in `neoforge.mods.toml` or the Modrinth dependency graph.

**For screenshot quality**, `--shaders` copies **Sodium**, **Iris**, **shaderpack** `.zip` files, and **Iris/Sodium config** from your modpack game directory (`MODPACK_GAME_DIR`, default `~/.minecraft/versions/Modern Industry & Colonies/`) into `run-publish-test/`. JARs come from `MODPACK_MODS` (`…/mods/`).

If Iris loads but the world looks flat/vanilla, check `run-publish-test/logs/latest.log` for `Shaders are disabled because no valid shaderpack is selected` — that means `shaderpacks/` was empty or `config/iris.properties` had `shaderPack=` blank. Re-run with `--shaders` so Complementary (or your pack) and `iris.properties` are copied.

### What Modern Industry & Colonies provides (NeoForge 1.21.1)

| Source | Item |
|--------|------|
| `mods/` | `sodium-neoforge-…`, `iris-neoforge-…`, optional `entityculling-neoforge-…` |
| `shaderpacks/` | e.g. `ComplementaryReimagined_r5.8.1.zip` |
| `config/` | `iris.properties` (`shaderPack=…`, `enableShaders=true`), `sodium-options.json`, `sodium-fingerprint.json`, `sodium-mixins.properties`, `iris-excluded.json` |

No Embeddium/Oculus in this pack — the script still tries those JAR patterns for other installs.

```bash
./scripts/launch-test-instance.sh --shaders --dry-run   # list what would be copied (no launch)
./scripts/launch-test-instance.sh --shaders             # stage mods + shaderpack + config, then launch
MODPACK_GAME_DIR=/path/to/instance ./scripts/launch-test-instance.sh --shaders
ENABLE_SHADERS=1 ./scripts/launch-test-instance.sh      # same via env var
```

### Complementary / Reimagined (or any pack)

1. Ensure the `.zip` exists in the modpack’s `shaderpacks/` (or copy it there once).
2. Run `./scripts/launch-test-instance.sh --shaders` (or `--no-build` on relaunch after staging).
3. Confirm `run-publish-test/config/iris.properties` has `shaderPack=YourPack.zip` and the file is under `run-publish-test/shaderpacks/`.
4. In game, **Options → Video Settings → Shader Packs** should show the pack enabled (Iris reads `iris.properties` on startup).

To use a different pack without the modpack: drop the `.zip` into `run-publish-test/shaderpacks/`, set `shaderPack=` in `run-publish-test/config/iris.properties`, restart the client.


## Dev vs publish debug policy

| Setting | Shipped mod default (`RealGeologyConfig.java`) | Modrinth / release JAR |
|---------|-----------------------------------------------|-------------------------|
| `debug.worldgen_mode` | `"off"` | Same — NeoForge generates `config/realgeology-common.toml` from code defaults on first run |
| `debug.force_collision_belt` | `false` | Same |

- **Worldgen debug** (`section` / `ores`) is **config-only**: no separate debug artifact; players opt in by editing `realgeology-common.toml` before creating a disposable world.
- **`/realgeology debug …` commands** ship in the release JAR but require operator permission (level 2). They are temporary in-world cutaways (restored on Save & Quit), independent of `worldgen_mode`. Safe for publish: defaults keep automatic section corridors **off**.
- **This test instance** may pre-seed `run-publish-test/config/realgeology-common.toml` with `worldgen_mode = "section"` for layer QA — do **not** copy that file into publish docs; use `docs/publish/realgeology-common-beta.toml` (`worldgen_mode = "off"`) for tester handoff.

## Git

- `run-publish-test/*` worlds and logs are ignored; `run-publish-test/README.md` is tracked.
- Do not commit `libs/*.jar` (already in `.gitignore`).
