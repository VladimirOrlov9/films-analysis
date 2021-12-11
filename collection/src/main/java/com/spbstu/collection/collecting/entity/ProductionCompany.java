package com.spbstu.collection.collecting.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ProductionCompany {

    public Long id;
    public String logo_path;
    public String name;
    public String origin_country;

    @Override
    public String toString() {
        return "ProductionCompany{" +
                "id=" + id +
                ", logoPath='" + logo_path + '\'' +
                ", name='" + name + '\'' +
                ", originCountry='" + origin_country + '\'' +
                '}';
    }
}