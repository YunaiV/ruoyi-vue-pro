package com.somle.eccang.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @className: InventoryBatchLogVO
 * @author: Wqh
 * @date: 2024/12/20 9:29
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangInventoryBatchLogVO {
    // 仓库代码如:["BALLLYHA1-01","HAOBA"]
    private List<String> warehouseCode;

    // 调整值0:全部1:正数2:负数
    private Integer adjustmentType;

    // 操作类型code
    private String applicationCode;

    // 操作单号可输入多个,每个以空格隔开
    private String refNo;

    // 入库单号
    private String receivingCode;

    // 库位可输入多个,每个以空格隔开
    private String lcCode;

    // 产品代码搜索类型0:精确1:模糊
    private Integer skuType;

    // 产品代码可输入多个,每个以空格隔开
    private String productCode;

    // 采购负责人
    private Integer buyerId;

    // 操作人id
    private Integer userId;

    // 操作时间--开始时间(格式为2021-01-01或 2021-01-01 00:00或 2021-01-01 00:00:00)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateFrom;

    // 操作时间--截止时间(格式为2021-01-01或 2021-01-01 00:00或 2021-01-01 00:00:00)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTo;

    // 是否需报关(0否、1是)
    private Integer isNeedDeclare;

    // 当前页
    private Integer page;

    // 每页条数,最大1000
    private Integer pageSize;
}
