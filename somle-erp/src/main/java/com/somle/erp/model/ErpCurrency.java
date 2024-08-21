package com.somle.erp.model;

import java.time.LocalDate;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import lombok.Data;

@Data
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class ErpCurrency {
    private String code;
    private String name;
    private String exchangeRate;
    private LocalDate date;
}
