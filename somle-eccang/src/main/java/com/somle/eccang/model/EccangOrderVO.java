package com.somle.eccang.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangOrderVO {
    // 当前页
    private Integer page;

    // 每页数量（最大100）
    private Integer pageSize;

    // 是否返回订单明细数据，1:返回，0：不返回
    private Integer getDetail;

    // 是否返回订单地址数据，1:返回，0：不返回
    private Integer getAddress;

    // 是否返回自定义订单类型，1:返回，0：不返回
    private Integer getCustomOrderType;

    // 按年份查询订单，传年份：2019
    private Integer year;

    // 查询条件：biz_content.condition
    private Condition condition;

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Condition {
        private List<String> referenceNoList;
        private List<String> orderCodeList;
        private List<String> warehouseOrderCodeList;
        private List<String> productSkuList;
        private List<String> userAccountList;
        private List<String> shippingMethod;
        private List<Integer> warehouseIdList;
        private String status;
        private List<Integer> processAgains;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdDateEnd;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDateEnd;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime shipDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime shipDateEnd;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime platformShipDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime platformShipDateEnd;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime warehouseShipDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime warehouseShipDateEnd;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime platformPaidDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime platformPaidDateEnd;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime platformCreateDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime platformCreateDateEnd;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dateCreateSysStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dateCreateSysEnd;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime trackDeliveredTimeStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime trackDeliveredTimeEnd;
        private String buyerName;
        private String platform;
        private Integer idDesc;
        private String customOrderType;
        private String isMark;
        private String createType;
        private Integer isCod;
        private Integer isTransferFbaDelivery;
    }
}