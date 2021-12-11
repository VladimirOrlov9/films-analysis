package com.spbstu.collection.collecting.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SpokenLanguage {

    public String englishName;
    public String iso6391;
    public String name;

    @Override
    public String toString() {
        return "SpokenLanguage{" +
                "englishName='" + englishName + '\'' +
                ", iso6391='" + iso6391 + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}