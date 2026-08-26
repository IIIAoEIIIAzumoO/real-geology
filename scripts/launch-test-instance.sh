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

usage() {
  cat <<'USAGE'
Usage: ./scripts/launch-test-instance.sh [client|server] [options]

Options:
  --server        Launch dedicated server (same as positional "server")
  --no-build      Skip ./gradlew :neoforge-1.21.1:build (faster relaunch)
  --stage-jars    Copy release JAR + libs/*.jar into run-publish-test/mods/ (optional QA)
  --help          Show this help

Environment:
  MODPACK_MODS    Folder to copy GeoStrata/Architectury from if libs/ is empty
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    client|server) MODE="$1"; shift ;;
    --server) MODE="server"; shift ;;
    --no-build) DO_BUILD=0; shift ;;
    --stage-jars) STAGE_JARS=1; shift ;;
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
