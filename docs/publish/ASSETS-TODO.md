# Real Geology — asset todo for first publish

Check items off as you go. Save finished files to `docs/publish/` (create the folder).

**Priority:** 🔴 required for launch · 🟡 strongly recommended · 🟢 nice to have

---

## 🔴 1. Mod icon (Modrinth + in-game)

**What:** Square logo shown on Modrinth search and in the mod list.

| Spec | Value |
|------|--------|
| Size | **256×256 px** minimum (512×512 is fine) |
| Format | PNG, transparent or solid background |
| Filename | `docs/publish/icon-256.png` |

**Ideas (pick one direction):**
- Stylised **folded strata** cross-section (3–4 coloured bands, simple curves)
- Layer cake of rock colours (shale grey, limestone cream, basalt dark, gneiss banded)
- Cross-section of an **anticline** with a pickaxe silhouette
- Letter **RG** on a rock-texture background (quick placeholder — replace later)

**Avoid:** copying GeoStrata textures; use flat/stylised colours only.

**After you make it:** tell the dev to wire `logoFile` in `neoforge.mods.toml`.

---

## 🔴 2. Modrinth gallery — 3 images minimum

Modrinth gallery = what sells the mod. Upload as PNG or JPG, **1280×720 or 1920×1080** preferred.

### Image A — Folded mountain belt (hero shot)

**Easiest path — you may already have this:**

Open one of these existing exports (pick the clearest folds):

```
build/cross-sections/terrain-geology-mountain--4915854425581810115-exaggerated-16x.png
build/cross-sections/terrain-geology-mountain--4915854425581810115-stable-folds-thrusts.png
build/cross-sections/imbricate-thrust-inspection.png
```

- Crop to the interesting centre (remove empty margins)
- Resize to ~1920×1080
- Save as `docs/publish/gallery-01-folded-mountain.png`
- **Caption for Modrinth:** *Collision belt — folded sedimentary strata and thrust faults (debug section, 16× vertical exaggeration)*

**Alternative — in-game screenshot:**
1. New test world, `config/realgeology-common.toml`:
   ```toml
   [debug]
   worldgen_mode = "section"
   force_collision_belt = true
   ```
2. Fly into an air trench at Y ~64, look at the exposed wall
3. F1 hide HUD, F2 screenshot

---

### Image B — Basin / lowland strata

**Existing file to start from:**

```
build/cross-sections/terrain-geology-lowland--4915854425581810115-exaggerated-16x.svg
```
(or export fresh: `./gradlew runGeologyPreview`)

Save as `docs/publish/gallery-02-basin-strata.png`

**Caption:** *Continental basin — quiet sedimentary cover with varying bed thickness*

---

### Image C — In-game surface + underground feel

**Must be in-game** (shows it’s Minecraft, not just a diagram).

1. Normal world (`worldgen_mode = "off"`, `force_collision_belt = false`)
2. Terralith mountain biome if available
3. Find a cliff or dig a small strip mine exposing **banded rock layers**
4. Include grass/sky in frame so it reads as “real world”
5. Save as `docs/publish/gallery-03-ingame-cliff.png`

**Caption:** *Exploring a mountain — erosion exposes folded bedrock*

---

## 🟡 3. Ore / deposit showcase (optional 4th gallery image)

Shows the mod isn’t only pretty layers — ores matter for modpack players.

**Option A — debug ores mode:**
```toml
worldgen_mode = "ores"
force_collision_belt = false
```
Floating ore shapes in air trenches look dramatic.

**Option B — strip mine into:**
- Kimberlite pipe (rare, deep)
- Banded iron formation in mountain belt
- Porphyry copper stockwork (granite with veinlets)

Save as `docs/publish/gallery-04-deposits.png`

---

## 🟡 4. GitHub README banner (optional but professional)

Wide image at top of README on GitHub.

| Spec | Value |
|------|--------|
| Size | **1280×640 px** (2:1) or **1200×630** |
| Format | PNG |
| Filename | `docs/publish/github-banner.png` |

Reuse the fold cross-section art from Image A, add text:

**Real Geology** · Folded strata for NeoForge 1.21.1

(Can be done in GIMP, Photopea, or Canva.)

---

## 🟡 5. Before/after comparison (great for Reddit / Discord)

Two-panel image: **vanilla stone** vs **Real Geology strata**.

1. Vanilla 1.21 world strip mine at same seed area (or creative superflat comparison)
2. Real Geology test world strip mine
3. Side-by-side composite, 2560×1440 or two 1280×720 panels

Save as `docs/publish/compare-vanilla-vs-realgeology.png`

---

## 🟢 6. Ko-fi / tip jar banner (if using Ko-fi)

| Spec | Value |
|------|--------|
| Size | Ko-fi header ~ **640×200** or profile image **256×256** |
| Filename | `docs/publish/kofi-header.png` |

Simple “Support Real Geology development” + fold graphic. Low priority.

---

## 🟢 7. CurseForge (only if you upload there too)

Same as Modrinth: icon 256×256 + 3–5 screenshots. Reuse gallery files.

---

## Quick capture cheat sheet

| Goal | Config | Command / action |
|------|--------|------------------|
| Fold wall | `section` + `force_collision_belt = true` | New world, fly into trench |
| Ore shapes | `ores` | New world, fly into trench |
| Normal play | both `false` / `off` | Strip mine or natural cliff |
| SVG export | — | `./gradlew runGeologyPreview` → `build/cross-sections/` |

Always use a **disposable test world**. Delete when done.

---

## What you can skip for beta

- Animated GIF
- YouTube thumbnail (unless you’re recording a devlog)
- Custom block/item models for “mod icon” in inventory
- Perfect true-scale cross-sections (exaggerated reads better in gallery)

---

## Deliver back to the repo

When done, drop files in:

```
docs/publish/
  icon-256.png
  gallery-01-folded-mountain.png
  gallery-02-basin-strata.png
  gallery-03-ingame-cliff.png
  gallery-04-deposits.png          (optional)
  github-banner.png                (optional)
  compare-vanilla-vs-realgeology.png (optional)
```

Then ping the dev: wire icon, update README embeds, upload to Modrinth gallery.
