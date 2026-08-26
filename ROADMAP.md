# Real Geology roadmap

Real Geology replaces Minecraft's generic underground with seed-stable geological provinces: folded strata, faults, intrusions, and deposit shapes that read like real field geology rather than a repeating stone palette.

This document is the public backlog. Items are ordered roughly by dependency and impact.

## Shipped in 0.21 beta (folding focus)

- Seed-stable provinces tied to terrain/climate (basin, margin, collision belt, pluton, volcanic arc)
- Finite stratigraphic columns with varying bed thickness
- Collision-belt fold families: anticline/syncline, asymmetric fold-and-thrust, monocline
- Post-depositional and syn-depositional faults, angular unconformities, cross-cutting dikes
- Debug section/ore raster modes and `/realgeology debug` commands
- **Vanilla ore spawning** for beta testers (custom deposits deferred)

## Next — custom ore deposits

Goal: re-enable Real Geology's deposit-specific shapes with host-aware ore blocks and mineral-accurate names.

- Porphyry Cu-Au stockworks, MVT Pb-Zn districts, kimberlite pipes, banded iron, coal growth faults, and related deposit logic (code is present but disabled in 0.21.0-beta.2)
- Host-aware ores with `c:ores/*` compatibility
- Re-enable the optional competing-ore removal modifier once custom placement is stable

## After deposits — karst and dissolution caves

Goal: caves preferentially follow soft sediment between harder rock, as if acidic groundwater dissolved limestone, dolomite, and shale rather than punching random holes through everything.

- Couple cave carving to lithology hardness and solubility
- Prefer passages along bedding planes and unconformities
- Keep vanilla/GeoTectonic-style large caves separate from this dissolution network where possible
- Document expected interaction with Terralith terrain and any future GeoTectonic install

## Medium term — owned rock blocks

Today Real Geology **places GeoStrata rock blocks** by registry ID. GeoStrata remains a required dependency for beta releases.

Later phases add Real Geology-owned blocks so the mod can stand alone:

1. Sandstone/arkose, mudstone/claystone, breccia, gypsum, rock salt (unlock fluvial and evaporite formations)
2. Migrate stratigraphy references from `geostrata:*` to `realgeology:*` one formation at a time
3. Own HD textures only where we control the block (ore overlays and kimberlite already live here)

Do **not** copy GeoStrata textures into this repository. Either keep the dependency or ship original art.

## Surface and hydrology (after caves)

See also `../docs/real-geology-surface-hydrology-backlog.md` in the parent project notes.

- Evaporite basins and salt lakes
- High-mountain glaciers and meltwater (separate from karst)
- Quicksand and peat swamps with proper movement rules

## Long term — Dr Stone direction

A technology tree grounded in realistic resource extraction and processing is possible, but it is a **separate effort** from world generation:

- Requires stable ore/host rock identity (largely done)
- Needs machines/recipes either in this mod or documented integration with an industrial modpack
- Best pursued once worldgen and cave work have contributors and test coverage

## Explicit non-goals for beta

- Full plate-tectonics simulation at real-world scale
- Rewriting Terralith or GeoStrata worldgen — Real Geology replaces underground placement only
- Supporting existing worlds (new chunks only; always test in a fresh world)
