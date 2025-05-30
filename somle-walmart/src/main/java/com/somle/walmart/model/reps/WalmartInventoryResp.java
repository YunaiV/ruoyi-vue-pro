package com.somle.walmart.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class WalmartInventoryResp {
    private String sku;
    private Quantity quantity;

    @Data

    @NoArgsConstructor
    public static class Quantity {
        private String unit;
        private int amount;
    }
}