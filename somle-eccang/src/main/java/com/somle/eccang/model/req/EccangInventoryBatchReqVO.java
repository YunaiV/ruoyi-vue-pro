package com.somle.eccang.model.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;


@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//eccang-InventoryBatch请求体
public class EccangInventoryBatchReqVO {
    // 产品SKU，多个时使用空格隔开
    private String productSku;

    // 产品SKU模糊查询
    private String productSkuLike;

    // 产品名称
    private String productTitle;

    // 产品名称模糊查询
    private String productTitleLike;

    // 库位，多个时使用空格隔开
    private String lcCode;

    // 锁状态：0 无，1 盘点锁，2 借领用锁
    private Integer holdStatus;

    // 库存类型：0 标准，1 不良品
    private Integer type;

    // 仓库ID
    private Integer warehouseId;

    // 上架时间-开始时间 (格式：YYYY-MM-DD)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fifoTimeFrom;

    // 上架时间-截止时间 (格式：YYYY-MM-DD)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fifoTimeTo;

    // 更新时间-开始时间 (格式：YYYY-MM-DD)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ibUpdateTimeFrom;

    // 更新时间-截止时间 (格式：YYYY-MM-DD)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ibUpdateTimeTo;

    // 当前页
    private Integer page = 1;

    // 每页条数，最大1000
    private Integer pageSize = 50;

    // 主键ID，传入上一页返回的ib_id，第一页可以传0
    private Integer ibId;
}
