# Optional runtime JARs for NeoForge 26.2 dev

When GeoStrata publishes for 26.2, place JARs here for integration testing:

| File | Role |
|------|------|
| `geostrata-*-26.2-NEOFORGE.jar` | Rock library |
| `architectury-*-neoforge.jar` | GeoStrata dependency |

Not committed to git. The 26.2 subproject uses `localRuntime` from this folder.

Until then, Real Geology falls back to vanilla stone palette via `RockPalette` (see [PORTING-26.md](../PORTING-26.md)).
