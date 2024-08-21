package com.somle.erp.model;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class ErpAddress {
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
