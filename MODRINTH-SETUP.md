# Modrinth setup — Real Geology

Modrinth is where most NeoForge testers find mods. GeoStrata is **not** on Modrinth yet (as of Aug 2026), so list it as a **manual dependency** in the project description until Alkeari publishes there.

## 1. Create your Modrinth account

https://modrinth.com/auth/sign-up

Enable **Creator monetization** under Settings → Billing if you want download revenue (Modrinth pays creators from their creator fund; amounts vary).

Also add an external link (Ko-fi, Patreon) on the project page for direct tips.

## 2. Create the project (web UI — first time only)

1. https://modrinth.com/mod/create
2. **Name:** Real Geology
3. **URL slug:** `real-geology`
4. **Summary:** Seed-stable folded strata and geological provinces for NeoForge 1.21.1
5. **Description:** paste from README.md (Requirements + warnings sections are critical)
6. **Categories:** World Gen, Utility (optional: Adventure)
7. **Client / server:** Both
8. **License:** MIT
9. **Links:** GitHub `https://github.com/AzumoO/real-geology`

In the description, add a bold **Dependencies** block:

```
Required:
- NeoForge 21.1.x (Minecraft 1.21.1)
- GeoStrata 1.2+ — install from CurseForge until it appears on Modrinth
```

## 3. Upload version 0.21.0-beta.2

1. Project → Versions → Create version
2. **Version number:** `0.21.0-beta.2`
3. **Name:** Folded strata beta
4. **Channel:** Beta
5. **Loaders:** NeoForge
6. **Game versions:** 1.21.1
7. **File:** `build/libs/realgeology-0.21.0-beta.2.jar`
8. **Changelog:** paste `CHANGELOG.md`

## 4. Gallery (recommended)

Upload 2–3 cross-section SVGs exported to PNG, or screenshots from debug section mode:

```bash
./gradlew runGeologyPreview
# build/cross-sections/*.svg → convert to PNG for Modrinth gallery
```

## 5. Optional — automated uploads later

Create a Modrinth personal access token (Settings → Tokens, scope `CREATE_VERSION`).

```bash
export MODRINTH_TOKEN="your-token"
# After project exists, note the project ID from the API or URL
export MODRINTH_PROJECT_ID="real-geology"
./gradlew modrinth
```

The Gradle `modrinth` task is configured in `build.gradle` but requires the token env var.

## 6. CurseForge (optional second channel)

Some players still use CurseForge. Same JAR, same changelog. GeoStrata may already be there — link it as a relation if the UI allows external relations.
