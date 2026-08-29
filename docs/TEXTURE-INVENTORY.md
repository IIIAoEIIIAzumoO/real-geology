# Texture Inventory — Modern Industry & Colonies + Real Geology

**Generated:** 2026-08-29  
**Scope:** Project source folders under `minecraft-givekit-project/` plus installed resource packs in `~/.minecraft/versions/Modern Industry & Colonies/resourcepacks/`.  
**Method:** PNG dimension scan via ImageMagick `identify`; provenance from build scripts (`build-*.sh`, `generate-*.sh`) and README files. No textures were extracted from mod JARs.

---

## Executive summary

| Category | User-created (128×) | Derived / pipeline | Third-party pack | Missing / gap |
|----------|--------------------:|-------------------:|-----------------:|--------------:|
| GeoStrata rock families (27 types × raw/brick/cobble/polished) | **162** colour faces (+324 PBR maps) | — | GeoStrata mod provides 16× defaults (overridden) | 0 variant gaps |
| Real Geology mod textures (ore overlays + kimberlite) | **23** materials × 3 maps = **69** PNGs | — | — | 0 |
| Vanilla / terrain blocks (grass, snow, stone, etc.) | **1** original (`moss_block`) | **35** via `unified-ores-hd-128x` LabPBR pass | ModernArch R 128× base art | **4** gaps (see Terrain) |
| Create + TFMG geology palettes | **~294** colour (+ PBR) | Sources from GeoStrata + industry photos | Create/TFMG 16× defaults | 0 in project |
| Chipped cobble variants | **44** materials × 3 maps = **132** PNGs | HD bases from GeoStrata / vanilla-rock pipeline | Chipped mod 16× defaults | 0 |
| Industrial ore block textures (Mek/IE/Rail/TFMG/Create) | **48** colour in `modern-industry-materials-hd-128x` | Consolidated in `unified-ores-hd-128x` | Mod 16× defaults | 0 in project |

### Key findings

1. **All 27 GeoStrata rock types used by Real Geology have complete 128× HD coverage** (raw, bricks, cobble side/top/bottom, polished) in `geostrata-hd-overlay-128x/`, each with `_n` / `_s` PBR maps.
2. **Real Geology ships only ore-overlay and kimberlite textures** — not rock faces. Rock blocks remain GeoStrata IDs; HD art lives in the resource-pack overlay.
3. **No 16× placeholder textures exist in the Real Geology mod** for future owned rock blocks (ROADMAP milestone). When owned blocks ship, new 128× art will be needed.
4. **Water block textures** are not overridden in any project pack; the modpack relies on **Complementary Reimagined shader water** (intentionally — see `QOL_AND_VISUALS.md`).
5. **Some source photos are AI-assisted layout references** (`*-ai-v1.png`, `*-v2.png`); final 128× outputs are procedurally composed via ImageMagick — authorship is user pipeline, source photo origin **needs verification** for publish/licensing.

---

## Project pack map

| Pack folder | Installed as | Role | User-created? |
|-------------|--------------|------|---------------|
| `geostrata-hd-overlay-128x/` | `GeoStrata-HD-Geology-Overlay-128x.zip` | GeoStrata rock/brick/cobble/polished + 8 vanilla ore overlays | **Yes** — `build-real-geology.sh` |
| `create-tfmg-geology-hd-128x/` | `Create-TFMG-Geology-HD-128x.zip` | Create stone palettes, TFMG blocks, Railcraft abyssal/quarried | **Yes** — `build-create-tfmg-geology.sh` |
| `modern-industry-materials-hd-128x/` | `Modern-Industry-Materials-HD-128x.zip` | Per-mod ore block textures | **Yes** — `build-industrial-materials.sh` |
| `unified-ores-hd-128x/` | `Unified-Ores-HD-128x.zip` + `Modern-Materials-HD-128x.zip` | Final consolidated ore + vanilla-rock LabPBR | **Yes** (derived pipeline) |
| `modern-natural-moss-128x/` | `Modern-Natural-Moss-128x.zip` | `moss_block` HD | **Yes** — `build-moss.sh` |
| `chipped-cobble-hd-128x/` | *(not in installed list — needs verification)* | Chipped mod cobbled_* variants | **Yes** — `build-chipped-cobbles.sh` |
| `geology-overhaul/…/realgeology/textures/` | *(mod JAR, not resource pack)* | Host-aware ore overlays + kimberlite | **Yes** — `generate-hosted-ore-textures.sh` |
| `ModernArch-R304-128x/` | Installed folder + zip | Main 128× base for most vanilla blocks | **No** — third-party |
| Fast Better Grass, Florescent, BetterFoliage overlays | Installed zips | Grass extension, flora 3D, foliage | **No** — third-party |

