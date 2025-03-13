package com.somle.eccang.model.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

//getReceiving 请求体
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangReceivingReqVo {
    // 入库单号
    private String receivingCode;

    // 客户参考号
    private String referenceNo;

    // 跟踪号
    private String trackingNumber;

    // 采购单号
    private String poCode;

    // 仓库ID
    private Integer warehouseId;

    // 入库单类型
    private Integer receivingType;

    // 入库状态
    private Integer receivingStatus;

    // 产品代码
    private String productHerode;

    // 产品代码（模糊）
    private String productHerodeLite;

    // 采购员（用户ID）
    private Integer receivingAddUser;

    // 单号（入库单、参考号、采购单）
    private String codeLite;

    // 供应商ID（多个）
    private List<Integer> supplier;

    // 排序方式
    private List<String> orderBy;

    // 查询时间类型
    private String searchDateType;

    // 大于等于某个时间类型
    private LocalDateTime dateFor;

    // 小于等于某个时间类型
    private LocalDateTime dateTo;

    // 当前页
    private Integer page;

    // 每页显示条数
    private Integer pageSize;

}
