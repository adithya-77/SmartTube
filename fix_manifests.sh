#!/bin/bash

echo "Fixing AndroidManifest.xml files to remove package attributes..."

# Find all AndroidManifest.xml files and remove package attributes
find . -name "AndroidManifest.xml" -type f | while read -r file; do
    echo "Processing $file"
    
    # Remove package attribute from manifest tag
    sed -i 's/package="[^"]*"//g' "$file"
    
    # Ensure xmlns:android is present
    if ! grep -q 'xmlns:android="http://schemas.android.com/apk/res/android"' "$file"; then
        sed -i 's/<manifest/<manifest xmlns:android="http:\/\/schemas.android.com\/apk\/res\/android"/' "$file"
    fi
done

echo "AndroidManifest.xml fixes completed!"