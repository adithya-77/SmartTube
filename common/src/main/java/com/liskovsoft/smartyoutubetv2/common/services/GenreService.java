package com.liskovsoft.smartyoutubetv2.common.services;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Shared service to store and retrieve movie genre information
 */
public class GenreService {
    private static final String TAG = GenreService.class.getSimpleName();
    private static GenreService sInstance;
    
    // Map to store video ID -> primary genre
    private final ConcurrentHashMap<String, String> sVideoGenreMap = new ConcurrentHashMap<>();
    
    private GenreService() {}
    
    public static GenreService instance() {
        if (sInstance == null) {
            sInstance = new GenreService();
        }
        return sInstance;
    }
    
    /**
     * Store the primary genre for a video
     */
    public void storeVideoGenre(String videoId, String primaryGenre) {
        if (videoId != null && primaryGenre != null && !primaryGenre.isEmpty()) {
            sVideoGenreMap.put(videoId, primaryGenre);
        }
    }
    
    /**
     * Get the primary genre for a video
     */
    public String getVideoGenre(String videoId) {
        if (videoId == null) {
            return null;
        }
        return sVideoGenreMap.get(videoId);
    }
    
    /**
     * Check if we have genre data for a video
     */
    public boolean hasGenreData(String videoId) {
        return videoId != null && sVideoGenreMap.containsKey(videoId);
    }
    
    /**
     * Get all video IDs that have genre data
     */
    public List<String> getVideosWithGenreData() {
        return new ArrayList<>(sVideoGenreMap.keySet());
    }
    
    /**
     * Clear all stored genre data
     */
    public void clearAll() {
        sVideoGenreMap.clear();
    }
}