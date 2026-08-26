# run-publish-test

Isolated NeoForge game directory for **pre-publish beta QA** (Real Geology + GeoStrata + Architectury only).

- Worlds, logs, and local config live here; they are **gitignored** except this README.
- Launch via `./scripts/launch-test-instance.sh` or `./gradlew runPublishTestClient`.
- See [TEST-INSTANCE.md](../TEST-INSTANCE.md) for mod list, config snippets, and the **new world only** warning.

Copy `docs/publish/realgeology-common-beta.toml` to `config/realgeology-common.toml` before your first world if you want the recommended beta defaults.
