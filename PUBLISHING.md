# Publishing checklist — Real Geology beta

## Quick start (one command)

After installing [GitHub CLI](https://cli.github.com/) (`~/.local/bin/gh` works):

```bash
cd minecraft-givekit-project/geology-overhaul
./scripts/first-publish.sh
```

That logs you into GitHub (browser), creates `AzumoO/real-geology`, pushes the code, and uploads `v0.21.0-beta.2` with the JAR.

Then follow [MODRINTH-SETUP.md](MODRINTH-SETUP.md) for the Modrinth page (where testers actually download mods).

Use a different GitHub account:

```bash
GITHUB_USER=IIIAoEIIIAzumoO ./scripts/first-publish.sh
```

---

## 1. Legal and attribution

- [x] Real Geology source: MIT ([LICENSE](LICENSE))
- [ ] GeoStrata: **required dependency**, not bundled — users install [GeoStrata](https://modrinth.com/mod/geostrata) separately (Alkeari License Agreement)
- [ ] README states which assets are ours (ore overlays, kimberlite, worldgen) vs GeoStrata (rock block textures)
- [ ] No GeoStrata JAR or textures in the release artifact

## 2. Repository

The project was bootstrapped from the NeoForge MDK template. Before publishing:

```bash
# Create a new GitHub repo, e.g. azumoo/real-geology
git remote remove origin
git remote add origin git@github.com:YOU/real-geology.git
```

Commit everything except ignored run worlds/logs (see `.gitignore`).

Suggested first commit message: `Real Geology 0.21.0-beta — folded strata worldgen`.

## 3. Version string

Beta releases should use semver pre-release tags, e.g. `0.21.0-beta.2`, not internal branch names like `fault-wedges-curved-asthenosphere`.

Update `mod_version` in `gradle.properties`, then:

```bash
./gradlew clean build
```

Release JAR: `build/libs/realgeology-<version>.jar`

## 4. Mod host page (Modrinth recommended)

Upload the JAR with:

**Summary:** Seed-stable geological provinces with folded strata, faults, and realistic ore deposits for NeoForge 1.21.1.

**Required dependencies:**

| Mod | Version |
|-----|---------|
| NeoForge | 21.1.x |
| GeoStrata | 1.2.0+ |

**Optional (tested in author's pack):** Terralith, Geophilic — Real Geology reads biome/terrain tags when present but does not hard-require them.

**Warnings on the page:**

- New worlds only — do not add mid-save to an existing world you care about
- Experimental beta — worldgen may change between releases
- GeoStrata must be installed; Real Geology does not ship rock textures

## 5. GitHub release

Attach the same JAR plus:

- `build/cross-sections/*.svg` (fold inspection screenshots)
- Link to [MATERIAL-CATALOG.md](MATERIAL-CATALOG.md)

Tag: `v0.21.0-beta.2`

## 6. Community hooks

- Enable GitHub Issues with labels: `bug`, `worldgen`, `help wanted`, `roadmap`
- Pin ROADMAP.md in the repo description
- Discord/Discussion forum (optional) — link from README when ready

## 7. Post-release

- Watch for GeoStrata updates breaking registry IDs
- Triage "caves don't follow geology yet" as expected until karst milestone ships
