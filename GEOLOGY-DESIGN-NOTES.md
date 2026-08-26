# Real Geology design notebook

This is a working record for the Minecraft geology overhaul. It describes the
actual pack, not a generic mod-list recommendation.

## Active terrain stack

The live NeoForge 1.21.1 profile is:
`~/.minecraft/versions/Modern Industry & Colonies`.

| Layer | Active component | Responsibility |
|---|---|---|
| Climate and continent-shaped terrain | Terralith 2.6.2 | Vanilla multi-noise parameters: temperature, humidity, continentalness, erosion, weirdness and depth; adds/reworks biomes and terrain. |
| Surface / vegetation detail | Geophilic 3.6 | Biome overlays and biome-specific plants, rocks and surface patches. |
| Compatibility | Terraphilic 1.2 | Makes Terralith and Geophilic work together. |
| Cities | Lost Cities | Places structures later; does not define climate or continental terrain. |
| Rock library | GeoStrata 1.2 | Supplies rock blocks and ore-host variants. |
| Geological replacement | Real Geology | Our custom feature: replaces new underground terrain with coherent rocks and deposits. |

Terralith is configured with `vanilla_stone_gen: false`, so vanilla
granite/diorite/andesite generation is deliberately off.

## Real Geology ownership rule

Only Real Geology should control ordinary Overworld rocks and ore deposits.
Competing GeoStrata, vanilla, Create, Mekanism, Railcraft, Immersive
Engineering and TFMG ore/stone features are removed where they duplicate this
system. Mods remain installed for machines, recipes, processing, decorative
blocks and special world features such as TFMG oil.

Never add a second broad geology/world-generation mod merely for a few rock
types: it would recreate duplicate deposits, inconsistent host rocks and the
low-resolution texture issues already cleaned up.

## Current generation model (0.20 isostatic plate-root experiment)

- Seed-stable plate-sized provinces: sedimentary basin, mountain belt,
  plutonic province and volcanic province.
- Finite stratigraphic columns with varying bed thicknesses.
- Strong, kilometre-scale folds are restricted to collision belts; basins retain gentle drape and fault-block offsets.
- Thin 2–4 m event beds/lenses occur inside suitable sedimentary formations, follow the same folded bedding, and pinch laterally.
- Basin faults choose either post-depositional rigid displacement or syn-depositional growth behaviour. In growth settings, progressively younger beds and coal seams thicken or thin across the fault, and rare palaeochannels remove short coal-seam sections.
- Iron has three distinct settings: banded iron formation in old metamorphic strata, magnetite-rich volcanic/hydrothermal fracture bodies, and existing intrusive/alteration-associated occurrences.
- Collision belts now use a finite, parametric fold ribbon instead of a repeating height-field oscillator. Each bed is positioned as a normal offset from the same folded centreline, so the transform can produce open, chevron, isoclinal, and recumbent geometry: close parallel limbs, locally overturned limbs, and low-angle lying-over folds rather than only a simple S-wave. A later steeply dipping fault cuts and offsets the folded sequence, while young cover still bypasses the transform above an unconformity.
- The folded succession now continues through mountain elevations instead of ending near ordinary ground level and leaving an indefinitely thick single youngest rock above it. Forced collision-belt testing always uses that folded succession, and fold packets cover most of the belt.
- Mountain faults are now paired dipping planes bounding broad dropped or uplifted blocks (graben/horst style), rather than repeated thin alternating offsets. The planes remain thin, but the displaced sector between them is large enough to read as tectonic structure.
- Section cutaways preserve the magma lower-crust boundary at the bottom of the trench, making the optional variable tectonic base observable.
- The bottom band is now an **asthenosphere proxy**, not a plate base. It inversely tracks lithosphere thickness: collision roots and old continental interiors expose little hot material, while young oceanic crust and rifts expose more. Its top follows tectonic setting plus a continuous long-wavelength field instead of random plate-sized steps.
- The isoclinal/recumbent preview displacement is now constrained so its fold centreline cannot cross itself. The previous self-intersection made the nearest-point solver switch between separate limbs, producing tangled bands. This revision favours stable steep folds; fully overturned recumbent nappes are deferred until a true 2D layer-rasterizer can represent overlapping limbs without ambiguity.
- Default mountain faulting has been simplified to sparse, modest-throw dipping cuts. The earlier paired fault blocks are removed because they read as artificial towers/walls in a section; a separately tuned graben system can be added later.
- The forced collision-belt inspection mode suppresses unrelated dikes, batholiths and contact halos so intrusive columns cannot be mistaken for part of the folded sediment package.
- An optional disabled-by-default variable tectonic base makes the solid lower-crust boundary range from 18–50 blocks according to the seeded plate field. It remains solid magma with rare sealed lava pockets, rather than flooding caves with a lava ocean.
- In forced collision-belt test mode only, an ocean vanilla spawn is replaced before a player joins by the nearest procedurally evaluated dry-land location. It reads terrain height directly from the generator and does not pre-generate a large search area.
- Sedimentary unconformity: older deformed beds, erosion, basal conglomerate,
  younger relatively flat cover.
- Mountain pressure/depth metamorphic progression: slate → phyllite → schist
  → gneiss, with quartzite, marble and amphibolite where appropriate.
- Discrete irregular granite/diorite intrusions, contact aureoles, pegmatites
  and cross-cutting diabase dikes.
- Terralith climate/terrain tags choose marine, alluvial/wet, or dry-hot
  sedimentary facies and influence bedrock exposure.
