package com.liskovsoft.smartyoutubetv2.tv.services;

import java.util.List;

public class TMDBDetailedMovieInfo {
    public String title;
    public String overview;
    public String posterUrl;
    public String backdropUrl;
    public String rating;
    public String releaseDate;
    public String runtime;
    public String status;
    public String tagline;
    public List<String> genres;
    public List<String> productionCompanies;
    public List<String> productionCountries;
    public List<String> spokenLanguages;
    public List<String> cast;
    public List<String> crew;
    public String director;
    public String writer;
    public String certification;
    
    public TMDBDetailedMovieInfo(String title, String overview, String posterUrl, String backdropUrl, 
                                String rating, String releaseDate, String runtime, String status, 
                                String tagline, List<String> genres, List<String> productionCompanies,
                                List<String> productionCountries, List<String> spokenLanguages,
                                List<String> cast, List<String> crew, String director, String writer, String certification) {
        this.title = title;
        this.overview = overview;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.status = status;
        this.tagline = tagline;
        this.genres = genres;
        this.productionCompanies = productionCompanies;
        this.productionCountries = productionCountries;
        this.spokenLanguages = spokenLanguages;
        this.cast = cast;
        this.crew = crew;
        this.director = director;
        this.writer = writer;
        this.certification = certification;
    }
}