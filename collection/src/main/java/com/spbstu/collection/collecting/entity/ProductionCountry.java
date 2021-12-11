package com.spbstu.collection.collecting.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ProductionCountry {

    public String iso_3166_1;
    public String name;

    @Override
    public String toString() {
        return "ProductionCountry{" +
                "iso31661='" + iso_3166_1 + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}