- The terrain setting now controls geological history: deep ocean creates a
  thick marine basin, coast creates a passive-margin wedge, wet lowland creates
  a continental basin, mountain/highland/cliff terrain creates a collision
  belt, and volcanic terrain creates a volcanic arc. The old seeded plate map
  provides fold/fault orientation and hidden crustal variation but cannot
  invent a visible mountain belt below a normal plain.
- Collision belts select coherent anticline/syncline, asymmetric fold-and-
  thrust, or monocline families. Their fold axes plunge gradually, producing
  curved map-view bands when erosion cuts them. Sedimentary margins and basins
  remain comparatively flat, sagging and thickening/pinching laterally.
- Eroded old layers can be overlain by younger cover in basins, passive
  margins, ocean basins and occasional quiet mountain-valley pockets. This
  creates genuine angular-unconformity behaviour: deformed rock below, a time
  break, then flatter young beds above.
- The lower crust ends in a configurable thermal base: up to four magma-block
  layers immediately above the random bedrock floor, with rare lava source
  pockets only where solid bedrock seals their underside. This is safe by
  default and does not create a continuous lava ocean.
- Deposits use distinct geological shapes: wet/riparian coal-lignite seams;
  shallow hot-wet bauxite blankets; dry-basin saltpeter; hematite/magnetite
  iron formations; intrusive porphyry Cu-Au stockworks; carbonate-hosted MVT
  Pb-Zn districts; clastic Pb-Zn-Ag beds; narrow polymetallic fissures;
  pegmatite tin/lithium/uranium; mafic cumulate nickel lenses; carbonate
  fluorite; volcanic sulphur; lazurite in metamorphosed carbonate; beryl in
  rare pegmatite/contact settings; and diamond-bearing kimberlite pipes.
- Canonical blocks retain industry-compatible `c:ores/<material>` IDs while
  their in-game names use actual mineral forms. Redstone stays explicitly
  fictional; ruby/sapphire are deferred until the pack has real recipes.

The staged package is `dist/Real-Geology-1.21.1-Thermal-Base-Test-0.11.0.zip`.
It must be tested in a **brand-new world**; generation never rewrite existing
chunks.

## Next design pass: couple geology to the real terrain/climate

Real Geology currently determines provinces independently. It should query
the generated biome and use Terralith's existing tags to choose sediment and
surface exposure without replacing the terrain generator.

Useful Terralith biome tags include `is_dry`, `is_wet`, `is_hot`, `is_cold`,
`is_mountain`, `is_hill`, `is_plateau`, `is_aquatic`, `is_river`, `is_beach`
and `is_badlands`.

Desired results:

- Oceans/coasts: marine shale, limestone, sandstone and occasional chert.
- Rivers/low basins: gravel, claystone/mudstone, coal swamps and alluvium.
- Arid/hot interiors: red beds, gypsum, halite/rock salt and bauxite/laterite.
- Cold/wet mountains: erosion exposes folded metamorphic basement.
- Volcanic Terralith biomes: basalt/andesite/dacite, tuff, ash and intrusions.
- Stable lowlands: broad, mostly flat sedimentary cover.

## Candidate new blocks — only when they unlock a formation

First priority: sandstone/arkose, mudstone or claystone, breccia, chert,
andesite, dacite, gypsum, rock salt and kimberlite.

Later: serpentinite, travertine, laterite, migmatite and anorthosite.

Any new block must be a controlled Real Geology block with an HD texture,
compatible tags/recipes, and no independent worldgen.

## Implementation order

1. Add controlled sandstone/arkose, mudstone, breccia, gypsum and more complete evaporite blocks.
2. Use sandstone + mudstone to make real fluvial uranium roll-fronts; refine the new kimberlite pipe texture and add its alteration halo.
3. Replace simple vertical boundaries with dipping/folded formation surfaces.
4. Add angular unconformities, pinch-outs, basin fill and river deposits.
5. Add rift and volcanic-arc sequences, then sills and more precise fault-hosted veins.
6. Test fresh worlds across several seeds before installing into the shared live pack.

## Deposit-model references

- USGS describes porphyry Cu deposits as large altered intrusions with dense
  stockwork veinlets, commonly open-pit mined.
- USGS describes MVT Pb-Zn as shallow carbonate-hosted (especially dolostone)
  stratabound/replacement deposits, unrelated to intrusions.
- USGS describes lithium-cesium-tantalum pegmatites as thin dikes with
  internal zoning near evolved granites in collisional belts.
- USGS describes sandstone uranium roll fronts as curved, bedding-confined
  redox deposits in fluvial/coastal sandstone; this needs sandstone/mudstone
  blocks before implementation.

## Debugging without WorldEdit lag

Worldgen debug uses `config/realgeology-common.toml` and must be chosen before
creating a disposable test world:

- `worldgen_mode = "section"`: a full-height, 50 m-wide X/Z raster of air
  trenches every 512 m; it exposes new terrain from both directions as chunks generate.
- `worldgen_mode = "ores"`: the same raster with every non-ore terrain
  block and fluid removed, leaving generated ore shapes visible.
- `worldgen_mode = "off"`: normal generation.

`force_collision_belt = true` is a disposable-test override that exposes the new structural transform everywhere. It must remain `false` in normal worlds.

Worldgen debug blocks are part of saved chunks and cannot safely self-delete.
Delete the dedicated debug world instead. The normal-world commands
`/realgeology debug section`, `/realgeology debug ores`, and
`/realgeology debug clear` are temporary; they change only 384 blocks per tick
and restore their blocks on Save & Quit.
