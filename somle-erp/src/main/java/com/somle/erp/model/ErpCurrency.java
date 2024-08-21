package com.somle.erp.model;

import java.time.LocalDate;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ErpCurrency {
    private String code;
    private String name;
    private String exchangeRate;
    private LocalDate date;
}
