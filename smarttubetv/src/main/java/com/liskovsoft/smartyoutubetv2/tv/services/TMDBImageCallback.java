package com.liskovsoft.smartyoutubetv2.tv.services;

public interface TMDBImageCallback {
    void onImageUrlReceived(String imageUrl);
    void onBackdropUrlReceived(String backdropUrl);
    void onMovieDetailsReceived(String title, String overview, String poster, String rating, String releaseDate);
    void onDetailedMovieInfoReceived(TMDBDetailedMovieInfo movieInfo);
}