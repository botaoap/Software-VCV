#!/usr/bin/env bash
# Web bundle-size budget for the deployable Wasm distribution.
#
# Measures the *served* payload — the .wasm binaries plus the JS entry/chunks,
# gzipped (source maps and LICENSE files are excluded; they aren't shipped to
# users). The Compose-for-Web canvas build is heavy by nature (SDD §10–11), so
# this guard exists to catch *regressions*, not to shrink today's baseline.
#
# Usage:  scripts/check-web-budget.sh [DIST_DIR]
# Env:    WARN_BYTES (soft, annotates)  FAIL_BYTES (hard, exits 1)
#
# Runs locally and in CI. In GitHub Actions it emits ::warning:: / ::error::
# annotations and appends a summary table to $GITHUB_STEP_SUMMARY.
set -euo pipefail

DIST_DIR="${1:-webApp/build/dist/wasmJs/productionExecutable}"
WARN_BYTES="${WARN_BYTES:-5242880}"   # 5 MiB gzipped — soft budget
FAIL_BYTES="${FAIL_BYTES:-7340032}"   # 7 MiB gzipped — hard budget

if [[ ! -d "$DIST_DIR" ]]; then
  echo "error: distribution dir not found: $DIST_DIR" >&2
  echo "       build it first: ./gradlew :webApp:wasmJsBrowserDistribution" >&2
  exit 2
fi

in_ci() { [[ -n "${GITHUB_ACTIONS:-}" ]]; }
human() { awk -v b="$1" 'BEGIN{ split("B KiB MiB GiB",u); s=1; while(b>=1024 && s<4){b/=1024; s++} printf "%.2f %s", b, u[s] }'; }

total=0
rows=""
# Served assets: wasm binaries + JS (entry and any code-split chunks). Skip maps/licenses.
while IFS= read -r f; do
  [[ -f "$f" ]] || continue
  raw=$(wc -c < "$f" | tr -d ' ')
  gz=$(gzip -c "$f" | wc -c | tr -d ' ')
  total=$((total + gz))
  rows+=$(printf '| %s | %s | %s |\n' "$(basename "$f")" "$(human "$raw")" "$(human "$gz")")
done < <(find "$DIST_DIR" -maxdepth 1 -type f \( -name '*.wasm' -o -name '*.js' \) ! -name '*.map' ! -name '*.LICENSE.txt' | sort)

echo "Served payload (gzipped) for: $DIST_DIR"
printf '  %-30s %12s %12s\n' "asset" "raw" "gzipped"
find "$DIST_DIR" -maxdepth 1 -type f \( -name '*.wasm' -o -name '*.js' \) ! -name '*.map' ! -name '*.LICENSE.txt' | sort | while IFS= read -r f; do
  raw=$(wc -c < "$f" | tr -d ' ')
  gz=$(gzip -c "$f" | wc -c | tr -d ' ')
  printf '  %-30s %12s %12s\n' "$(basename "$f")" "$(human "$raw")" "$(human "$gz")"
done
echo "  ----"
printf '  %-30s %12s %12s\n' "TOTAL" "" "$(human "$total")"
echo "  budget: warn $(human "$WARN_BYTES") · fail $(human "$FAIL_BYTES")"

if in_ci; then
  {
    echo "### Web bundle budget"
    echo ""
    echo "| asset | raw | gzipped |"
    echo "|---|--:|--:|"
    printf '%s' "$rows"
    echo "| **TOTAL** | | **$(human "$total")** |"
    echo ""
    echo "Budget: warn $(human "$WARN_BYTES") · fail $(human "$FAIL_BYTES")."
  } >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
fi

if (( total > FAIL_BYTES )); then
  msg="Web bundle $(human "$total") gzipped exceeds the hard budget $(human "$FAIL_BYTES")."
  in_ci && echo "::error::$msg"
  echo "FAIL: $msg" >&2
  exit 1
elif (( total > WARN_BYTES )); then
  msg="Web bundle $(human "$total") gzipped exceeds the soft budget $(human "$WARN_BYTES")."
  in_ci && echo "::warning::$msg"
  echo "WARN: $msg"
else
  echo "OK: within budget ($(human "$total") gzipped)."
fi
