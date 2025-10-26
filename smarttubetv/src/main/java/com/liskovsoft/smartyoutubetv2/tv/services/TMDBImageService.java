package com.liskovsoft.smartyoutubetv2.tv.services;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.liskovsoft.smartyoutubetv2.tv.presenters.MovieDetailsVideoActionPresenter;

public class TMDBImageService {
    private static final String TAG = "TMDBImageService";
    private static final String TMDB_API_KEY = "68872c817530adf9fd665f33874e926e";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String TMDB_BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w1280";
    
    private final OkHttpClient client;
    private final Gson gson;
    private final Random random;
    private final ExecutorService executor;
    
    public TMDBImageService() {
        // ARMv7: Configure OkHttp with SSL bypass for TMDB API
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(new okhttp3.ConnectionPool(2, 5, java.util.concurrent.TimeUnit.MINUTES));
        
        // ARMv7: Bypass SSL validation for TMDB API due to certificate issues
        try {
            // Create a trust manager that accepts all certificates
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[] {
                new javax.net.ssl.X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        // Accept all client certificates
                    }
                    
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        // Accept all server certificates
                    }
                    
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                }
            };
            
            // Create SSL context that accepts all certificates
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true); // Accept all hostnames
        } catch (Exception e) {
            Log.w(TAG, "Failed to configure SSL bypass, using default: " + e.getMessage());
        }
        
        this.client = builder.build();
        this.gson = new Gson();
        this.random = new Random();
        this.executor = Executors.newFixedThreadPool(1); // ARMv7: Reduce to single thread to prevent memory pressure
    }
    
    public void getRandomMoviePoster(TMDBImageCallback callback) {
        executor.execute(() -> {
            try {
                // Get popular movies
                String url = TMDB_BASE_URL + "/movie/popular?api_key=" + TMDB_API_KEY + "&page=" + (random.nextInt(20) + 1);
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        TMDBResponse tmdbResponse = gson.fromJson(json, TMDBResponse.class);
                        
                        if (tmdbResponse.results != null && !tmdbResponse.results.isEmpty()) {
                            TMDBMovie movie = tmdbResponse.results.get(random.nextInt(tmdbResponse.results.size()));
                            if (movie.posterPath != null && !movie.posterPath.isEmpty()) {
                                callback.onImageUrlReceived(TMDB_IMAGE_BASE_URL + movie.posterPath);
                                return;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error fetching TMDB poster", e);
            }
            
            // If we get here, something went wrong
            callback.onImageUrlReceived(null);
        });
    }
    
    public void getMoviePosterByTitle(String title, String description, TMDBImageCallback callback) {
        executor.execute(() -> {
            try {
                // First, check for TMDB movie ID in description
                String tmdbMovieId = extractTMDBMovieId(description);
                
                // If not found in description, check in title
                if (tmdbMovieId == null || tmdbMovieId.isEmpty()) {
                    tmdbMovieId = extractTMDBMovieId(title);
                }
                
                if (tmdbMovieId != null && !tmdbMovieId.isEmpty()) {
                    Log.i(TAG, "Found TMDB ID: " + tmdbMovieId);
                    getMoviePosterById(tmdbMovieId, callback);
                    return;
                }

                // Clean up the title for better matching
                String cleanTitle = cleanMovieTitle(title);
                if (cleanTitle.isEmpty()) {
                    callback.onImageUrlReceived(null);
                    return;
                }

                // Reduced delay for faster response when navigating quickly
                Thread.sleep(20);

                // Search for movie by title (fallback)
                String encodedTitle = java.net.URLEncoder.encode(cleanTitle, "UTF-8");
                String url = TMDB_BASE_URL + "/search/movie?api_key=" + TMDB_API_KEY + "&query=" + encodedTitle + "&include_adult=false";
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        TMDBResponse tmdbResponse = gson.fromJson(json, TMDBResponse.class);
                        
                        if (tmdbResponse.results != null && !tmdbResponse.results.isEmpty()) {
                            // Try to find the best match
                            TMDBMovie bestMatch = findBestMatch(cleanTitle, tmdbResponse.results);
                            if (bestMatch != null && bestMatch.posterPath != null && !bestMatch.posterPath.isEmpty()) {
                                String posterUrl = TMDB_IMAGE_BASE_URL + bestMatch.posterPath;
                                callback.onImageUrlReceived(posterUrl);
                                
                                // Send backdrop URL if available
                                if (bestMatch.backdropPath != null && !bestMatch.backdropPath.isEmpty()) {
                                    String backdropUrl = TMDB_BACKDROP_BASE_URL + bestMatch.backdropPath;
                                    callback.onBackdropUrlReceived(backdropUrl);
                                }
                                
                                // Also provide movie details
                                String rating = String.format("%.1f", bestMatch.voteAverage);
                                String releaseDate = bestMatch.releaseDate != null ? bestMatch.releaseDate.substring(0, 4) : "Unknown";
                                callback.onMovieDetailsReceived(
                                    bestMatch.title,
                                    bestMatch.overview != null ? bestMatch.overview : "No overview available",
                                    posterUrl,
                                    rating,
                                    releaseDate
                                );
                                
                                // Fetch detailed movie information (including certification)
                                getDetailedMovieInfo(bestMatch.id, callback);
                                return;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error searching TMDB for: " + title, e);
                // ARMv7: Try fallback approach for SSL issues
                tryFallbackSearch(cleanMovieTitle(title), callback);
                return;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error searching TMDB for: " + title, e);
            }

            // If we get here, something went wrong
            callback.onImageUrlReceived(null);
        });
    }
    
    
    private String fetchMovieCertification(int movieId) {
        try {
            // Get release dates and certification
            String url = TMDB_BASE_URL + "/movie/" + movieId + "/release_dates?api_key=" + TMDB_API_KEY;
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    TMDBReleaseDatesResponse releaseDatesResponse = gson.fromJson(json, TMDBReleaseDatesResponse.class);
                    
                    if (releaseDatesResponse.results != null && !releaseDatesResponse.results.isEmpty()) {
                        return findBestCertification(releaseDatesResponse.results);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching certification for movie ID: " + movieId, e);
        }
        return null;
    }
    
    private String findBestCertification(List<TMDBReleaseDateResult> results) {
        // Priority order: US, CA, GB, AU, then any other
        String[] preferredCountries = {"US", "CA", "GB", "AU"};
        
        for (String country : preferredCountries) {
            for (TMDBReleaseDateResult result : results) {
                if (country.equals(result.iso31661)) {
                    for (TMDBReleaseDate releaseDate : result.releaseDates) {
                        if (releaseDate.certification != null && !releaseDate.certification.isEmpty()) {
                            return releaseDate.certification;
                        }
                    }
                }
            }
        }
        
        // If no preferred country found, return any certification
        for (TMDBReleaseDateResult result : results) {
            for (TMDBReleaseDate releaseDate : result.releaseDates) {
                if (releaseDate.certification != null && !releaseDate.certification.isEmpty()) {
                    return releaseDate.certification;
                }
            }
        }
        
        return null;
    }
    
    private void tryFallbackSearch(String cleanTitle, TMDBImageCallback callback) {
        try {
            Log.i(TAG, "Trying fallback search for: " + cleanTitle);
            
            // Create a simpler client with SSL bypass
            OkHttpClient.Builder fallbackBuilder = new OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false);
            
            // Apply same SSL bypass for fallback
            try {
                javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[] {
                    new javax.net.ssl.X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
                };
                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                fallbackBuilder.sslSocketFactory(sslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) trustAllCerts[0]);
                fallbackBuilder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                Log.w(TAG, "Failed to configure fallback SSL bypass: " + e.getMessage());
            }
            
            OkHttpClient fallbackClient = fallbackBuilder.build();
            
            // Try a simpler search with basic parameters
            String encodedTitle = java.net.URLEncoder.encode(cleanTitle, "UTF-8");
            String url = TMDB_BASE_URL + "/search/movie?api_key=" + TMDB_API_KEY + "&query=" + encodedTitle;
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "SmartTube/1.0")
                    .build();

            try (Response response = fallbackClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    TMDBResponse tmdbResponse = gson.fromJson(json, TMDBResponse.class);
                    
                    if (tmdbResponse.results != null && !tmdbResponse.results.isEmpty()) {
                        TMDBMovie bestMatch = findBestMatch(cleanTitle, tmdbResponse.results);
                        if (bestMatch != null && bestMatch.posterPath != null && !bestMatch.posterPath.isEmpty()) {
                            String posterUrl = TMDB_IMAGE_BASE_URL + bestMatch.posterPath;
                            callback.onImageUrlReceived(posterUrl);
                            Log.i(TAG, "Fallback search successful for: " + cleanTitle);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Fallback search also failed for: " + cleanTitle, e);
        }
        
        // If fallback also fails, return null
        callback.onImageUrlReceived(null);
    }
    
    public void getDetailedMovieInfo(int movieId, TMDBImageCallback callback) {
        executor.execute(() -> {
            try {
                // ARMv7: Add longer delay to prevent memory pressure
                Thread.sleep(500);
                
                // Get detailed movie information including credits
                String url = TMDB_BASE_URL + "/movie/" + movieId + "?api_key=" + TMDB_API_KEY + "&append_to_response=credits";
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        
                        // ARMv7: Parse JSON more carefully to avoid memory issues
                        TMDBMovieDetails movieDetails = null;
                        try {
                            movieDetails = gson.fromJson(json, TMDBMovieDetails.class);
                        } catch (Exception parseException) {
                            Log.e(TAG, "JSON parsing error for movie ID: " + movieId, parseException);
                            callback.onDetailedMovieInfoReceived(null);
                            return;
                        }
                        
                        if (movieDetails != null) {
                            // Create detailed movie info object
                            TMDBDetailedMovieInfo detailedInfo = createDetailedMovieInfo(movieDetails);
                            
                            // Fetch certification synchronously
                            String certification = fetchMovieCertification(movieId);
                            if (certification != null && !certification.isEmpty()) {
                                detailedInfo.certification = certification;
                            }
                            
                            callback.onDetailedMovieInfoReceived(detailedInfo);
                            
                            // ARMv7: Force garbage collection after processing
                            System.gc();
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error fetching detailed movie info for ID: " + movieId, e);
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error fetching detailed movie info for ID: " + movieId, e);
            }
            
            // If we get here, something went wrong
            callback.onDetailedMovieInfoReceived(null);
        });
    }
    
    private TMDBDetailedMovieInfo createDetailedMovieInfo(TMDBMovieDetails details) {
        // Extract genres
        List<String> genres = new java.util.ArrayList<>();
        if (details.genres != null) {
            for (TMDBGenre genre : details.genres) {
                if (genre.name != null) {
                    genres.add(genre.name);
                }
            }
        }
        
        // Extract production companies
        List<String> productionCompanies = new java.util.ArrayList<>();
        if (details.productionCompanies != null) {
            for (TMDBProductionCompany company : details.productionCompanies) {
                if (company.name != null) {
                    productionCompanies.add(company.name);
                }
            }
        }
        
        // Extract production countries
        List<String> productionCountries = new java.util.ArrayList<>();
        if (details.productionCountries != null) {
            for (TMDBProductionCountry country : details.productionCountries) {
                if (country.name != null) {
                    productionCountries.add(country.name);
                }
            }
        }
        
        // Extract spoken languages
        List<String> spokenLanguages = new java.util.ArrayList<>();
        if (details.spokenLanguages != null) {
            for (TMDBSpokenLanguage language : details.spokenLanguages) {
                if (language.name != null) {
                    spokenLanguages.add(language.name);
                }
            }
        }
        
        // Extract cast (top 10)
        List<String> cast = new java.util.ArrayList<>();
        if (details.credits != null && details.credits.cast != null) {
            for (int i = 0; i < Math.min(10, details.credits.cast.size()); i++) {
                TMDBCast castMember = details.credits.cast.get(i);
                if (castMember.name != null && castMember.character != null) {
                    cast.add(castMember.name + " as " + castMember.character);
                }
            }
        }
        
        // Extract crew (directors and writers)
        List<String> crew = new java.util.ArrayList<>();
        String director = "Unknown";
        String writer = "Unknown";
        
        if (details.credits != null && details.credits.crew != null) {
            for (TMDBCrew crewMember : details.credits.crew) {
                if (crewMember.name != null) {
                    if ("Director".equals(crewMember.job)) {
                        director = crewMember.name;
                    } else if ("Writer".equals(crewMember.job) || "Screenplay".equals(crewMember.job)) {
                        if ("Unknown".equals(writer)) {
                            writer = crewMember.name;
                        } else {
                            writer += ", " + crewMember.name;
                        }
                    }
                }
            }
        }
        
        // Format runtime
        String runtime = details.runtime > 0 ? details.runtime + " minutes" : "Unknown";
        
        // Format rating
        String rating = String.format("%.1f/10", details.voteAverage);
        
        // Format release date
        String releaseDate = details.releaseDate != null && details.releaseDate.length() >= 4 ? 
            details.releaseDate.substring(0, 4) : "Unknown";
        
        return new TMDBDetailedMovieInfo(
            details.title != null ? details.title : "Unknown Title",
            details.overview != null ? details.overview : "No overview available",
            details.posterPath != null ? TMDB_IMAGE_BASE_URL + details.posterPath : "",
            details.backdropPath != null ? "https://image.tmdb.org/t/p/w1280" + details.backdropPath : "",
            rating,
            releaseDate,
            runtime,
            details.status != null ? details.status : "Unknown",
            details.tagline != null ? details.tagline : "",
            genres,
            productionCompanies,
            productionCountries,
            spokenLanguages,
            cast,
            crew,
            director,
            writer,
            null // certification will be set later by getMovieCertification
        );
    }
    
    private String cleanMovieTitle(String title) {
        if (title == null) return "";
        
        // Remove common YouTube suffixes and clean up the title
        String cleaned = title
                .replaceAll("\\s*\\(\\d{4}\\)\\s*$", "") // Remove year in parentheses
                .replaceAll("\\s*\\[.*?\\]\\s*$", "") // Remove anything in square brackets
                .replaceAll("\\s*\\(.*?Trailer.*?\\)\\s*$", "") // Remove trailer mentions
                .replaceAll("\\s*\\(.*?Official.*?\\)\\s*$", "") // Remove official mentions
                .replaceAll("\\s*\\(.*?HD.*?\\)\\s*$", "") // Remove HD mentions
                .replaceAll("\\s*\\(.*?4K.*?\\)\\s*$", "") // Remove 4K mentions
                .replaceAll("\\s*\\|.*$", "") // Remove everything after |
                .replaceAll("\\s*-.*$", "") // Remove everything after -
                .trim();
        
        return cleaned;
    }
    
    private String extractTMDBMovieId(String description) {
        if (description == null || description.isEmpty()) {
            Log.d(TAG, "extractTMDBMovieId: description is null or empty");
            return null;
        }
        
        Log.d(TAG, "extractTMDBMovieId: searching in description: " + description.substring(0, Math.min(200, description.length())));
        
        // Look for patterns like "tmdb_id:123456" or "TMDB ID: 123456" or "tmdb:123456"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?i)(?:tmdb_id|tmdb|movie_id|movie):\\s*(\\d+)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(description);
        
        if (matcher.find()) {
            String extractedId = matcher.group(1);
            Log.i(TAG, "extractTMDBMovieId: Found TMDB ID: " + extractedId);
            return extractedId;
        }
        
        Log.d(TAG, "extractTMDBMovieId: No TMDB ID pattern found in description");
        return null;
    }
    
    private void getMoviePosterById(String movieId, TMDBImageCallback callback) {
        try {
            Log.i(TAG, "getMoviePosterById: Fetching movie details for ID: " + movieId);
            String url = TMDB_BASE_URL + "/movie/" + movieId + "?api_key=" + TMDB_API_KEY;
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    TMDBMovieDetails movie = gson.fromJson(json, TMDBMovieDetails.class);
                    
                    if (movie != null) {
                        Log.i(TAG, "getMoviePosterById: Movie found - Title: " + movie.title + ", ID: " + movie.id);
                        if (movie.posterPath != null && !movie.posterPath.isEmpty()) {
                            String posterUrl = TMDB_IMAGE_BASE_URL + movie.posterPath;
                            callback.onImageUrlReceived(posterUrl);
                            
                            // Send backdrop URL if available
                            if (movie.backdropPath != null && !movie.backdropPath.isEmpty()) {
                                String backdropUrl = TMDB_BACKDROP_BASE_URL + movie.backdropPath;
                                callback.onBackdropUrlReceived(backdropUrl);
                            }
                            
                            // Also provide movie details
                            String rating = String.format("%.1f", movie.voteAverage);
                            String releaseDate = movie.releaseDate != null ? movie.releaseDate.substring(0, 4) : "Unknown";
                            callback.onMovieDetailsReceived(
                                movie.title,
                                movie.overview != null ? movie.overview : "No overview available",
                                posterUrl,
                                rating,
                                releaseDate
                            );
                            
                            // Fetch detailed movie information (including certification)
                            getDetailedMovieInfo(movie.id, callback);
                            return;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching movie by ID: " + movieId, e);
        }
        
        // If we get here, something went wrong - fallback to title search
        Log.w(TAG, "getMoviePosterById: Failed to fetch movie by ID, falling back to title search");
        callback.onImageUrlReceived(null);
    }
    
    private TMDBMovie findBestMatch(String searchTitle, List<TMDBMovie> movies) {
        if (movies.isEmpty()) return null;
        
        String lowerSearchTitle = searchTitle.toLowerCase();
        
        // First try exact match
        for (TMDBMovie movie : movies) {
            if (movie.title != null && movie.title.toLowerCase().equals(lowerSearchTitle)) {
                return movie;
            }
        }
        
        // Then try contains match
        for (TMDBMovie movie : movies) {
            if (movie.title != null && movie.title.toLowerCase().contains(lowerSearchTitle)) {
                return movie;
            }
        }
        
        // Finally, try partial match (at least 50% of words match)
        for (TMDBMovie movie : movies) {
            if (movie.title != null && calculateSimilarity(lowerSearchTitle, movie.title.toLowerCase()) > 0.5) {
                return movie;
            }
        }
        
        // Return first result as fallback
        return movies.get(0);
    }
    
    private double calculateSimilarity(String title1, String title2) {
        String[] words1 = title1.split("\\s+");
        String[] words2 = title2.split("\\s+");
        
        int matches = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2) || word1.contains(word2) || word2.contains(word1)) {
                    matches++;
                    break;
                }
            }
        }
        
        return (double) matches / Math.max(words1.length, words2.length);
    }
    
    private static class TMDBResponse {
        @SerializedName("results")
        List<TMDBMovie> results;
    }
    
    private static class TMDBMovie {
        @SerializedName("poster_path")
        String posterPath;
        
        @SerializedName("backdrop_path")
        String backdropPath;
        
        @SerializedName("title")
        String title;
        
        @SerializedName("overview")
        String overview;
        
        @SerializedName("vote_average")
        double voteAverage;
        
        @SerializedName("release_date")
        String releaseDate;
        
        @SerializedName("id")
        int id;
        
        @SerializedName("genre_ids")
        List<Integer> genreIds;
        
        @SerializedName("original_language")
        String originalLanguage;
        
        @SerializedName("popularity")
        double popularity;
        
        @SerializedName("vote_count")
        int voteCount;
    }
    
    private static class TMDBMovieDetails {
        @SerializedName("id")
        int id;
        
        @SerializedName("title")
        String title;
        
        @SerializedName("overview")
        String overview;
        
        @SerializedName("poster_path")
        String posterPath;
        
        @SerializedName("backdrop_path")
        String backdropPath;
        
        @SerializedName("vote_average")
        double voteAverage;
        
        @SerializedName("vote_count")
        int voteCount;
        
        @SerializedName("release_date")
        String releaseDate;
        
        @SerializedName("runtime")
        int runtime;
        
        @SerializedName("status")
        String status;
        
        @SerializedName("tagline")
        String tagline;
        
        @SerializedName("genres")
        List<TMDBGenre> genres;
        
        @SerializedName("production_companies")
        List<TMDBProductionCompany> productionCompanies;
        
        @SerializedName("production_countries")
        List<TMDBProductionCountry> productionCountries;
        
        @SerializedName("spoken_languages")
        List<TMDBSpokenLanguage> spokenLanguages;
        
        @SerializedName("credits")
        TMDBCredits credits;
    }
    
    private static class TMDBGenre {
        @SerializedName("id")
        int id;
        
        @SerializedName("name")
        String name;
    }
    
    private static class TMDBProductionCompany {
        @SerializedName("id")
        int id;
        
        @SerializedName("name")
        String name;
        
        @SerializedName("logo_path")
        String logoPath;
        
        @SerializedName("origin_country")
        String originCountry;
    }
    
    private static class TMDBProductionCountry {
        @SerializedName("iso_3166_1")
        String isoCode;
        
        @SerializedName("name")
        String name;
    }
    
    private static class TMDBSpokenLanguage {
        @SerializedName("iso_639_1")
        String isoCode;
        
        @SerializedName("name")
        String name;
    }
    
    private static class TMDBCredits {
        @SerializedName("cast")
        List<TMDBCast> cast;
        
        @SerializedName("crew")
        List<TMDBCrew> crew;
    }
    
    private static class TMDBCast {
        @SerializedName("id")
        int id;
        
        @SerializedName("name")
        String name;
        
        @SerializedName("character")
        String character;
        
        @SerializedName("order")
        int order;
        
        @SerializedName("profile_path")
        String profilePath;
    }
    
    private static class TMDBCrew {
        @SerializedName("id")
        int id;
        
        @SerializedName("name")
        String name;
        
        @SerializedName("job")
        String job;
        
        @SerializedName("department")
        String department;
    }
    
    private static class TMDBReleaseDatesResponse {
        @SerializedName("id")
        int id;
        
        @SerializedName("results")
        List<TMDBReleaseDateResult> results;
    }
    
    private static class TMDBReleaseDateResult {
        @SerializedName("iso_3166_1")
        String iso31661;
        
        @SerializedName("release_dates")
        List<TMDBReleaseDate> releaseDates;
    }
    
    private static class TMDBReleaseDate {
        @SerializedName("certification")
        String certification;
        
        @SerializedName("descriptors")
        List<String> descriptors;
        
        @SerializedName("iso_639_1")
        String iso6391;
        
        @SerializedName("note")
        String note;
        
        @SerializedName("release_date")
        String releaseDate;
        
        @SerializedName("type")
        int type;
    }
}