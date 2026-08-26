# Beta playtest feedback — plate / province generation (Aug 2026)

Structured record of in-game observations from Real Geology 0.20+ plate/province
generation. **Documentation only** — no fixes implemented in this pass.

**Code cross-reference:** `GeologicalProvincesFeature` — province selection
(`province()`, `adaptProvince()`), terrain classification (`environment()`,
`TerrainSetting`), column choice (`geologicalColumn()`), structural transforms
(`structuralTransform()`, `sedimentaryStructure()`), and thin event beds
(`thinInterbed()`).

See also [GEOLOGY-DESIGN-NOTES.md](../GEOLOGY-DESIGN-NOTES.md) for the intended
generation model and [ROADMAP.md](../ROADMAP.md) for planned work.

---

## 1. Extreme cuts at biome transitions

| Field | Detail |
|---|---|
| **Symptom** | Brutal discontinuities when biomes change; example: switching from far-inland mid to far-inland high reads as a brute-force cut, not a gradual geological transition. |
| **Expected behavior** | Geologically sensible gradation across terrain/climate boundaries — province type, column facies, fold amplitude, and bed thickness should blend over hundreds of metres, not flip at the biome edge. |
| **Suspected cause** | `adaptProvince()` maps `TerrainSetting` to `Province` with a hard switch (no blending). `environment()` assigns `TerrainSetting` from discrete biome tags and thresholds (`mountain && (relief >= 2 \|\| elevation > 38)` → `COLLISION_BELT`; else often `STABLE_INTERIOR`). Crossing a Terralith biome can instantly change province (e.g. `SEDIMENTARY`/`PLUTONIC` → `MOUNTAIN`), column (`SEDIMENTARY_COLUMN` vs `MOUNTAIN_COLUMN` / `FOLDED_SEDIMENTARY_COLUMN`), and transform (`StructuralTransform.rigid()` vs `FoldRibbon`). Per-column evaluation at block resolution amplifies the seam. |
| **Priority** | **P0** — most visible regression; undermines the “coherent plate” goal. |

---

## 2. High coast — no folds, thick horizontal layers

| Field | Detail |
|---|---|
| **Symptom** | High coast terrain shows no folds; only very thick, straight horizontal layers. As a high / mountain terrain type, folding would be expected. |
| **Expected behavior** | Elevated coastal or mountain-margin settings should show compressional structure (anticlines/synclines or at least strong monoclinal dip) consistent with collision or active margin tectonics. |
| **Suspected cause** | Coast/beach biomes map to `TerrainSetting.PASSIVE_MARGIN` (`aquatic \|\| beach`), which forces `Province.SEDIMENTARY` and `StructuralTransform.rigid()` — folds are explicitly restricted to `Province.MOUNTAIN` (`structuralTransform()` line ~1131). `PASSIVE_MARGIN` uses `formationThicknessScale` **1.20** (thicker beds) and `sedimentaryStructure()` with only gentle drape (`broadFold * 2.3`). Unless relief/elevation also triggers `COLLISION_BELT`, high coast never receives `FoldRibbon`. |
| **Priority** | **P1** — common exploration terrain; mismatch between player terrain mental model and passive-margin geology. |

---

## 3. Mid coast — thick layers, no pancake mixing

| Field | Detail |
|---|---|
| **Symptom** | Mid coast similar to high coast: very thick layers, no “pancake mixing” (interbedding / thin event beds). |
| **Expected behavior** | Coastal successions should show interbedded shale/siltstone/limestone event beds within thicker formations — readable “pancake stack” in section views. |
| **Suspected cause** | `thinInterbed()` only runs for `Province.SEDIMENTARY` or `foldedSedimentary` mountain belts, and requires `bed.thickness() >= 10` plus a narrow lateral noise gate (`lateralPresence < -.16`). `PASSIVE_MARGIN_COLUMN` units are already thick (e.g. shale 24–56 after scale); interbeds may be rare or visually lost. Passive-margin wedge scaling (`localScale` in `sampleFromBase`) thins edge units but does not add internal event beds. |
| **Priority** | **P1** — affects readability of sedimentary facies at margins. |

---

## 4. Softer rock folding — insufficient relative to hard beds

| Field | Detail |
|---|---|
| **Symptom** | General sense that softer sediments do not fold enough relative to harder beds; competent units appear to dominate while shales/siltstones look under-deformed. |
| **Expected behavior** | Competence contrast: softer horizons should thicken in hinge zones, thin on limbs, and show more ductile curvature; detachment along shale should be visible. |
| **Suspected cause** | `FoldRibbon` applies a single structural transform to the whole column — no per-lithology competence or differential thickening during folding. `preferredDetachmentHorizon()` selects a weak horizon for thrusts but does not modulate fold shape by rock type. Metamorphic progression (`regionalMetamorphicRock()`) only applies when `!foldedSedimentary`. |
| **Priority** | **P2** — polish / realism; depends on fold system being visible first (issues 1–2). |

---

## 5. High far inland — best case, but folds still subtle globally

