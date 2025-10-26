#!/bin/bash

echo "Adding Kotlin JVM target configuration to build.gradle files..."

# Find all build.gradle files that use Kotlin and add JVM target
find . -name "build.gradle" -type f -exec grep -l "kotlin-android" {} \; | while read -r file; do
    echo "Processing $file"
    
    # Add kotlinOptions after compileOptions if it doesn't exist
    if ! grep -q "kotlinOptions" "$file"; then
        # Find the compileOptions block and add kotlinOptions after it
        sed -i '/compileOptions {/,/}/a\    }\n\n    kotlinOptions {\n        jvmTarget = "11"\n    }' "$file"
    fi
done

echo "Kotlin JVM target configuration completed!"