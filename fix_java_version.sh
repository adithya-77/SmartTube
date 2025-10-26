#!/bin/bash

echo "Updating Java version from 1.8 to 11 for compatibility with Kotlin..."

# Find all build.gradle files and update Java version
find . -name "build.gradle" -type f -exec sed -i 's/JavaVersion\.VERSION_1_8/JavaVersion.VERSION_11/g' {} \;

echo "Java version updates completed!"