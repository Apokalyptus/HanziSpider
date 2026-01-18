#!/bin/bash

# ==============================================================================
# HanziSpider Build Script
# ==============================================================================
# This script builds the HanziSpider application.

# --- Build Project with Maven ---
echo "Building project with Maven (skipping tests)..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Maven build failed. Exiting."
    exit 1
fi

# --- Gather Dependencies ---
echo "Copying dependencies..."
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency
if [ $? -ne 0 ]; then
    echo "Failed to copy dependencies. Exiting."
    exit 1
fi

echo "Build complete."