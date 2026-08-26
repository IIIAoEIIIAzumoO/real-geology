Place optional runtime JARs here for local Gradle runs. These files are **not** published with Real Geology.

| File | Purpose |
|------|---------|
| `geostrata-1.2.0-1.21.1-NEOFORGE.jar` | Required rock library for dev/testing |
| `architectury-13.0.11-neoforge.jar` | GeoStrata dependency |

Download GeoStrata from [Modrinth](https://modrinth.com/mod/geostrata) or CurseForge. Do not commit these JARs to git.

End users install GeoStrata separately; the release JAR only declares it as a required dependency in `neoforge.mods.toml`.