| Field | Detail |
|---|---|
| **Symptom** | High far inland looks best (good pancake / interbedding) but folding remains subtle from typical viewing angles; overall stacking and fold amplitude may be too weak globally. |
| **Expected behavior** | Inland highlands / collision settings should show clearly readable folds in natural cliff and trench views without debug section mode; bed stack + fold amplitude should “pop” at human scale. |
| **Suspected cause** | When `COLLISION_BELT` applies, only ~72% of columns use `FOLDED_SEDIMENTARY_COLUMN` (`hash > .28` gate); remainder use metamorphic `MOUNTAIN_COLUMN` with no sediment interbeds. Fold amplitude is capped (~34–50 blocks vertical in `FoldRibbon.create`, further scaled by family e.g. chevron `* .42`). `formationThicknessScale(COLLISION_BELT)` is **0.82** (thinner beds help readability but reduce contrast). `mountainRootUplift()` couples broad elevation but is intentionally not 1:1 with surface. Viewing angle / Minecraft vertical exaggeration may hide gentle dips. |
| **Priority** | **P1** — tune amplitude and inspection defaults; validate against non-debug worlds. |

---

## 6. Open question — terrain tags vs generation coupling

| Field | Detail |
|---|---|
| **Symptom** | Unclear whether player-facing terrain categories (high / mid / far inland, coast, etc.) actually drive generation as intended. |
| **Expected behavior** | Terralith (or pack) terrain classification should deterministically and observably control `TerrainSetting`, province override, column facies, fold eligibility, and thickness scales — with logging or debug overlay to verify. |
| **Suspected cause** | `environment()` does **not** read Terralith inland/coast distance tags directly; it infers `TerrainSetting` from biome tags (`is_mountain`, `is_hill`, `is_plateau`, `terralith:highlands`, `terralith:cliffs`, `is_beach`, `is_aquatic`, elevation, relief). “Far inland mid” vs “far inland high” may differ only by elevation threshold (`> 38`) and mountain tags — not a dedicated inland gradient. `province(seed,x,z)` plate map (~2.4 km cells) still supplies orientation and margin style; `adaptProvince()` can override it per column. No playtest telemetry exists to confirm mapping. |
| **Priority** | **P0 (investigation)** — add verification pass before further tuning: sample coordinates per Terralith terrain class, log `(biome, elevation, relief, TerrainSetting, Province, foldedSedimentary, column)`. |

---

## Suggested next-iteration order

1. **Verify coupling** (issue 6) — instrument or script biome → `Environment` → column mapping.
2. **Blend across `TerrainSetting` boundaries** (issue 1) — cross-fade province/column/transform over a transition band.
3. **Revisit coast / high-elevation rules** (issues 2–3) — decide when passive margin should grade into collision-style folding.
4. **Global fold / interbed tuning** (issues 4–5) — amplitude, `foldedSedimentary` ratio, interbed thresholds after boundaries are smooth.

---

## 7. Fault / crack offset layer doubling (Aug 2026 playtest)

| Field | Detail |
|---|---|
| **Symptom** | When the generator offsets terrain to display a crack or fault, it appears to **insert new geological layers** rather than deform existing beds. When a section is pushed up, layers appear added **below**; when pushed down, layers appear added **above**. The pushing block seems to **duplicate the layer above or below the fault** instead of displacing the same stratigraphy. |
| **Expected behavior** | Proper **deformation** — the same beds bent or displaced across the fault plane — not partial **doubling** of stratigraphy. |
| **Suspected cause** | Crack/fault offset logic in `GeologicalProvincesFeature` (`structuralTransform()`, `sedimentaryFault()`, fold/fault sampling) may sample source coordinates on the wrong side of the discontinuity, effectively re-inserting adjacent beds at the fault instead of applying true displacement. |
| **Priority** | **P1** — visible in debug cutaways and section views; undermines structural realism. |

---

## Related code anchors

```408:418:src/main/java/com/azumoo/realgeology/worldgen/GeologicalProvincesFeature.java
    /** Let terrain/climate constrain the tectonic field, never replace it. */
    private static Province adaptProvince(Province tectonic, Environment environment) {
        // The terrain generator owns visible landform. The seeded plate field
        // supplies hidden orientation and history, but may no longer invent a
        // mountain belt under an ordinary plain or a deep marine basin.
        return switch (environment.setting()) {
            case OCEAN_BASIN, PASSIVE_MARGIN, CONTINENTAL_BASIN -> Province.SEDIMENTARY;
            case COLLISION_BELT -> Province.MOUNTAIN;
            case VOLCANIC_ARC -> Province.VOLCANIC;
            case STABLE_INTERIOR -> tectonic == Province.MOUNTAIN ? Province.PLUTONIC : tectonic;
        };
    }
```

```391:400:src/main/java/com/azumoo/realgeology/worldgen/GeologicalProvincesFeature.java
        TerrainSetting setting;
        // Continentalness makes the broad land/ocean framework, but terrain
        // tags and relief decide whether that continental crust is a quiet
        // basin, an eroded collision belt, or a volcanic region.
        if (aquatic && deepOcean) setting = TerrainSetting.OCEAN_BASIN;
        else if (aquatic || beach) setting = TerrainSetting.PASSIVE_MARGIN;
        else if (volcanic) setting = TerrainSetting.VOLCANIC_ARC;
        else if (mountain && (relief >= 2 || elevation > 38)) setting = TerrainSetting.COLLISION_BELT;
        else if (river || biome.is(WET) && elevation < 26) setting = TerrainSetting.CONTINENTAL_BASIN;
        else setting = TerrainSetting.STABLE_INTERIOR;
```

```1130:1133:src/main/java/com/azumoo/realgeology/worldgen/GeologicalProvincesFeature.java
    private static StructuralTransform structuralTransform(long seed, Province province, Environment environment, int x, int z) {
        if (province != Province.MOUNTAIN) {
            return StructuralTransform.rigid(structuralOffset(seed, province, environment, x, z));
        }
```
