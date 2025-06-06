package com.somle.bestbuy.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BestbuyOrderReq {
    private Integer offset;
    private Integer max;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
