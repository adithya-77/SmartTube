#!/bin/bash

echo "Fixing all namespace issues for Android Gradle Plugin 8.x compatibility..."

# Function to add namespace to a build.gradle file
add_namespace() {
    local file="$1"
    local namespace="$2"
    
    if [ -f "$file" ] && grep -q "apply plugin: 'com.android.library'" "$file" && ! grep -q "namespace" "$file"; then
        echo "Adding namespace to $file"
        # Add namespace after android {
        sed -i "/^android {/a\    namespace '$namespace'\n" "$file"
    fi
}

# Fix main project modules
add_namespace "common/build.gradle" "com.liskovsoft.smartyoutubetv2.common"
add_namespace "chatkit/build.gradle" "com.stfalcon.chatkit"
add_namespace "leanbackassistant/build.gradle" "com.liskovsoft.leanbackassistant"
add_namespace "fragment-1.1.0/build.gradle" "androidx.fragment"
add_namespace "filepicker-lib/build.gradle" "com.nononsenseapps.filepicker"
add_namespace "leanback-1.0.0/build.gradle" "androidx.leanback"

# Fix ExoPlayer modules
add_namespace "exoplayer-amzn-2.10.6/library/core/build.gradle" "com.google.android.exoplayer2"
add_namespace "exoplayer-amzn-2.10.6/library/dash/build.gradle" "com.google.android.exoplayer2.ext.dash"
add_namespace "exoplayer-amzn-2.10.6/library/hls/build.gradle" "com.google.android.exoplayer2.ext.hls"
add_namespace "exoplayer-amzn-2.10.6/library/smoothstreaming/build.gradle" "com.google.android.exoplayer2.ext.smoothstreaming"
add_namespace "exoplayer-amzn-2.10.6/library/ui/build.gradle" "com.google.android.exoplayer2.ui"
add_namespace "exoplayer-amzn-2.10.6/library/sabr/build.gradle" "com.google.android.exoplayer2.ext.sabr"
add_namespace "exoplayer-amzn-2.10.6/library/all/build.gradle" "com.google.android.exoplayer2.all"

# Fix ExoPlayer extensions
add_namespace "exoplayer-amzn-2.10.6/extensions/cast/build.gradle" "com.google.android.exoplayer2.ext.cast"
add_namespace "exoplayer-amzn-2.10.6/extensions/cronet/build.gradle" "com.google.android.exoplayer2.ext.cronet"
add_namespace "exoplayer-amzn-2.10.6/extensions/ffmpeg/build.gradle" "com.google.android.exoplayer2.ext.ffmpeg"
add_namespace "exoplayer-amzn-2.10.6/extensions/flac/build.gradle" "com.google.android.exoplayer2.ext.flac"
add_namespace "exoplayer-amzn-2.10.6/extensions/gvr/build.gradle" "com.google.android.exoplayer2.ext.gvr"
add_namespace "exoplayer-amzn-2.10.6/extensions/ima/build.gradle" "com.google.android.exoplayer2.ext.ima"
add_namespace "exoplayer-amzn-2.10.6/extensions/jobdispatcher/build.gradle" "com.google.android.exoplayer2.ext.jobdispatcher"
add_namespace "exoplayer-amzn-2.10.6/extensions/leanback/build.gradle" "com.google.android.exoplayer2.ext.leanback"
add_namespace "exoplayer-amzn-2.10.6/extensions/mediasession/build.gradle" "com.google.android.exoplayer2.ext.mediasession"
add_namespace "exoplayer-amzn-2.10.6/extensions/okhttp/build.gradle" "com.google.android.exoplayer2.ext.okhttp"
add_namespace "exoplayer-amzn-2.10.6/extensions/opus/build.gradle" "com.google.android.exoplayer2.ext.opus"
add_namespace "exoplayer-amzn-2.10.6/extensions/rtmp/build.gradle" "com.google.android.exoplayer2.ext.rtmp"
add_namespace "exoplayer-amzn-2.10.6/extensions/vp9/build.gradle" "com.google.android.exoplayer2.ext.vp9"
add_namespace "exoplayer-amzn-2.10.6/extensions/workmanager/build.gradle" "com.google.android.exoplayer2.ext.workmanager"

# Fix test modules
add_namespace "exoplayer-amzn-2.10.6/testutils/build.gradle" "com.google.android.exoplayer2.testutil"
add_namespace "exoplayer-amzn-2.10.6/testutils_robolectric/build.gradle" "com.google.android.exoplayer2.testutil_robolectric"
add_namespace "exoplayer-amzn-2.10.6/playbacktests/build.gradle" "com.google.android.exoplayer2.playbacktests"

echo "Namespace fixes completed!"
echo "Now trying to build the project..."