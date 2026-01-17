#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/render-plantuml.sh -i <input> -o <output-dir> [-f png|svg]

Renders PlantUML .puml files to images using plantuml or PLANTUML_JAR.
EOF
}

INPUT=""
OUTPUT=""
FORMAT="png"

while getopts ":i:o:f:h" opt; do
  case "$opt" in
    i) INPUT="$OPTARG" ;;
    o) OUTPUT="$OPTARG" ;;
    f) FORMAT="$OPTARG" ;;
    h) usage; exit 0 ;;
    *) usage; exit 1 ;;
  esac
done

if [ -z "$INPUT" ] || [ -z "$OUTPUT" ]; then
  usage
  exit 1
fi

if [ "$FORMAT" != "png" ] && [ "$FORMAT" != "svg" ]; then
  echo "Unsupported format: $FORMAT (use png or svg)" >&2
  exit 1
fi

if [ ! -e "$INPUT" ]; then
  echo "Input not found: $INPUT" >&2
  exit 1
fi

mkdir -p "$OUTPUT"

PLANTUML_BIN="${PLANTUML_BIN:-plantuml}"
PLANTUML_JAR="${PLANTUML_JAR:-}"

if [ -z "$PLANTUML_JAR" ] && ! command -v "$PLANTUML_BIN" >/dev/null 2>&1; then
  echo "plantuml not found. Install PlantUML or set PLANTUML_JAR." >&2
  exit 1
fi

render_file() {
  local file="$1"
  local base
  base="$(basename "$file" .puml)"
  local out="$OUTPUT/$base.$FORMAT"

  if [ -n "$PLANTUML_JAR" ]; then
    if ! command -v java >/dev/null 2>&1; then
      echo "java not found; required for PLANTUML_JAR." >&2
      exit 1
    fi
    java -jar "$PLANTUML_JAR" "-t$FORMAT" -pipe < "$file" > "$out"
  else
    "$PLANTUML_BIN" "-t$FORMAT" -pipe < "$file" > "$out"
  fi
}

if [ -d "$INPUT" ]; then
  found=0
  while IFS= read -r -d '' file; do
    found=1
    render_file "$file"
  done < <(find "$INPUT" -type f -name "*.puml" -print0)
  if [ "$found" -eq 0 ]; then
    echo "No .puml files found in $INPUT" >&2
  fi
else
  render_file "$INPUT"
fi
