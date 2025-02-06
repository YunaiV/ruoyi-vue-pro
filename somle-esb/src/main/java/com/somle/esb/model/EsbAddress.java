package com.somle.esb.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EsbAddress {
    private String line1;
    private String line2;
    private String line3;
    private String postalCode;
    private String district;
    private String cityName;
    private String state;
    private String countryCode;
    private String countryName;
    private String longitude;
    private String latitude;
}
