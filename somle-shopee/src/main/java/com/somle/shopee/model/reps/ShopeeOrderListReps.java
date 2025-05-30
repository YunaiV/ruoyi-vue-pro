package com.somle.shopee.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopeeOrderListReps {
    private String error;
    private String message;
    private String requestId;
    private Response response;

    @Data
    @NoArgsConstructor
    public static class Response {
        private boolean more;
        private String nextCursor;
        private List<Order> orderList;

        @Data
        @NoArgsConstructor
        public static class Order {
            private String orderSn;
            private String bookingSn;
        }
    }
}