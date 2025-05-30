package com.somle.cdiscount.model.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CdiscountProductRespVO {

    private String cursor;

    private int itemsPerPage;

    private List<Item> items;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Item {
        private String reference;
        private String gtin;
        private String language;
        private String label;
        private String description;
        private List<Picture> pictures;
        private Category category;
        private Brand brand;
        private List<Seller> sellers;
        private Permissions permissions;

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Picture {
            private int index;
            private String url;
            private LocalDateTime updatedAt;
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Category {
            private String reference;
            private String label;
            private String referencePath;
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Brand {
            private String reference;
            private String name;
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Permissions {
            private boolean edit;
            private boolean enrich;
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Seller {
            private String reference;
            private String productReference;
            private Boolean isCreator;
        }
    }
}