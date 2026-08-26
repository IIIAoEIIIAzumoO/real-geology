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

Stand on the positive-X side and look west along the cut plane for layer exposure. Fluids are stripped in the removed half and on the block adjacent to the cut face so waterfalls do not spill into the void.

## Simulating a Modrinth install (optional)

Gradle dev always loads Real Geology from sources. To stage JARs like a player install:

```bash
./scripts/launch-test-instance.sh --stage-jars
```

Copies `realgeology-*.jar` plus `libs/*.jar` into `run-publish-test/mods/`. Still launch with `runPublishTestClient` for NeoForge.

## Screenshots / shaders

Optional — **not** part of the published mod or Modrinth dependency list.

### Shader JARs in Modern Industry & Colonies (`MODPACK_MODS`)

Observed in `~/.minecraft/versions/Modern Industry & Colonies/mods/` (NeoForge 1.21.1):

| JAR | Role |
|-----|------|
| `sodium-neoforge-0.8.13-beta.2%2Bmc1.21.1.jar` | Sodium (note `%2B` instead of `+` in filename) |
| `iris-neoforge-1.8.14-beta.1+mc1.21.1.jar` | Iris |
| `entityculling-neoforge-1.10.5-mc1.21.1.jar` | Optional perf (not copied by `--shaders`) |

No Embeddium/Oculus in this pack — script still tries those patterns for other installs.

`--shaders` globs: `*mc1.21.1*` for Sodium/Iris (handles `+` and `%2B`). Missing shader JARs are **optional**; launch continues.

NeoForge 1.21.1 uses **Sodium NeoForge + Iris NeoForge** (not Embeddium/Oculus). Your Modern Industry & Colonies pack already has compatible JARs.

```bash
./scripts/launch-test-instance.sh --shaders              # copy Iris + Sodium into run-publish-test/mods/
ENABLE_SHADERS=1 ./scripts/launch-test-instance.sh       # same via env var
```

1. Launch with `--shaders` (copies from `MODPACK_MODS` or download from Modrinth).
2. Drop a `.zip` shader pack into `run-publish-test/shaderpacks/`.
3. In game: **Options → Video Settings → Shader Packs** → select pack.

Shader mods load from `run-publish-test/mods/` only; they are never listed in `neoforge.mods.toml`.


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
