package com.somle.amazon.model.reps;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AmazonSpListingRepsVO {

    private Integer numberOfResults;
    private Pagination pagination;
    private List<ProductItem> items;

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Pagination {
        private String nextToken;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductItem {
        private String sku;
        private List<ProductSummary> summaries;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductSummary {
        private String marketplaceId;
        private String asin;
        private String productType;
        private String conditionType;
        private List<String> status;
        private String fnSku;
        private String itemName;
        private String createdDate;
        private String lastUpdatedDate;
        private MainImage mainImage;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MainImage {
        private String link;
        private Integer height;
        private Integer width;
    }
}
