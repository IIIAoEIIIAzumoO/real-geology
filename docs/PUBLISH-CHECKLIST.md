# Real Geology beta publish checklist

**Target version:** `0.21.0-beta.2`  
**Primary release:** Minecraft **Java 1.21.1** + NeoForge 21.1.x + **GeoStrata 1.2+**  
**Do not publish** the NeoForge 26.2 JAR to Modrinth unless uploaded as a clearly labeled experimental file on a separate version channel.

Last audited: 2026-08-26

---

## Status at a glance

| Area | Status |
|------|--------|
| Source & version (`gradle.properties`) | ✅ Done |
| Release JAR built (1.21.1) | ✅ Done — `neoforge-1.21.1/build/libs/realgeology-0.21.0-beta.2.jar` (+ copy in `build/libs/`) |
| Docs (CHANGELOG, PUBLISHING, MODRINTH-SETUP, KNOWN-ISSUES) | ✅ Done |
| Publish scripts & CI workflows | ✅ Done (`scripts/first-publish.sh`, `.github/workflows/release.yml`) |
| GitHub repo + release | ⬜ **You** — `gh auth login` then `./scripts/first-publish.sh` |
| Modrinth project + version upload | ⬜ **You** — web UI (see [MODRINTH-SETUP.md](../MODRINTH-SETUP.md)) |
| Mod icon + gallery images | ⬜ **You** — see [publish/ASSETS-TODO.md](publish/ASSETS-TODO.md) |
| Ko-fi link on README / FUNDING | 🟡 Placeholder (`ko-fi.com/azumoo`) — confirm handle |

---

## 1. Code & build (complete)

- [x] `mod_version=0.21.0-beta.2` in `gradle.properties`
- [x] Dual-version Gradle layout (1.21.1 primary, 26.2 experimental compile-only)
- [x] GeoStrata required dependency in `neoforge.mods.toml` (not bundled)
- [x] Vanilla ore spawning for beta (custom deposits deferred)
- [x] MIT [LICENSE](../LICENSE)
- [x] [CHANGELOG.md](../CHANGELOG.md) for `0.21.0-beta.2`
- [x] [KNOWN-ISSUES-PLATE-GEN.md](KNOWN-ISSUES-PLATE-GEN.md) — documented playtest findings (not beta blockers)
- [x] Release JAR built:
  ```bash
  ./gradlew :neoforge-1.21.1:build
  # → neoforge-1.21.1/build/libs/realgeology-0.21.0-beta.2.jar (1.6 MB)
  ```

---

## 2. Repository & GitHub (you)

- [x] Local git history on `main` (9 commits ahead of NeoForge MDK template remote)
- [x] [scripts/first-publish.sh](../scripts/first-publish.sh) — creates `AzumoO/real-geology`, pushes, tags `v0.21.0-beta.2`, uploads JAR
- [x] [.github/workflows/release.yml](../.github/workflows/release.yml) — auto-release on tag push
- [x] Issue templates (bug report, feature request)
- [ ] **GitHub CLI authenticated** — run once:
  ```bash
  gh auth login --hostname github.com --git-protocol https --web
  ```
- [ ] **Run first publish:**
  ```bash
  cd minecraft-givekit-project/geology-overhaul
  ./scripts/first-publish.sh
  ```
  What it does: removes NeoForge MDK `origin` if present → creates public `AzumoO/real-geology` → pushes `main` → tags `v0.21.0-beta.2` → creates GitHub Release with JAR + CHANGELOG.
- [ ] Verify: https://github.com/AzumoO/real-geology/releases/tag/v0.21.0-beta.2

Different GitHub account:

```bash
GITHUB_USER=YourHandle ./scripts/first-publish.sh
```

---

## 3. Modrinth (you)

- [x] [MODRINTH-SETUP.md](../MODRINTH-SETUP.md) step-by-step
- [x] Paste-ready description: [publish/MODRINTH-DESCRIPTION.md](publish/MODRINTH-DESCRIPTION.md)
- [x] Gradle `modrinth` task configured (optional automation later via `MODRINTH_TOKEN`)
- [ ] Create Modrinth account / creator profile (if needed)
- [ ] Create project **Real Geology** — slug `real-geology`
- [ ] Upload version **0.21.0-beta.2**:
  - Channel: **Beta**
  - Loader: **NeoForge**
  - Game version: **1.21.1 only** (not 26.2)
  - File: `build/libs/realgeology-0.21.0-beta.2.jar`
  - Changelog: paste from [CHANGELOG.md](../CHANGELOG.md) (0.21.0-beta.2 section)
- [ ] Set **manual dependency** note for GeoStrata (not on Modrinth yet — install from CurseForge)
- [ ] Add GitHub link: `https://github.com/AzumoO/real-geology`
- [ ] Add Ko-fi / external support link (optional)

---

## 4. Assets (you — blocks Modrinth polish, not the JAR)

See [publish/ASSETS-TODO.md](publish/ASSETS-TODO.md).

- [ ] **Mod icon** — `docs/publish/icon-256.png` (256×256 PNG) → upload on Modrinth; wire `logoFile` in `neoforge.mods.toml` when ready
- [ ] **Gallery image 1** — folded mountain belt (`gallery-01-folded-mountain.png`)
- [ ] **Gallery image 2** — basin strata (`gallery-02-basin-strata.png`)
- [ ] **Gallery image 3** — in-game cliff (`gallery-03-ingame-cliff.png`)
- [ ] (Optional) GitHub README banner, before/after comparison, ore showcase

**Fast path for gallery 1–2:** crop/resize existing `build/cross-sections/*.png` or run `./gradlew runGeologyPreview` and export SVG → PNG.

---

## 5. README & community (mostly complete)

- [x] Supported versions table (1.21.1 beta vs 26.2 experimental)
- [x] Install steps, beta warnings, GeoStrata requirement
- [x] Link to [KNOWN-ISSUES-PLATE-GEN.md](KNOWN-ISSUES-PLATE-GEN.md) as post-beta improvements
- [x] [CONTRIBUTING.md](../CONTRIBUTING.md), [ROADMAP.md](../ROADMAP.md)
- [x] [.github/FUNDING.yml](../.github/FUNDING.yml) — `ko_fi: azumoo` (confirm URL)
- [ ] Pin ROADMAP or beta install note in GitHub repo description (after repo exists)
- [ ] Enable GitHub Discussions (optional)

---

## 6. Legal & attribution (complete)

- [x] Real Geology source: MIT
- [x] GeoStrata **not** bundled — users install separately
- [x] README states Real Geology owns worldgen; GeoStrata owns rock textures
- [x] No GeoStrata JAR in release artifact

---

## Fastest path to live beta (≈15 min after assets)

1. `gh auth login` (one time)
2. `./scripts/first-publish.sh` → GitHub repo + release JAR
3. Modrinth → Create project → paste [MODRINTH-DESCRIPTION.md](publish/MODRINTH-DESCRIPTION.md) → upload JAR
4. Drop icon + 3 gallery PNGs into `docs/publish/` when ready → upload to Modrinth gallery
5. Share Modrinth link with testers

---

## Post-release

- [ ] Watch GeoStrata updates for registry ID breaks
- [ ] Triage “caves don’t follow geology” as expected (karst milestone)
- [ ] Track [KNOWN-ISSUES-PLATE-GEN.md](KNOWN-ISSUES-PLATE-GEN.md) for post-beta fixes
