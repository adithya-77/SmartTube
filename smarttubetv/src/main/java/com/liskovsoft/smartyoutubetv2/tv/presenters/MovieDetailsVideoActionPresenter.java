package com.liskovsoft.smartyoutubetv2.tv.presenters;

import android.content.Context;
import android.content.Intent;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.tv.ui.moviedetails.MovieDetailsActivity;
import com.liskovsoft.smartyoutubetv2.tv.services.TMDBDetailedMovieInfo;
import com.liskovsoft.youtubeapi.service.GenreService;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom presenter that opens movie details page instead of direct playback
 */
public class MovieDetailsVideoActionPresenter {
    
    private final Context mContext;
    private static final ConcurrentHashMap<String, TMDBDetailedMovieInfo> sDetailedMovieInfoMap = new ConcurrentHashMap<>();
    
    public MovieDetailsVideoActionPresenter(Context context) {
        mContext = context;
    }
    
    public static MovieDetailsVideoActionPresenter instance(Context context) {
        return new MovieDetailsVideoActionPresenter(context);
    }
    
    public static void storeDetailedMovieInfo(String videoId, TMDBDetailedMovieInfo detailedInfo) {
        if (videoId != null && detailedInfo != null) {
            sDetailedMovieInfoMap.put(videoId, detailedInfo);
            
            // Also store the primary genre in the shared service
            if (detailedInfo.genres != null && !detailedInfo.genres.isEmpty()) {
                String primaryGenre = detailedInfo.genres.get(0);
                GenreService.instance().storeVideoGenre(videoId, primaryGenre);
            }
        }
    }
    
    /**
     * Get the primary genre for a video from stored TMDB data
     */
    public static String getPrimaryGenreForVideo(String videoId) {
        if (videoId == null) {
            return null;
        }
        
        TMDBDetailedMovieInfo detailedInfo = sDetailedMovieInfoMap.get(videoId);
        if (detailedInfo != null && detailedInfo.genres != null && !detailedInfo.genres.isEmpty()) {
            // Return the first genre as the primary genre
            return detailedInfo.genres.get(0);
        }
        
        return null;
    }
    
    /**
     * Get all genres for a video from stored TMDB data
     */
    public static java.util.List<String> getAllGenresForVideo(String videoId) {
        if (videoId == null) {
            return null;
        }
        
        TMDBDetailedMovieInfo detailedInfo = sDetailedMovieInfoMap.get(videoId);
        if (detailedInfo != null) {
            return detailedInfo.genres;
        }
        
        return null;
    }
    
    /**
     * Get detailed movie info for a video ID (for external access)
     */
    public static TMDBDetailedMovieInfo getDetailedMovieInfo(String videoId) {
        if (videoId == null) {
            return null;
        }
        return sDetailedMovieInfoMap.get(videoId);
    }
    
    public void apply(Video item) {
        if (item == null || mContext == null) {
            android.util.Log.e("MovieDetailsVideoActionPresenter", "Item or context is null!");
            return;
        }
        
        android.util.Log.d("MovieDetailsVideoActionPresenter", "Opening movie details for: " + item.getTitle());
        // Open movie details page instead of direct playback
        openMovieDetails(item);
    }
    
    private void openMovieDetails(Video video) {
        if (video == null || mContext == null) {
            return;
        }
        
        Intent intent = new Intent(mContext, MovieDetailsActivity.class);
        
        // Get backdrop URL from SQLite cache if not in memory
        String backdropUrl = video.backdropImageUrl;
        if ((backdropUrl == null || backdropUrl.isEmpty()) && video.videoId != null) {
            backdropUrl = com.liskovsoft.smartyoutubetv2.tv.services.TMDBDataCache.instance(mContext).getBackdropUrl(video.videoId);
        }
        
        // Check if we have detailed movie information
        TMDBDetailedMovieInfo detailedInfo = sDetailedMovieInfoMap.get(video.videoId);
        if (detailedInfo != null) {
            
            // Use detailed TMDB information
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_TITLE, detailedInfo.title);
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_OVERVIEW, detailedInfo.overview);
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_POSTER, detailedInfo.posterUrl);
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_BACKDROP, backdropUrl != null && !backdropUrl.isEmpty() ? backdropUrl : "");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_RATING, detailedInfo.rating);
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_RELEASE_DATE, detailedInfo.releaseDate);
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_RUNTIME, detailedInfo.runtime);
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_STATUS, detailedInfo.status);
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_TAGLINE, detailedInfo.tagline);
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_GENRES, String.join(", ", detailedInfo.genres));
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_PRODUCTION_COMPANIES, String.join(", ", detailedInfo.productionCompanies));
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_PRODUCTION_COUNTRIES, String.join(", ", detailedInfo.productionCountries));
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_SPOKEN_LANGUAGES, String.join(", ", detailedInfo.spokenLanguages));
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_CERTIFICATION, detailedInfo.certification != null ? detailedInfo.certification : "");
        } else {
            // Fallback to basic video information
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_TITLE, video.getTitle());
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_OVERVIEW, "No overview available");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_POSTER, video.getCardImageUrl() != null ? video.getCardImageUrl() : "");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_BACKDROP, backdropUrl != null && !backdropUrl.isEmpty() ? backdropUrl : "");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_RATING, "N/A");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_RELEASE_DATE, "Unknown");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_RUNTIME, "Unknown");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_STATUS, "Unknown");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_TAGLINE, "");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_GENRES, "");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_PRODUCTION_COMPANIES, "");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_PRODUCTION_COUNTRIES, "");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_SPOKEN_LANGUAGES, "");
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_CERTIFICATION, "");
        }
        
        intent.putExtra(MovieDetailsActivity.EXTRA_VIDEO_ID, video.videoId);
        
        mContext.startActivity(intent);
    }
    
    public static void updateMovieCertification(String videoId, String certification) {
        TMDBDetailedMovieInfo detailedInfo = sDetailedMovieInfoMap.get(videoId);
        if (detailedInfo != null) {
            detailedInfo.certification = certification;
        }
    }
}