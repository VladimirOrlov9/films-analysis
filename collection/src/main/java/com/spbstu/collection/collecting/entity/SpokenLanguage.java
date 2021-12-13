package com.spbstu.collection.collecting.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SpokenLanguage {

    public String english_name;
    public String iso_639_1;
    public String name;

    @Override
    public String toString() {
        return "SpokenLanguage{" +
                "englishName='" + english_name + '\'' +
                ", iso6391='" + iso_639_1 + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}