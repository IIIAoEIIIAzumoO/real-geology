# Modrinth paste-ready text — Real Geology 0.21.0-beta.2

Copy sections below into the Modrinth project **Description** and version **Changelog** fields.

---

## Summary (one line — Modrinth project subtitle)

Seed-stable folded strata and geological provinces for NeoForge 1.21.1.

---

## Full description (paste into project Description)

Real Geology replaces generic underground stone with **seed-stable geological provinces**: folded sedimentary strata, metamorphic mountain belts, plutons, volcanic arcs, faults, and cross-cutting intrusions — all deterministic from world seed so columns meet cleanly across chunk borders.

This **0.21.0-beta.2** release focuses on **structural geology**. Ore placement uses **vanilla Minecraft worldgen** (coal, iron, copper, gold, diamond, etc.) while custom deposit shapes finish development.

### Requirements

| Mod | Version | Notes |
|-----|---------|-------|
| **NeoForge** | 21.1.x | Minecraft **1.21.1** |
| **[GeoStrata](https://www.curseforge.com/minecraft/mc-mods/geostrata)** | 1.2.0+ | **Required** — rock block library. Not on Modrinth yet; install from CurseForge. |

Real Geology **does not ship rock textures**. It arranges GeoStrata's named rocks into formations.

**Optional (recommended):** Terralith, Geophilic — Real Geology reads biome/terrain tags when present for richer provinces.

### What you get

- Terrain-guided provinces: ocean basin, passive margin, continental basin, collision belt, plutonic province, volcanic arc
- Finite stratigraphic columns with smoothly varying bed thickness
- Fold families in mountain belts, plus faults, dikes, contact aureoles, and angular unconformities
- Debug modes to inspect folds in disposable test worlds (`/realgeology debug section`)

### Important — read before installing

- **Supported: Minecraft Java 1.21.1 only** for this beta. NeoForge 26.2 builds exist for development only and are **not** published here.
- **New worlds only** — existing chunks are never rewritten; only newly generated terrain uses Real Geology.
- **Experimental beta** — worldgen will change between releases. Use a copy of your instance or a fresh profile.
- **GeoStrata required** — there is no standalone rock pack yet.

### Known limitations (not beta blockers)

Playtesting shows the mod is stable enough for exploration, but some province transitions are rough at biome edges (especially in debug section view). Full details: [Known issues — plate/province generation](https://github.com/IIIAoEIIIAzumoO/real-geology/blob/main/docs/KNOWN-ISSUES-PLATE-GEN.md)

Other planned work: karst caves following soluble rock, owned sedimentary blocks, custom ore deposits — see [ROADMAP](https://github.com/IIIAoEIIIAzumoO/real-geology/blob/main/ROADMAP.md).

### Links

- **Source & issues:** https://github.com/IIIAoEIIIAzumoO/real-geology
- **Support (optional):** https://ko-fi.com/azumoo

### License

MIT — Real Geology source code only. GeoStrata is a separate mod with its own license.

---

## Version changelog (paste into version 0.21.0-beta.2)

### 0.21.0-beta.2 — Folded strata beta

Beta republish: **vanilla ore spawning** while custom deposits finish development.

**Changed**
- Removed the biome modifier that stripped vanilla ore placed features
- Disabled Real Geology custom deposit placement in worldgen (hosted ore blocks remain registered but do not generate)

**Unchanged**
- Folded strata, provinces, faults, and intrusions

**Requirements**
- NeoForge 21.1.x · Minecraft 1.21.1
- GeoStrata 1.2+ (install from CurseForge)

**Warnings**
- New worlds only
- Experimental beta — worldgen may change between releases

---

## Modrinth form quick reference

| Field | Value |
|-------|-------|
| Name | Real Geology |
| Slug | `real-geology` |
| Categories | World Gen |
| Client / Server | Both |
| License | MIT |
| Version number | `0.21.0-beta.2` |
| Version name | Folded strata beta |
| Channel | Beta |
| Loaders | NeoForge |
| Game versions | **1.21.1** |
| JAR file | `build/libs/realgeology-0.21.0-beta.2.jar` |

**Dependencies (set in UI + mention in description):**
- NeoForge (1.21.1)
- GeoStrata — manual / external link until on Modrinth