---

## Rocks — GeoStrata families (Real Geology stratigraphy)

Real Geology places these 27 GeoStrata block IDs. HD textures are in `geostrata-hd-overlay-128x/assets/geostrata/textures/block/`.  
Build script: `geostrata-hd-overlay-128x/build-real-geology.sh`.  
Each rock has colour + `_n` + `_s` at **128×128**.

| Rock | Raw | Bricks | Cobble (side/top/bottom) | Polished | Source photo | User-created | 16× vanilla slot |
|------|:---:|:------:|:------------------------:|:--------:|--------------|:------------:|------------------|
| amphibolite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `amphibolite-real.png` | yes | N/A (GeoStrata mod) |
| andesite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `andesite-real.png` | yes | N/A |
| basalt | ✅ | ✅ | ✅ / ✅ / ✅ (+ side variant) | ✅ | `basalt-real.png` | yes | N/A |
| basaltic_glass | ✅ | ✅ | ✅ / ✅ / ✅ (+ side variant) | ✅ | `basaltic_glass-real-v2.png` | yes | N/A |
| conglomerate | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `conglomerate-real.png` | yes | N/A |
| diabase | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `diabase-real.png` | yes | N/A |
| diorite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `diorite-real.png` | yes | N/A |
| dolomite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `dolomite-real.png` | yes | N/A |
| gabbro | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `gabbro-real.png` | yes | N/A |
| gneiss | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `gneiss-real.png` | yes | N/A |
| granite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `granite-real.png` | yes | N/A |
| hornfels | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `hornfels-real.png` | yes | N/A |
| limestone | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `limestone-real.png` | yes | N/A |
| marble | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `marble-real.png` | yes | N/A |
| novaculite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `novaculite-real.png` | yes | N/A |
| pegmatite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `pegmatite-real.png` | yes | N/A |
| peridotite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `peridotite-real-v2.png` | yes | N/A |
| phyllite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `phyllite-real.png` | yes | N/A |
| quartzite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `quartzite-real.png` | yes | N/A |
| rhyolite | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `rhyolite-real.png` | yes | N/A |
| rock_salt | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `rock_salt-real.png` | yes | N/A |
| schist | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `schist-real.png` | yes | N/A |
| scoria | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `scoria-real-v2.png` | yes | N/A |
| shale | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `shale-real.png` | yes | N/A |
| siltstone | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `siltstone-real.png` | yes | N/A |
| slate | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `slate-real.png` | yes | N/A |
| tuff | ✅ | ✅ | ✅ / ✅ / ✅ | ✅ | `tuff-real.png` | yes | N/A |

**Totals:** 27 rock types · 162 colour variant faces · 486 PNGs with PBR · **0 missing variants**

### Basalt family extras

Basalt and basaltic_glass have additional GeoStrata-specific faces beyond the standard six-variant set:

- `deepslate_basalt`, `deepslate_basaltic_glass`
- `cobbled_basalt_side`, `cobbled_basaltic_glass_side` (alongside top/bottom)
- All at 128×128, user-created, same source photos

### Cobble layout references (not block textures)

These source files guide cobble joint geometry; they are **not** placed in-game:

| File | Resolution | Notes |
|------|------------|-------|
| `basalt-cobbled-reference-ai-v1.png` | 1254×1254 | AI-assisted reference |
| `cobble-layout-igneous-ai-v1.png` | 1254×1254 | AI-assisted layout |
| `cobble-layout-metamorphic-ai-v1.png` | 1254×1254 | AI-assisted layout |
| `cobble-layout-sedimentary-ai-v1.png` | 1254×1254 | AI-assisted layout |
| `cobble-layout-volcanic-ai-v1.png` | 1254×1254 | AI-assisted layout |
| `interlocking-cobble-layout-reference.png` | 128×128 | Joint map |
| `interlocking-cobble-layout-reference-v2.png` | 128×128 | Joint map v2 |

### GeoStrata vanilla ore overlays (in HD pack, not Real Geology mod)

Eight host-rock ore overlays at 128×128 in `geostrata-hd-overlay-128x/` (built by `build-ore-overlays.sh`):

`coal`, `copper`, `diamond`, `emerald`, `gold`, `iron`, `lapis`, `redstone`

