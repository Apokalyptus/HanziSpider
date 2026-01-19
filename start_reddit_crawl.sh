#!/bin/bash
set -euo pipefail

# Reddit crawl (30 min, 7 threads) with scoped URL and main content selector.

if [ -f .env ]; then
  # shellcheck disable=SC1091
  source .env
fi

DB_HOST="${DB_HOST:-192.168.178.240}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-crawler_test}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-}"

if [ -z "$DB_PASSWORD" ]; then
  echo "ERROR: DB_PASSWORD is not set. Provide it via .env or environment."
  exit 1
fi

DB_CONNECTION_STRING="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?user=${DB_USER}&password=${DB_PASSWORD}"

export DB_CONNECTION_STRING
export MAX_THREADS="${MAX_THREADS:-7}"
export SLEEP_INTERVAL_MS="${SLEEP_INTERVAL_MS:-1000}"
export WEBHANDLER="${WEBHANDLER:-jsoup}"
export OUTPUTHANDLER="${OUTPUTHANDLER:-mysqldatabase}"
export SINGLE_SPIDER_URL="${SINGLE_SPIDER_URL:-https://www.reddit.com/r/China_irl/}"
export CONTENT_SELECTOR="${CONTENT_SELECTOR:-#main-content}"
export CHECKOUT_TIMEOUT_MS="${CHECKOUT_TIMEOUT_MS:-600000}"

timeout 1800s java -jar target/HanziSpider-1.0-SNAPSHOT-jar-with-dependencies.jar
