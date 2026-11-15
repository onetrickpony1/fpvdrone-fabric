#!/usr/bin/env bash
# Boots the mod on a dedicated server (Fabric's gametest server: starts, runs the registered tests, exits).
# Catches common code that reaches client-only classes.   tools/run-server-check.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
until [ "$(awk '/MemAvailable/{print $2}' /proc/meminfo)" -gt 6000000 ]; do echo "waiting for memory..."; sleep 15; done
source "$ROOT/env.sh"
cd "$ROOT/fpvdrone-fabric"
mkdir -p build/server-run && echo "eula=true" > build/server-run/eula.txt
bash ./gradlew runServerCheck --console=plain "$@" > build/server-check.log 2>&1 && status=0 || status=$?
grep -E "\[server\]|fpvdrone|Exception|Error|BUILD|FAILED|passed|failed|Loading" build/server-check.log | grep -v '^\s*at ' | cut -c1-300 | tail -40 || true
echo "log: fpvdrone-fabric/build/server-check.log (exit $status)"
exit $status
