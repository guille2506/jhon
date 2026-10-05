#!/usr/bin/env bash
#
# Notes app launcher.
#
# Brings up the whole stack with a single command:
#   1. PostgreSQL database (via Docker Compose)
#   2. Spring Boot backend (REST API on :8080) — schema is auto-created by JPA
#   3. React + Vite frontend (SPA on :5173)
#
# Usage:
#   ./run.sh          Start the full stack (Ctrl+C to stop)
#   ./run.sh --stop   Stop and remove the database container
#
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

# ---- Configuration (override via environment variables) ---------------------
DB_NAME="${DB_NAME:-notesdb}"
DB_USER="${DB_USER:-notes}"
DB_PASSWORD="${DB_PASSWORD:-notes}"
DB_PORT="${DB_PORT:-5432}"
SERVER_PORT="${SERVER_PORT:-8080}"
export DB_NAME DB_USER DB_PASSWORD DB_PORT SERVER_PORT

# Backend datasource (consumed by Spring Boot's application.properties)
export DB_URL="jdbc:postgresql://localhost:${DB_PORT}/${DB_NAME}"

# ---- Helpers ----------------------------------------------------------------
log() { printf "\033[1;34m[run]\033[0m %s\n" "$1"; }
err() { printf "\033[1;31m[run]\033[0m %s\n" "$1" >&2; }

# Pick the available Docker Compose command
if docker compose version >/dev/null 2>&1; then
  COMPOSE="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE="docker-compose"
else
  err "Docker Compose is required but was not found. Install Docker Desktop / docker-compose."
  exit 1
fi

# ---- Stop mode --------------------------------------------------------------
if [[ "${1:-}" == "--stop" ]]; then
  log "Stopping database container..."
  (cd "$ROOT_DIR" && $COMPOSE down)
  log "Stopped."
  exit 0
fi

# ---- Cleanup on exit --------------------------------------------------------
BACKEND_PID=""
cleanup() {
  log "Shutting down..."
  [[ -n "$BACKEND_PID" ]] && kill "$BACKEND_PID" >/dev/null 2>&1 || true
  log "Frontend and backend stopped. The database container keeps running; use './run.sh --stop' to remove it."
}
trap cleanup EXIT INT TERM

# ---- 1. Database ------------------------------------------------------------
log "Starting PostgreSQL (Docker)..."
(cd "$ROOT_DIR" && $COMPOSE up -d)

log "Waiting for the database to accept connections..."
for _ in $(seq 1 40); do
  if (cd "$ROOT_DIR" && $COMPOSE exec -T db pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1); then
    break
  fi
  sleep 2
done
log "Database is ready."

# ---- 2. Backend -------------------------------------------------------------
log "Starting Spring Boot backend on :${SERVER_PORT} (this also creates the DB schema)..."
(cd "$BACKEND_DIR" && ./mvnw -q spring-boot:run) &
BACKEND_PID=$!

log "Waiting for the backend to answer..."
for _ in $(seq 1 60); do
  if curl -sf "http://localhost:${SERVER_PORT}/api/categories" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done
log "Backend is up."

# ---- 3. Frontend ------------------------------------------------------------
log "Installing frontend dependencies (if needed)..."
(cd "$FRONTEND_DIR" && [[ -d node_modules ]] || npm install)

log "Starting frontend dev server on http://localhost:5173 ..."
log "Open http://localhost:5173 in your browser. Press Ctrl+C to stop."
(cd "$FRONTEND_DIR" && npm run dev)