---

## Terrain and surface blocks

Vanilla `minecraft:` blocks used in overworld / Nether / End terrain. Resolution from `unified-ores-hd-128x/build/material-stage/` when present, else ModernArch install, else vanilla 16×.

| Block / texture | User-created | Source | Resolution | 16× slot in Real Geology mod |
|-----------------|:------------:|--------|:----------:|:----------------------------:|
| `grass_block_top` | derived | unified-ores → ModernArch base + LabPBR | 128×128 | N/A |
| `grass_block_side` | derived | unified-ores | 128×128 | N/A |
| `grass_block_side_overlay` | derived | unified-ores | 128×128 | N/A |
| `short_grass` | derived | unified-ores | 128×128 | N/A |
| `fern` | no | ModernArch (no unified override) | 16×16 | N/A |
| `snow` | derived | unified-ores | 128×128 | N/A |
| `ice` | derived | unified-ores | 128×128 | N/A |
| `packed_ice` | derived | unified-ores | 128×128 | N/A |
| `blue_ice` | derived | unified-ores | 128×128 | N/A |
| `water_still` | no | Shader water (Complementary); no block PNG override | — | N/A |
| `water_flow` | no | Shader water; no block PNG override | — | N/A |
| `sand` | derived | unified-ores | 128×128 | N/A |
| `red_sand` | derived | unified-ores | 128×128 | N/A |
| `dirt` | derived | unified-ores | 128×128 | N/A |
| `coarse_dirt` | derived | unified-ores | 128×128 | N/A |
| `podzol_top` / `podzol_side` | derived | unified-ores | 128×128 | N/A |
| `mycelium_top` / `mycelium_side` | derived | unified-ores | 128×128 | N/A |
| `mud` | derived | unified-ores | 128×128 | N/A |
| `clay` | derived | unified-ores | 128×128 | N/A |
| `gravel` | derived | unified-ores | 128×128 | N/A |
| `stone` | derived | unified-ores | 128×128 | N/A |
| `cobblestone` | derived | unified-ores | 128×128 | N/A |
| `mossy_cobblestone` | derived | unified-ores | 128×128 | N/A |
| `deepslate` | derived | unified-ores | 128×128 | N/A |
| `deepslate_top` | no | No project override found | 16×16 | N/A |
| `tuff` | derived | unified-ores | 128×128 | N/A |
| `calcite` | derived | unified-ores | 128×128 | N/A |
| `dripstone_block` | derived | unified-ores | 128×128 | N/A |
| `basalt_side` / `basalt_top` | derived | unified-ores | 128×128 | N/A |
| `smooth_basalt` | derived | unified-ores | 128×128 | N/A |
| `moss_block` | **yes** | `modern-natural-moss-128x` (`forest-moss.png` source) | 128×128 | N/A |
| `sandstone` / `sandstone_top` | derived | unified-ores | 128×128 | N/A |
| `red_sandstone` | derived | unified-ores | 128×128 | N/A |
| `obsidian` | derived | unified-ores | 128×128 | N/A |
| `netherrack` | derived | unified-ores | 128×128 | N/A |
| `end_stone` | derived | unified-ores | 128×128 | N/A |

### Terrain gaps (open work)

| Gap | Status | Notes |
|-----|--------|-------|
| `water_still` / `water_flow` | Intentional | Disabled custom water overlay; Complementary shader handles appearance |
| `deepslate_top` | Missing HD | Not in `unified-ores` material stage — may fall back to 16× under ModernArch |
| `fern` | Low-res | No unified-ores entry; Florescent 3D model may mask this |
| `chipped-cobble-hd-128x.zip` | Not in modpack `resourcepacks/` | Built locally (44 materials) but **needs verification** whether it should be installed |

---

## Real Geology mod textures (`realgeology:` namespace)

Path: `geology-overhaul/src/main/resources/assets/realgeology/textures/block/`  
All at **128×128** with `_n` / `_s` PBR maps. **User-created.**

### Kimberlite (owned block texture)

| Texture | Resolution | Source | 16× placeholder |
|---------|:----------:|--------|-----------------|
| `kimberlite` (+ `_n`, `_s`) | 128×128 | Derived from GeoStrata peridotite HD + colour grade (`generate-mineral-identity-assets.sh`) | N/A — full 128× shipped |

### Host-aware ore overlays (22 materials × 3 maps = 66 PNGs)

Built by `generate-hosted-ore-textures.sh` from macro mineral photos in `modern-industry-materials-hd-128x/sources/` and `create-tfmg-geology-hd-128x/sources/lignite.png`.

