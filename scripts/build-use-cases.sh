#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
UC_DIR="$ROOT_DIR/docs/use-cases"
OUT_MD="$ROOT_DIR/docs/USE_CASES_ALL.md"
OUT_RENDERED_MD="$ROOT_DIR/docs/USE_CASES_ALL.rendered.md"
TMP_DIR="$UC_DIR/.tmp-plantuml"
DIAGRAMS_DIR="$UC_DIR/diagrams"
FORMAT="png"
RENDER_HTML="true"

usage() {
  cat <<'EOF'
Usage: scripts/build-use-cases.sh [-f png|svg] [--no-html]

Composes UC docs, extracts PlantUML, renders diagrams, and optionally renders HTML.
Requires: scripts/render-plantuml.sh and a markdown renderer (pandoc).
EOF
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    -f)
      FORMAT="$2"
      shift 2
      ;;
    --no-html)
      RENDER_HTML="false"
      shift 1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [ "$FORMAT" != "png" ] && [ "$FORMAT" != "svg" ]; then
  echo "Unsupported format: $FORMAT (use png or svg)" >&2
  exit 1
fi

mkdir -p "$UC_DIR" "$TMP_DIR" "$DIAGRAMS_DIR"
rm -f "$TMP_DIR"/*.puml

UC_FILES=(
  UC0-authenticate-start-session.md
  UC1-manage-athlete.md
  UC2-sync-athlete-data.md
  UC3-plan-lifecycle.md
  UC4-execute-workouts.md
  UC5-wellness-readiness.md
  UC6-compliance-progress.md
  UC7-adjust-plan.md
  UC8-communication.md
  UC9-configure-integrations.md
  UC10-testing-zones.md
  UC11-calendar-availability.md
  UC12-events-races.md
  UC13-notifications.md
  UC14-reports-exports.md
  UC15-admin-roles.md
  UC16-safety-guardrails.md
  UC17-workout-library.md
  UC18-multi-platform-reconciliation.md
  UC19-athlete-self-service-settings.md
  UC20-data-retention-consent.md
)

: > "$OUT_MD"
: > "$OUT_RENDERED_MD"

for file in "${UC_FILES[@]}"; do
  path="$UC_DIR/$file"
  if [ ! -f "$path" ]; then
    echo "Missing UC file: $path" >&2
    continue
  fi

  cat "$path" >> "$OUT_MD"
  printf "\n\n" >> "$OUT_MD"

  prefix="$(basename "$file" .md)"
  awk -v outdir="$TMP_DIR" -v prefix="$prefix" -v relpath="use-cases/diagrams" -v fmt="$FORMAT" '
    BEGIN {in_block=0; idx=0;}
    /^```plantuml[[:space:]]*$/ {in_block=1; idx++; fname=sprintf("%s/%s-%02d.puml", outdir, prefix, idx); next}
    in_block==1 && /^```/ {
      in_block=0;
      close(fname);
      printf("![%s-%02d](%s/%s-%02d.%s)\n", prefix, idx, relpath, prefix, idx, fmt);
      next
    }
    in_block==1 {print $0 >> fname; next}
    {print}
  ' "$path" >> "$OUT_RENDERED_MD"
  printf "\n\n" >> "$OUT_RENDERED_MD"
done

"$ROOT_DIR/scripts/render-plantuml.sh" -i "$TMP_DIR" -o "$DIAGRAMS_DIR" -f "$FORMAT"

if [ "$RENDER_HTML" = "true" ]; then
  if command -v pandoc >/dev/null 2>&1; then
    pandoc "$OUT_RENDERED_MD" -o "$ROOT_DIR/docs/USE_CASES_ALL.html"
  else
    echo "pandoc not found; skipping HTML render." >&2
  fi
fi

echo "Wrote: $OUT_MD"
echo "Wrote: $OUT_RENDERED_MD"
echo "Diagrams: $DIAGRAMS_DIR"
