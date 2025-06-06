package com.somle.rakuten.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RakutenInventoryRepsVO {

    private List<InventoryItem> inventories;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class InventoryItem {

        private String manageNumber;
        private String variantId;
        private int quantity;
        private OffsetDateTime created;
        private OffsetDateTime updated;
    }
}