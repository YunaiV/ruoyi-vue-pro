package com.somle.eccang.model.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangDeliverBatchReqVO {
    // 入库控件集合（多个）
    private List<String> shipmentIdArr;

    // 发送单号集合（多个）
    private List<String> doCodeArr;

    // 创建时间-开始（大于等于）
    private LocalDateTime createdStart;

    // 创建时间-结束（小于等于）
    private LocalDateTime createdEnd;

    // 出库时间-开始（大于等于）
    private LocalDateTime shipStart;

    // 出库时间-结束（小于等于）
    private LocalDateTime shipEnd;

    // 更新时间-开始（大于等于）
    private LocalDateTime updatedStart;

    // 更新时间-结束（小于等于）
    private LocalDateTime updatedEnd;

    // 状态：1:待确认，2:待发送，3:已发送，4:作废
    private Integer orderStatus;

    // 是否标记完成（0-否，1-是）
    private String isMarkComplete;

    // 页码（默认1）
    private Integer page;

    // 每页数量（默认20，最大50）
    private Integer pageSize;
}
