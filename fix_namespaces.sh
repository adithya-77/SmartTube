#!/bin/bash

# Fix namespace issues for all SharedModules build.gradle files

modules=(
    "SharedModules/sharedutils:com.liskovsoft.sharedutils"
    "SharedModules/commons-io-2.8.0:org.apache.commons.io"
    "SharedModules/sharedtests:com.liskovsoft.sharedtests"
    "SharedModules/j2v8:com.eclipsesource.j2v8"
    "MediaServiceCore/SharedModules/appupdatechecker:com.liskovsoft.appupdatechecker"
    "MediaServiceCore/SharedModules/appupdatechecker2:com.liskovsoft.appupdatechecker2"
    "MediaServiceCore/SharedModules/sharedutils:com.liskovsoft.sharedutils"
    "MediaServiceCore/SharedModules/commons-io-2.8.0:org.apache.commons.io"
    "MediaServiceCore/SharedModules/sharedtests:com.liskovsoft.sharedtests"
    "MediaServiceCore/SharedModules/j2v8:com.eclipsesource.j2v8"
)

for module_info in "${modules[@]}"; do
    IFS=':' read -r module_path namespace <<< "$module_info"
    build_file="$module_path/build.gradle"
    
    if [ -f "$build_file" ]; then
        echo "Fixing namespace for $build_file"
        
        # Check if namespace already exists
        if ! grep -q "namespace" "$build_file"; then
            # Add namespace after android {
            sed -i '/^android {/a\    namespace '\''"$namespace"'\''\n' "$build_file"
        fi
    fi
done

echo "Namespace fixes completed!"