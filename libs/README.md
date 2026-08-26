Place optional runtime JARs here for local Gradle runs. These files are **not** published with Real Geology.

| File | Purpose |
|------|---------|
| `geostrata-1.2.0-1.21.1-NEOFORGE.jar` | Required rock library for dev/testing |
| `architectury-13.0.11-neoforge.jar` | GeoStrata dependency |

Optional screenshot-only mods (test instance — **not** shipped in the Real Geology release JAR):

| File | Purpose |
|------|---------|
| `sodium-neoforge-*-mc1.21.1.jar` | Performance renderer for Iris (copied to `run-publish-test/mods/` via `--shaders`) |
| `iris-neoforge-*-mc1.21.1.jar` | Shader pack loader for NeoForge 1.21.1 |
| `worldedit-mod-*.jar` | Optional manual fluid QA via `--screenshot-mods` (not required — Real Geology freezes fluids in debug modes) |

Download from [Modrinth](https://modrinth.com/mod/sodium) / [Iris](https://modrinth.com/mod/iris), or copy from your modpack. Do not add these to `libs/` for Gradle `localRuntime` unless you want them on every dev run.

End users install GeoStrata separately; the release JAR only declares it as a required dependency in `neoforge.mods.toml`.
