#!/usr/bin/env bash
# Bare Real Geology test instance — NeoForge 26.2 (no GeoStrata; RockPalette vanilla fallbacks).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

GAME_DIR="$ROOT/run-publish-test-26.2"
MODE="client"
DO_BUILD=1

usage() {
  cat <<'USAGE'
Usage: ./scripts/launch-test-instance-26.2.sh [client|server] [options]

Options:
  --server        Launch dedicated server
  --no-build      Skip ./gradlew :neoforge-26.2:build
  --help          Show this help

Mods: Real Geology (dev classpath only). Optional JARs in libs-26.2/ if present.
Requires Java 25 toolchain (Gradle Foojay auto-provision).
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    client|server) MODE="$1"; shift ;;
    --server) MODE="server"; shift ;;
    --no-build) DO_BUILD=0; shift ;;
    --help|-h) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage >&2; exit 1 ;;
  esac
done

VERSION="$(grep '^mod_version=' gradle.properties | cut -d= -f2)"
MC="$(grep '^minecraft_version=' gradle.properties | cut -d= -f2)"

CONFIG_SRC="$GAME_DIR/config/realgeology-common.toml"
if [[ ! -f "$CONFIG_SRC" ]]; then
  mkdir -p "$GAME_DIR/config"
  if [[ -f "$ROOT/run-publish-test/config/realgeology-common.toml" ]]; then
    cp "$ROOT/run-publish-test/config/realgeology-common.toml" "$CONFIG_SRC"
  else
    echo "Missing $CONFIG_SRC — create with worldgen_mode = \"section\" for fold QA" >&2
    exit 1
  fi
fi

echo "==> Real Geology 26.2 bare test instance"
echo "    Version:  $VERSION (Minecraft $MC)"
echo "    Game dir: $GAME_DIR"
echo "    Mods:     Real Geology only (+ optional libs-26.2/*.jar)"
echo

if [[ "$DO_BUILD" -eq 1 ]]; then
  echo "==> Building :neoforge-26.2"
  ./gradlew :neoforge-26.2:build --no-daemon -q
  echo
fi

TASK="runPublishTestClient"
if [[ "$MODE" == "server" ]]; then
  TASK="runPublishTestServer"
fi

echo "==> Launching Gradle :neoforge-26.2:$TASK"
exec ./gradlew ":neoforge-26.2:$TASK" --no-daemon
