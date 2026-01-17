#!/usr/bin/env bash

set -euo pipefail

PORT="8080"
BACKEND_ONLY=""
UI_ONLY=""
NO_BUILD=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --port)
      PORT="$2"; shift 2 ;;
    --backend-only)
      BACKEND_ONLY="--backend-only"; shift ;;
    --ui-only)
      UI_ONLY="--ui-only"; shift ;;
    --no-build)
      NO_BUILD="--no-build"; shift ;;
    -h|--help)
      echo "Usage: scripts/run-uat.sh [--port 8080] [--backend-only] [--ui-only] [--no-build]"
      exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1 ;;
  esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
"$SCRIPT_DIR/run.sh" --profile uat --mode uat --port "$PORT" $BACKEND_ONLY $UI_ONLY $NO_BUILD