| Material | Macro source photo | User-created |
|----------|-------------------|:------------:|
| bauxite | `bauxite.png` | yes |
| coal | GeoStrata ore pipeline | yes |
| copper | GeoStrata ore pipeline | yes |
| diamond | GeoStrata ore pipeline | yes |
| emerald | GeoStrata ore pipeline | yes |
| fluorite | `fluorite.png` | yes |
| galena | `galena-lead-v2.png` | yes |
| gold | GeoStrata ore pipeline | yes |
| iron | GeoStrata ore pipeline | yes |
| lapis | GeoStrata ore pipeline | yes |
| lead | `galena-lead-v2.png` | yes |
| lignite | `lignite.png` (TFMG sources) | yes |
| lithium | `spodumene-lithium.png` | yes |
| nickel | `pentlandite-nickel.png` | yes |
| osmium | `iridosmine-osmium.png` | yes |
| redstone | GeoStrata ore pipeline | yes |
| saltpeter | `spodumene-lithium.png` | yes |
| silver | `galena-lead-v2.png` | yes |
| sulfur | `native-sulfur.png` | yes |
| tin | `cassiterite-tin.png` | yes |
| uranium | `uraninite-uranium-v2.png` | yes |
| zinc | `sphalerite-zinc.png` | yes |

**16× vanilla slot status:** Real Geology does not use 16× placeholder slots for these — models reference 128× overlays directly. GeoStrata provides the host rock face at runtime.

### Future owned rock blocks (ROADMAP — not yet textured)

Per `ROADMAP.md`, planned Real Geology-owned blocks still need original 128× art:

- Sandstone/arkose, mudstone/claystone, breccia, gypsum, rock salt (evaporite/fluvial formations)
- Migration of existing `geostrata:*` references to `realgeology:*`

**Status:** No placeholder PNGs exist yet — **open / missing**.

---

## Mod-specific geology textures

### Create stone palettes (`create-tfmg-geology-hd-128x`)

14 fictional Create stone types, each with full masonry palette (natural, cut, polished, brick, slab, pillar, cap, layered, alternatives). All **128×128**, user-created.

| Palette name | Real-material source (documented in build script) |
|--------------|---------------------------------------------------|
| andesite | `andesite-real.png` |
| calcite | `marble-real.png` |
| deepslate | `slate-real.png` |
| diorite | `diorite-real.png` |
| dripstone | `limestone-real.png` |
| granite | `granite-real.png` |
| limestone | `limestone-real.png` |
| tuff | `tuff-real.png` |
| scoria | `scoria-real-v2.png` |
| scorchia | `scorchia-black-vesicular-basalt.png` |
| asurine | `asurine-sodalite-syenite-v2.png` |
| crimsite | `crimsite-ferruginous-rhyolite-v2.png` |
| ochrum | `ochrum-ferruginous-sandstone-v2.png` |
| veridium | `veridium-serpentinite-v2.png` |

### TFMG blocks + stone palettes

| Block / palette | Resolution | Source | User-created |
|-----------------|:----------:|--------|:------------:|
| `lignite` | 128×128 | `lignite.png` | yes |
| `fossilstone` | 128×128 | `limestone-real.png` crop | yes |
| `fireclay` | 128×128 | `ochrum-ferruginous-sandstone-v2.png` | yes |
| `limesand` | 128×128 | `limestone-real.png` crop | yes |
| `bauxite` palette (full masonry set) | 128×128 | `bauxite.png` | yes |
| `galena` palette (full masonry set) | 128×128 | `galena-lead-v2.png` | yes |

### Railcraft abyssal / quarried stone (`create-tfmg-geology-hd-128x`)

14 colour textures at 128×128. Fictional names mapped to real materials:

| Railcraft block | Rendered as | User-created |
|-----------------|-------------|:------------:|
| abyssal_stone (+ cobble, bricks, paver, polished, chiseled, etched) | dark hornfels / glassy basalt | yes |
| quarried_stone (+ cobble, bricks, paver, polished, chiseled, etched) | granite | yes |

### Chipped cobble variants (`chipped-cobble-hd-128x`)

44 `cobbled_<material>` textures at 128×128 (+ PBR). User-created dry-stone-wall assemblies.

