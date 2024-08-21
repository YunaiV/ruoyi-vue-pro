package com.somle.erp.model;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import lombok.Data;

@Data
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class ErpCountry {
    private String code;
    private String nameZh;
    private String nameEn;
}
