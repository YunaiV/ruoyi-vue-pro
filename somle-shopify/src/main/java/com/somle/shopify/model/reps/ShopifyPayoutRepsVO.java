package com.somle.shopify.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopifyPayoutRepsVO {

    private Long id;

    private String status;

    private String date;

    private String currency;

    private String amount;

    private SummaryDTO summary;

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SummaryDTO {

        private String adjustmentsFeeAmount;

        private String adjustmentsGrossAmount;

        private String chargesFeeAmount;

        private String chargesGrossAmount;

        private String refundsFeeAmount;

        private String refundsGrossAmount;

        private String reservedFundsFeeAmount;

        private String reservedFundsGrossAmount;

        private String retriedPayoutsFeeAmount;

        private String retriedPayoutsGrossAmount;
    }
}
