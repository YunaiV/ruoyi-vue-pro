package com.somle.cdiscount.model.req;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderReqVO {

    private Integer pageIndex;
    private Integer pageSize;
    private String sort;
    private String desc;
    private String salesChannelId;
    private Boolean isBusinessOrder;
    private String status;
    private String supplyMode;
    private String shippingCountry;
    private LocalDateTime updatedAtMin;
    private LocalDateTime updatedAtMax;
    private LocalDateTime createdAtMin;
    private LocalDateTime createdAtMax;
    private LocalDateTime shippedAtMin;
    private LocalDateTime shippedAtMax;
}
