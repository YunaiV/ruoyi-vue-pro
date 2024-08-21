package com.somle.eccang.model;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangOrderVO {
    private List<String> referenceNoList;
    private List<String> orderCodeList;
    private List<String> warehouseOrderCodeList;
    private List<String> productSkuList;
    private List<String> userAccountList;
    private List<String> shippingMethod;
    private List<Integer> warehouseIdList;
    private String status;
    private List<Integer> processAgains;
    private LocalDateTime createdDateStart;
    private LocalDateTime createdDateEnd;
    private LocalDateTime updateDateStart;
    private LocalDateTime updateDateEnd;
    private LocalDateTime shipDateStart;
    private LocalDateTime shipDateEnd;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime platformShipDateStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime platformShipDateEnd;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime warehouseShipDateStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime warehouseShipDateEnd;
    private LocalDateTime platformPaidDateStart;
    private LocalDateTime platformPaidDateEnd;
    private LocalDateTime platformCreateDateStart;
    private LocalDateTime platformCreateDateEnd;
    private LocalDateTime dateCreateSysStart;
    private LocalDateTime dateCreateSysEnd;
    private LocalDateTime trackDeliveredTimeStart;
    private LocalDateTime trackDeliveredTimeEnd;
    private String buyerName;
    private String platform;
    private int idDesc;
    private String customOrderType;
    private String isMark;
    private String createType;
    private int isCod;
    private int isTransferFbaDelivery;
}