package com.spbstu.collection.collecting.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BelongsToCollection {

    public Long id;
    public String name;
    public String poster_path;
    public String backdrop_path;

    @Override
    public String toString() {
        return "BelongsToCollection{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", posterPath='" + poster_path + '\'' +
                ", backdropPath='" + backdrop_path + '\'' +
                '}';
    }
}