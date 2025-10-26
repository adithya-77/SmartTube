#!/bin/bash

echo "Fixing namespace variables in build.gradle files..."

# Fix SharedModules
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'com.liskovsoft.sharedutils'"'"'/g' SharedModules/sharedutils/build.gradle
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'com.eclipsesource.j2v8'"'"'/g' SharedModules/j2v8/build.gradle
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'com.liskovsoft.sharedtests'"'"'/g' SharedModules/sharedtests/build.gradle

# Fix MediaServiceCore SharedModules
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'com.liskovsoft.appupdatechecker'"'"'/g' MediaServiceCore/SharedModules/appupdatechecker/build.gradle
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'com.liskovsoft.appupdatechecker2'"'"'/g' MediaServiceCore/SharedModules/appupdatechecker2/build.gradle
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'com.liskovsoft.sharedutils'"'"'/g' MediaServiceCore/SharedModules/sharedutils/build.gradle
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'com.eclipsesource.j2v8'"'"'/g' MediaServiceCore/SharedModules/j2v8/build.gradle
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'com.liskovsoft.sharedtests'"'"'/g' MediaServiceCore/SharedModules/sharedtests/build.gradle
sed -i 's/namespace '"'"'"$namespace"'"'"'/namespace '"'"'org.apache.commons.io'"'"'/g' MediaServiceCore/SharedModules/commons-io-2.8.0/build.gradle

echo "Namespace variable fixes completed!"