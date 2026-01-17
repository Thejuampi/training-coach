#!/usr/bin/env bash

set -euo pipefail

PROFILE="dev"
MODE="uat"
BACKEND_ONLY="false"
UI_ONLY="false"
NO_BUILD="false"
PORT="8080"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profile)
      PROFILE="$2"; shift 2 ;;
    --mode)
      MODE="$2"; shift 2 ;;
    --backend-only)
      BACKEND_ONLY="true"; shift ;;
    --ui-only)
      UI_ONLY="true"; shift ;;
    --no-build)
      NO_BUILD="true"; shift ;;
    --port)
      PORT="$2"; shift 2 ;;
    -h|--help)
      echo "Usage: scripts/run.sh [--profile dev|uat|prod] [--mode uat|prod] [--backend-only] [--ui-only] [--no-build] [--port 8080]"
      exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1 ;;
  esac
done

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

build_backend() {
  if [[ "$NO_BUILD" == "true" ]]; then
    return
  fi
  mvn -q -DskipTests package
}

build_tui_classpath() {
  if [[ "$NO_BUILD" == "true" ]]; then
    return
  fi
  mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
}

start_backend_bg() {
  export SPRING_PROFILES_ACTIVE="$PROFILE"
  export SERVER_PORT="$PORT"
  mvn spring-boot:run -Dspring-boot.run.profiles="$PROFILE" &
  BACKEND_PID=$!
}

wait_for_backend() {
  local url="http://localhost:${PORT}/hello"
  local attempts=30
  local count=0
  while [[ $count -lt $attempts ]]; do
    if command -v curl >/dev/null 2>&1; then
      if curl -s -o /dev/null "$url"; then
        return 0
      fi
    fi
    sleep 1
    count=$((count + 1))
  done
  return 0
}

start_backend_fg() {
  export SPRING_PROFILES_ACTIVE="$PROFILE"
  export SERVER_PORT="$PORT"
  mvn spring-boot:run -Dspring-boot.run.profiles="$PROFILE"
}

start_tui_fg() {
  build_tui_classpath
  CLASSPATH="target/classes"
  if [[ -f "target/classpath.txt" ]]; then
    DEPS="$(cat target/classpath.txt | tr -d '\n')"
    if [[ -n "$DEPS" ]]; then
      CLASSPATH="${CLASSPATH}:${DEPS}"
    fi
  fi
  export TRAINING_COACH_BASE_URL="http://localhost:${PORT}"
  java -cp "$CLASSPATH" com.training.coach.tui.TuiApp
}

cleanup() {
  if [[ -n "${TUI_PID:-}" ]]; then
    kill "$TUI_PID" >/dev/null 2>&1 || true
  fi
  if [[ -n "${BACKEND_PID:-}" ]]; then
    kill "$BACKEND_PID" >/dev/null 2>&1 || true
  fi
}

trap cleanup EXIT

build_backend

if [[ "$BACKEND_ONLY" == "true" && "$UI_ONLY" == "false" ]]; then
  start_backend_fg
  exit 0
fi

if [[ "$UI_ONLY" == "true" && "$BACKEND_ONLY" == "false" ]]; then
  start_tui_fg
  exit 0
fi

start_backend_bg
wait_for_backend
start_tui_fg
