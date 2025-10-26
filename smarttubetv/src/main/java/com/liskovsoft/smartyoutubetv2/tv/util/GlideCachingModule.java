package com.liskovsoft.smartyoutubetv2.tv.util;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

/**
 * https://bumptech.github.io/glide/doc/configuration.html#disk-cache<br/>
 * https://stackoverflow.com/questions/46108915/how-to-increase-the-cache-size-in-glide-android
 */
@GlideModule
public class GlideCachingModule extends AppGlideModule {
    private final static long DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100 MB (increased from 10 MB)

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //if (MyApplication.from(context).isTest())
        //    return; // NOTE: StatFs will crash on robolectric.

        // Increase disk cache size for better caching
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
        
        // Increase memory cache size to prevent reloading when navigating
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(3) // Cache 3 screens worth of images (increased from default 2)
                .build();
        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()));
    }
}
