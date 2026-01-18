#!/bin/bash

# ==============================================================================
# HanziSpider Start Script
# ==============================================================================
# This script runs the HanziSpider application.
# It allows for easy configuration of all necessary settings via environment
# variables defined at the top of this file.

# --- Configuration ---
echo "Loading configuration..."

# General Settings
MAX_THREADS=10
SLEEP_INTERVAL_MS=5000
WEBHANDLER="jsoup"            # or "selenium"
OUTPUTHANDLER="mysqldatabase" # or "file"

# Proxy Settings (leave empty if no proxy is needed)
PROXY=""     # e.g., "localhost"
PROXYPORT="" # e.g., "3128"

# --- Database Configuration ---
# Set the database type you want to use: "mysql" or "postgresql"
DB_TYPE="postgresql"

DB_HOST="192.168.178.240"
DB_NAME="crawler"
DB_USER="crawler"
DB_PASSWORD="crawlerX"

# Ports
DB_PORT_MYSQL="3306"
DB_PORT_POSTGRES="5432"

# --- Set DB Connection String based on DB_TYPE ---
if [ "$DB_TYPE" == "mysql" ]; then
  DB_CONNECTION_STRING="jdbc:mysql://${DB_HOST}:${DB_PORT_MYSQL}/${DB_NAME}?user=${DB_USER}&password=${DB_PASSWORD}"
elif [ "$DB_TYPE" == "postgresql" ]; then
  # PostgreSQL driver is included via pom.xml
  DB_CONNECTION_STRING="jdbc:postgresql://${DB_HOST}:${DB_PORT_POSTGRES}/${DB_NAME}?user=${DB_USER}&password=${DB_PASSWORD}"
else
  echo "ERROR: Invalid DB_TYPE specified. Use 'mysql' or 'postgresql'."
  exit 1
fi

# --- Export Environment Variables for the Java Application ---
export MAX_THREADS
export SLEEP_INTERVAL_MS
export WEBHANDLER
export OUTPUTHANDLER
export PROXY
export PROXYPORT
export DB_CONNECTION_STRING

echo "Configuration loaded successfully."
echo "Database Type: $DB_TYPE"
echo "Connection String will be: $DB_CONNECTION_STRING"

# --- Find and Run the Fat JAR ---
echo "Starting HanziSpider..."

# Look for the assembled JAR with all dependencies
FAT_JAR=$(find target -name "*-jar-with-dependencies.jar" 2>/dev/null | head -1)

if [ -z "$FAT_JAR" ]; then
  echo "ERROR: Fat JAR not found in target directory."
  echo "Please run 'build.sh' first to build the project."
  exit 1
fi

echo "Using JAR: $FAT_JAR"
java -jar "$FAT_JAR"

echo "HanziSpider has stopped."
