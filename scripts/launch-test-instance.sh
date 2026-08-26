#!/usr/bin/env bash
# Minimal Real Geology beta test instance — NeoForge 1.21.1 via Gradle dev runs.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

MODPACK_MODS="${MODPACK_MODS:-${HOME}/.minecraft/versions/Modern Industry & Colonies/mods}"
MODPACK_GAME_DIR="${MODPACK_GAME_DIR:-$(dirname "$MODPACK_MODS")}"
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
  --shaders       Copy Sodium/Iris JARs, shaderpack(s), and Iris/Sodium config from modpack
  --dry-run       With --shaders: list copies only; do not build or launch
  --help          Show this help

Environment:
  MODPACK_MODS    Folder to copy GeoStrata/Architectury from if libs/ is empty
  MODPACK_GAME_DIR  Modpack instance root (default: parent of MODPACK_MODS)
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


copy_modpack_file() {
  local rel="$1"
  local src="$MODPACK_GAME_DIR/$rel"
  local dest="$GAME_DIR/$rel"
  if [[ ! -f "$src" ]]; then
    return 1
  fi
  if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "    Would copy: $src -> $dest"
  else
    mkdir -p "$(dirname "$dest")"
    cp -f "$src" "$dest"
    echo "    Copied: $rel"
  fi
  return 0
}

stage_shaderpacks_and_config() {
  echo "==> Staging shader packs + Iris/Sodium config (test instance only)"
  if [[ ! -d "$MODPACK_GAME_DIR" ]]; then
    echo "    Skipped — MODPACK_GAME_DIR not found: $MODPACK_GAME_DIR" >&2
    return 1
  fi
  echo "    Modpack game dir: $MODPACK_GAME_DIR"

  local pack_dir="$MODPACK_GAME_DIR/shaderpacks"
  local copied_packs=0
  if [[ -d "$pack_dir" ]]; then
    local z
    shopt -s nullglob
    for z in "$pack_dir"/*.zip; do
      if [[ "$DRY_RUN" -eq 1 ]]; then
        echo "    Would copy shaderpack: $(basename "$z") -> $GAME_DIR/shaderpacks/"
      else
        mkdir -p "$GAME_DIR/shaderpacks"
        cp -f "$z" "$GAME_DIR/shaderpacks/"
        echo "    Copied shaderpack: $(basename "$z")"
      fi
      copied_packs=1
    done
    shopt -u nullglob
    if [[ "$copied_packs" -eq 0 ]]; then
      echo "    No .zip shader packs in $pack_dir" >&2
    fi
  else
    echo "    No shaderpacks/ in modpack game dir" >&2
  fi

  local rel
  for rel in \
    config/iris.properties \
    config/sodium-options.json \
    config/sodium-fingerprint.json \
    config/sodium-mixins.properties \
    config/iris-excluded.json; do
    if ! copy_modpack_file "$rel"; then
      echo "    Skipped $rel — not in modpack" >&2
    fi
  done

  if [[ "$DRY_RUN" -eq 0 && -f "$GAME_DIR/config/iris.properties" ]]; then
    local selected
    selected="$(grep -E '^shaderPack=' "$GAME_DIR/config/iris.properties" | cut -d= -f2- || true)"
    if [[ -n "$selected" && ! -f "$GAME_DIR/shaderpacks/$selected" ]]; then
      echo "    WARN: iris.properties shaderPack=$selected but file missing in run-publish-test/shaderpacks/" >&2
    elif [[ -n "$selected" ]]; then
      echo "    Active shader pack (iris.properties): $selected"
    fi
  fi
  echo
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
  copy_optional_mod_patterns "Entity Culling (optional)" \
    "entityculling-neoforge-*.jar" && staged=1 || true
  stage_shaderpacks_and_config
  if [[ "$staged" -eq 0 ]]; then
    echo "    No shader renderer JARs found — continuing without shaders (optional)." >&2
  else
    echo "    Shader stack: Sodium+Iris (+ optional Entity Culling) from MODPACK_MODS."
    echo "    Shader pack + iris.properties copied from MODPACK_GAME_DIR when present."
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
