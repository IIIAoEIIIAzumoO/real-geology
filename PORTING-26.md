# Porting Real Geology to Minecraft 26.2

Status as of **2026-08-26** (mod `0.21.0-beta.2`).

## Summary

| Target | NeoForge | Java | Gradle plugin | Compile | Runtime |
|--------|----------|------|---------------|---------|---------|
| **1.21.1** | `21.1.248` | 21 | ModDevGradle `2.0.144` | ✅ | ✅ Full (GeoStrata required) |
| **26.2** | `26.2.0.66` | 25 | NeoGradle userdev `7.1.38` | ✅ | ⚠️ Partial (no GeoStrata) |

NeoForge **does exist** for Minecraft 26.2. Official MDK: [NeoForgeMDKs/MDK-26.2-NeoGradle](https://github.com/NeoForgeMDKs/MDK-26.2-NeoGradle).

```properties
minecraft_version=26.2
minecraft_version_range=[26.2]
neo_version=26.2.0.66
```

Requires **Gradle 9.1+** (project uses 9.2.1) and **Java 25** for the 26.2 subproject.

## Blocker: GeoStrata

**GeoStrata is not published for Minecraft 26.x** (only `1.2.0-1.21.1-NEOFORGE` exists on CurseForge; not on Modrinth).

Real Geology's stratigraphy, ore hosts, and block models reference `geostrata:*` blocks extensively (~30 rock types, hosted ore parents). Without GeoStrata:

- The mod **compiles** for 26.2
- The mod **loads** (GeoStrata dependency removed from 26.2 `neoforge.mods.toml`)
- Underground generation uses **`RockPalette` vanilla fallback** (stone/deepslate/granite/basalt/tuff mapped by rock name)
- Hosted ore JSON models still parent `geostrata:block/ore_block` — **missing textures/models** until GeoStrata ports or owned rock blocks ship (see [ROADMAP.md](ROADMAP.md))

### Paths to full 26.2 support

1. **Wait for GeoStrata 26.2** from Alkeari (preferred; preserves current art pipeline)
2. **Owned rock blocks** — migrate `geostrata:*` → `realgeology:*` with original textures (ROADMAP milestone)
3. **Ship vanilla-stone-only mode** — already partially implemented via `RockPalette`; document as experimental 26.2 profile

## What was implemented

### Dual-version Gradle layout (Option A: subprojects)

```
geology-overhaul/
├── src/main/java/          # shared mod logic
├── src/main/resources/
├── neoforge-1.21.1/        # ModDevGradle + Parchment + Java 21
│   └── src/main/java/.../compat/GameCompat.java
├── neoforge-26.2/          # NeoGradle userdev + Java 25
│   └── src/main/java/.../compat/GameCompat.java
├── settings.gradle         # include both subprojects
└── build.gradle            # root aggregator
```

Build commands:

```bash
./gradlew :neoforge-1.21.1:build    # release JAR for 1.21.1
./gradlew :neoforge-26.2:build      # experimental 26.2 JAR
./gradlew build                     # both
```

Run configs (1.21.1 only, unchanged paths):

```bash
./gradlew :neoforge-1.21.1:runServer
./gradlew :neoforge-1.21.1:runGeologyPreview
```

### API porting (26.2)

Shared code uses version-specific `GameCompat` shims for breaking changes:

| 1.21.1 | 26.2 |
|--------|------|
| `ResourceLocation` | `Identifier` |
| `getMinBuildHeight()` | `getMinY()` |
| `key.location()` | `key.identifier()` |
| `BuiltInRegistries.BLOCK.get(id)` | `BuiltInRegistries.BLOCK.getValue(id)` |
| `hasPermission(2)` | `permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)` |
| `getSharedSpawnPos()` / `setDefaultSpawnPos()` | `getRespawnData()` / `setRespawnData()` |

`RockPalette` resolves rock names to GeoStrata blocks when present, else vanilla substitutes.

## Not yet done

- [x] In-game boot test on NeoForge 26.2 client — **fails** at registry (`realgeology:coal_ore` / block id not set); use `run-publish-test-26.2` + `./scripts/launch-test-instance-26.2.sh`
- [ ] Modrinth second game version entry for 26.2 (wait until GeoStrata or owned rocks)
- [ ] Replace `geostrata:block/ore_block` model parents for 26.2 standalone mode
- [ ] CI matrix building both subprojects
- [x] `scripts/launch-test-instance-26.2.sh` + Gradle `publishTestClient` / `publishTestServer` in `neoforge-26.2/build.gradle`

## Recommended next steps

1. **Keep shipping 1.21.1** as the supported beta (`0.21.0-beta.x`)
2. **Contact Alkeari** about GeoStrata 26.2 timeline
3. **Parallel track:** owned sedimentary/igneous blocks (ROADMAP) — unblocks 26.2 without waiting
4. When GeoStrata 26.2 exists: drop JARs in `libs-26.2/`, flip `geostrata_dependency_block` to `required` in `neoforge-26.2/build.gradle`, test worldgen

## References

- [NeoForge 26.1 release notes](https://neoforged.net/news/26.1release/) — Java 25, unobfuscated sources, version scheme
- [MDK-26.2-NeoGradle](https://github.com/NeoForgeMDKs/MDK-26.2-NeoGradle) — canonical `gradle.properties` values
- GeoStrata 1.21.1: CurseForge / local `libs/` (see [libs/README.md](libs/README.md))
