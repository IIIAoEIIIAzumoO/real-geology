#!/usr/bin/env bash
# One-time setup: GitHub repo + first release. Run from geology-overhaul/ root.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

export PATH="${HOME}/.local/bin:${PATH}"

GITHUB_USER="${GITHUB_USER:-IIIAoEIIIAzumoO}"
REPO_NAME="${REPO_NAME:-real-geology}"
VERSION="$(grep '^mod_version=' gradle.properties | cut -d= -f2)"
TAG="v${VERSION}"

echo "==> Real Geology first publish"
echo "    GitHub: ${GITHUB_USER}/${REPO_NAME}"
echo "    Tag:    ${TAG}"
echo "    JAR:    realgeology-${VERSION}.jar"
echo

if ! command -v gh >/dev/null 2>&1; then
  echo "Install GitHub CLI first: https://cli.github.com/"
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "Log in to GitHub (browser will open — paste the one-time code if asked):"
  gh auth login --hostname github.com --git-protocol https --web
fi

echo "==> Building release JAR (1.21.1 primary target only)"
./gradlew :neoforge-1.21.1:build --no-daemon

JAR="neoforge-1.21.1/build/libs/realgeology-${VERSION}.jar"
# Keep legacy path for release.yml / Modrinth docs
mkdir -p build/libs
cp -f "$JAR" "build/libs/realgeology-${VERSION}.jar"
if [[ ! -f "$JAR" ]]; then
  echo "Expected JAR not found: $JAR"
  exit 1
fi

echo "==> Configuring git remote"
CURRENT_ORIGIN="$(git remote get-url origin 2>/dev/null || true)"
if [[ "$CURRENT_ORIGIN" == *NeoForgeMDKs* ]]; then
  git remote remove origin
fi

echo "==> Creating GitHub repository (public)"
if ! gh repo view "${GITHUB_USER}/${REPO_NAME}" >/dev/null 2>&1; then
  gh repo create "${REPO_NAME}" \
    --public \
    --source=. \
    --remote=origin \
    --description "NeoForge 1.21.1 — seed-stable folded strata and geological provinces for Minecraft" \
    --push
else
  if ! git remote get-url origin >/dev/null 2>&1; then
    gh repo set-default "${GITHUB_USER}/${REPO_NAME}"
    git remote add origin "https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
  fi
  echo "    Repository already exists — pushing main"
  git push -u origin main
fi

echo "==> Tagging release"
git tag -a "$TAG" -m "Real Geology ${VERSION} — folded strata beta" 2>/dev/null || true
git push origin "$TAG" 2>/dev/null || git push origin "$TAG" --force

echo "==> GitHub Release (also built by .github/workflows/release.yml on tag push)"
if ! gh release view "$TAG" >/dev/null 2>&1; then
  gh release create "$TAG" "$JAR" \
    --title "Real Geology ${VERSION}" \
    --notes-file CHANGELOG.md
fi

echo
echo "Done. Next steps:"
echo "  1. Open https://github.com/${GITHUB_USER}/${REPO_NAME}/releases"
echo "  2. Upload the same JAR to Modrinth — see MODRINTH-SETUP.md"
echo "  3. Add your Ko-fi URL to README.md and .github/FUNDING.yml"
echo "  4. Share the Modrinth page with testers"