| Material group | Examples |
|----------------|----------|
| Igneous / metamorphic | andesite, basalt, diorite, granite, tuff, calcite, dripstone_block |
| Sedimentary / surface | dirt, clay, sandstone, red_sandstone, cobblestone, stone, smooth_stone |
| Nether / End | netherrack, nether_bricks, obsidian, end_stone, ancient_debris, blackstone |
| Ice / snow | ice, packed_ice, blue_ice, snow_block (from calcite base) |
| Decorative / ore blocks | amethyst_block, lapis_block, quartz_block, prismarine, purpur_block, raw_*_block |

### Industrial ore block textures (`modern-industry-materials-hd-128x`)

48 colour textures at 128×128, consolidated into `unified-ores-hd-128x`. See `unified-ores-hd-128x/build/material-stage/ORE-CATALOG.md` for the full material list across Create, Mekanism, Immersive Engineering, Railcraft, and TFMG.

---

## Third-party textures (not user-created)

| Source | What it provides | Resolution |
|--------|------------------|:----------:|
| **ModernArch R 128×** | Base HD for most vanilla blocks, items, entities | 128×128 (most blocks) |
| **Fast Better Grass** | Grass/snow/podzol side extension | Uses ModernArch grass sprites |
| **Florescent 1.3** | 3D flower/crop/mushroom models | Model geometry + ModernArch sprites |
| **ModernArch BetterFoliage 128×** | 3D grass blade sprite mapping | 128×128 |
| **GeoStrata mod (JAR)** | Default 16× rock textures when HD overlay absent | 16×16 |
| **Complementary Reimagined** | Water appearance via shaders | N/A (shader) |

---

## Build scripts reference

| Script | Output |
|--------|--------|
| `geostrata-hd-overlay-128x/build-real-geology.sh` | 27 rock families + variants |
| `geostrata-hd-overlay-128x/build-ore-overlays.sh` | 8 vanilla ore overlays for GeoStrata |
| `geostrata-hd-overlay-128x/build-material-maps.sh` | PBR `_n` / `_s` for GeoStrata rocks |
| `create-tfmg-geology-hd-128x/build-create-tfmg-geology.sh` | Create + TFMG + Railcraft geology |
| `modern-industry-materials-hd-128x/build-industrial-materials.sh` | Per-mod ore blocks |
| `unified-ores-hd-128x/build-unified-ores.sh` | Final ore consolidation |
| `unified-ores-hd-128x/build-vanilla-rock-materials.sh` | Vanilla rock LabPBR |
| `unified-ores-hd-128x/build-vanilla-ores.sh` | Vanilla ore replacement |
| `modern-natural-moss-128x/build-moss.sh` | Moss block HD |
| `chipped-cobble-hd-128x/build-chipped-cobbles.sh` | Chipped cobble variants |
| `geology-overhaul/generate-hosted-ore-textures.sh` | Real Geology mod ore overlays |
| `geology-overhaul/generate-mineral-identity-assets.sh` | Kimberlite + ore model wiring |

---

## Counts summary

| Metric | Count |
|--------|------:|
| GeoStrata rock types with full HD variant set | 27 |
| GeoStrata colour block textures (incl. extras) | 224 |
| GeoStrata PNGs incl. PBR | 672 |
| Real Geology mod textures (ore + kimberlite, incl. PBR) | 69 |
| Create/TFMG/Railcraft geology colour textures | ~294 |
| Chipped cobble materials | 44 |
| Industrial ore block colour textures | 48 |
| Terrain blocks with user-original art | 1 (`moss_block`) |
| Terrain blocks with derived HD (unified-ores pipeline) | 35 |
| Terrain / surface gaps | 4 |
| Future owned rock blocks (no art yet) | ~5 types (ROADMAP) |
| Textures from third-party packs (ModernArch, etc.) | Majority of non-geology vanilla blocks |
| User-created 128× textures (project source, excl. PBR duplicates) | **~700+** colour faces |

---

## Regenerating this inventory

```bash
# From minecraft-givekit-project root — re-scan PNG dimensions
find geostrata-hd-overlay-128x/assets modern-industry-materials-hd-128x/assets \
  create-tfmg-geology-hd-128x/assets geology-overhaul/src/main/resources/assets/realgeology/textures \
  -name '*.png' | while read f; do identify -format '%wx%h %f\n' "$f"; done | sort | uniq -c

# Audit geology coverage against installed pack (requires Modern-Materials-HD-128x.zip)
bash unified-ores-hd-128x/audit-rock-textures.sh
```

---

*This document reflects the repository state as of 2026-08-29. Re-run dimension scans after rebuilding any `build-*.sh` pipeline.*
