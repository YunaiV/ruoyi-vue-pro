package com.somle.manomano.model.req;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单查询请求参数实体
 */
@Builder
@Data
public class OrderQueryReqVO {


    private Long sellerContractId;

    private String status;

    private String orderReference;

    private String carrier;

    private LocalDateTime createdAtStart;

    private LocalDateTime createdAtEnd;

    private LocalDateTime statusUpdatedAtStart;

    private LocalDateTime statusUpdatedAtEnd;

    private Integer page;

    private Integer limit;

}