#!/usr/bin/env bash
# Minimal Real Geology beta test instance — NeoForge 1.21.1 via Gradle dev runs.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

MODPACK_MODS="${MODPACK_MODS:-${HOME}/.minecraft/versions/Modern Industry & Colonies/mods}"
GAME_DIR="$ROOT/run-publish-test"
LIBS_DIR="$ROOT/libs"
MODE="client"
DO_BUILD=1
STAGE_JARS=0
ENABLE_SHADERS="${ENABLE_SHADERS:-0}"
DRY_RUN=0

usage() {
  cat <<'USAGE'
Usage: ./scripts/launch-test-instance.sh [client|server] [options]

Options:
  --server        Launch dedicated server (same as positional "server")
  --no-build      Skip ./gradlew :neoforge-1.21.1:build (faster relaunch)
  --stage-jars    Copy release JAR + libs/*.jar into run-publish-test/mods/ (optional QA)
  --shaders       Copy shader stack JARs from MODPACK_MODS into run-publish-test/mods/
  --dry-run       With --shaders: list JARs that would be copied; do not build or launch
  --help          Show this help

Environment:
  MODPACK_MODS    Folder to copy GeoStrata/Architectury from if libs/ is empty
  ENABLE_SHADERS  Set to 1 to enable --shaders (same as passing --shaders)
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    client|server) MODE="$1"; shift ;;
    --server) MODE="server"; shift ;;
    --no-build) DO_BUILD=0; shift ;;
    --stage-jars) STAGE_JARS=1; shift ;;
    --shaders) ENABLE_SHADERS=1; shift ;;
    --dry-run) DRY_RUN=1; shift ;;
    --help|-h) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage >&2; exit 1 ;;
  esac
done

VERSION="$(grep '^mod_version=' gradle.properties | cut -d= -f2)"

ensure_lib() {
  local name="$1"
  local pattern="$2"
  if compgen -G "$LIBS_DIR/$pattern" > /dev/null; then
    return 0
  fi
  if [[ ! -d "$MODPACK_MODS" ]]; then
    echo "Missing $name in libs/ and MODPACK_MODS not found: $MODPACK_MODS" >&2
    echo "Place JARs in libs/ — see libs/README.md" >&2
    exit 1
  fi
  local src
  src="$(find "$MODPACK_MODS" -maxdepth 1 -name "$pattern" -print -quit)"
  if [[ -z "$src" ]]; then
    echo "Could not find $pattern in $MODPACK_MODS" >&2
    exit 1
  fi
  mkdir -p "$LIBS_DIR"
  cp -f "$src" "$LIBS_DIR/"
  echo "Copied $(basename "$src") -> libs/"
}

copy_optional_mod_patterns() {
  local label="$1"
  shift
  local patterns=("$@")
  if [[ ! -d "$MODPACK_MODS" ]]; then
    echo "    Skipped $label — MODPACK_MODS not found: $MODPACK_MODS" >&2
    return 1
  fi
  local pattern src
  for pattern in "${patterns[@]}"; do
    src="$(find "$MODPACK_MODS" -maxdepth 1 -name "$pattern" -print -quit)"
    if [[ -n "$src" ]]; then
      if [[ "$DRY_RUN" -eq 1 ]]; then
        echo "    Would stage shader mod: $(basename "$src")"
      else
        mkdir -p "$GAME_DIR/mods"
        cp -f "$src" "$GAME_DIR/mods/"
        echo "    Staged shader mod: $(basename "$src")"
      fi
      return 0
    fi
  done
  echo "    Skipped $label — none of: ${patterns[*]} in $MODPACK_MODS" >&2
  return 1
}

stage_shader_mods() {
  echo "==> Staging screenshot shader mods (test instance only — not in release JAR)"
  local staged=0
  # Sodium + Iris (NeoForge 1.21.1). Pack filenames may use + or URL-encoded %2B before mc1.21.1.
  copy_optional_mod_patterns "Sodium NeoForge" \
    "sodium-neoforge-*mc1.21.1*.jar" \
    "sodium-neoforge-*-mc1.21.1.jar" \
    "sodium-neoforge-*.jar" && staged=1 || true
  copy_optional_mod_patterns "Iris NeoForge" \
    "iris-neoforge-*mc1.21.1*.jar" \
    "iris-neoforge-*-mc1.21.1.jar" \
    "iris-neoforge-*.jar" && staged=1 || true
  # Embeddium + Oculus (alternate stack; not in Modern Industry & Colonies)
  copy_optional_mod_patterns "Embeddium NeoForge" \
    "embeddium-*-neoforge*.jar" \
    "embeddium-neoforge-*.jar" \
    "embeddium-*.jar" && staged=1 || true
  copy_optional_mod_patterns "Oculus NeoForge" \
    "oculus-*-neoforge*.jar" \
    "oculus-neoforge-*.jar" \
    "oculus-*.jar" && staged=1 || true
  if [[ "$staged" -eq 0 ]]; then
    echo "    No shader renderer JARs found — continuing without shaders (optional)." >&2
  else
    echo "    Shader stack: Sodium+Iris and/or Embeddium+Oculus (from MODPACK_MODS or Modrinth)."
    echo "    Place a shader pack in run-publish-test/shaderpacks/ and enable in Video Settings."
  fi
  echo
}

verify_libs() {
  local missing=0
  for pattern in "geostrata-*-NEOFORGE.jar" "architectury-*-neoforge.jar"; do
    if ! compgen -G "$LIBS_DIR/$pattern" > /dev/null; then
      echo "Missing libs/$pattern (required for localRuntime)" >&2
      missing=1
    fi
  done
  if [[ "$missing" -ne 0 ]]; then
    exit 1
  fi
  echo "==> Verified libs/*.jar (GeoStrata + Architectury)"
}

echo "==> Real Geology beta test instance"
echo "    Version:  $VERSION"
echo "    Game dir: $GAME_DIR"
echo "    Mods:     Real Geology (dev), GeoStrata, Architectury"
echo

ensure_lib "GeoStrata" "geostrata-*-NEOFORGE.jar"
ensure_lib "Architectury" "architectury-*-neoforge.jar"
verify_libs
echo

if [[ "$STAGE_JARS" -eq 1 ]]; then
  echo "==> Staging JARs into run-publish-test/mods/"
  mkdir -p "$GAME_DIR/mods"
  ./gradlew :neoforge-1.21.1:build --no-daemon -q
  JAR="neoforge-1.21.1/build/libs/realgeology-${VERSION}.jar"
  if [[ ! -f "$JAR" ]]; then
    echo "Built JAR not found: $JAR" >&2
    exit 1
  fi
  cp -f "$JAR" "$GAME_DIR/mods/"
  cp -f "$LIBS_DIR"/*.jar "$GAME_DIR/mods/"
  echo "    Staged: realgeology-${VERSION}.jar + libs/*.jar"
  echo
fi

if [[ "$ENABLE_SHADERS" -eq 1 ]]; then
  stage_shader_mods
fi

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "==> Dry run complete (no build, no launch)"
  exit 0
fi

if [[ "$DO_BUILD" -eq 1 ]]; then
  echo "==> Building mod"
  ./gradlew :neoforge-1.21.1:build --no-daemon -q
  echo
fi

TASK="runPublishTestClient"
if [[ "$MODE" == "server" ]]; then
  TASK="runPublishTestServer"
fi

echo "==> Launching Gradle $TASK (NeoForge dev)"
exec ./gradlew ":neoforge-1.21.1:$TASK" --no-daemon
