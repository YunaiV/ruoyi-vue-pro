package com.somle.manomano.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OffersInfoRespVO {

    private List<ContentDTO> content;
    private PaginationDTO pagination;

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaginationDTO {
        private Integer page;
        private Integer pages;
        private Integer items;
        private Integer limit;
        private LinksDTO links;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class LinksDTO {
            private String previous;
            private String next;
            private String first;
            private String last;
            private String gotoX;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ContentDTO {
        private String sku;
        private Double price;
        private Double retailPrice;
        private Integer stock;
        private Integer idMe;
        private String idMeLink;
        private String carrier;
        private String shippingTime;
        private Boolean offerIsOnline;
        private Boolean frozenPrice;
        private Boolean frozenRetailPrice;
        private Boolean frozenStock;
        private String status;
        private List<?> errors;
    }
}
