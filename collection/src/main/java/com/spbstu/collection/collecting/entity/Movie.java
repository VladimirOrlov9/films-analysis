package com.spbstu.collection.collecting.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Movie {

    private Boolean adult;
    private String backdrop_path;
    private BelongsToCollection belongs_to_collection;
    private Long budget;
    private Genre[] genres;
    private String homepage;
    private Long id;
    private String imdb_id;
    private String original_language;
    private String original_title;
    private String overview;
    private Double popularity;
    private String poster_path;
    private ProductionCompany[] production_companies;
    private ProductionCountry[] production_countries;
    private String release_date;
    private Long revenue;
    private Long runtime;
    private SpokenLanguage[] spoken_languages;
    private String status;
    private String tagline;
    private String title;
    private Boolean video;
    private Double vote_average;
    private Long vote_count;

    @Override
    public String toString() {
        return "Movie{" +
                "adult=" + adult +
                ", backdropPath='" + backdrop_path + '\'' +
                ", belongsToCollection=" + belongs_to_collection +
                ", budget=" + budget +
                ", genres=" + Arrays.toString(genres) +
                ", homepage='" + homepage + '\'' +
                ", id=" + id +
                ", imdbId='" + imdb_id + '\'' +
                ", originalLanguage='" + original_language + '\'' +
                ", originalTitle='" + original_title + '\'' +
                ", overview='" + overview + '\'' +
                ", popularity=" + popularity +
                ", posterPath='" + poster_path + '\'' +
                ", productionCompanies=" + Arrays.toString(production_companies) +
                ", productionCountries=" + Arrays.toString(production_countries) +
                ", releaseDate='" + release_date + '\'' +
                ", revenue=" + revenue +
                ", runtime=" + runtime +
                ", spokenLanguages=" + Arrays.toString(spoken_languages) +
                ", status='" + status + '\'' +
                ", tagline='" + tagline + '\'' +
                ", title='" + title + '\'' +
                ", video=" + video +
                ", voteAverage=" + vote_average +
                ", voteCount=" + vote_count +
                '}';
    }
}