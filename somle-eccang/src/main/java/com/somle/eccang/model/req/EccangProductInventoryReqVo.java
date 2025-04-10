package com.somle.eccang.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangProductInventoryReqVo {
    @JsonProperty("pageSize")
    private Integer pageSize;          // 每页数据长度(最大2000)
    private Integer page;             // 当前页
    private String productSku;        // SKU
    private List<String> productSkuArr; // 多个SKU,数组格式
    private String warehouseCode;     // 仓库代码
    private List<String> warehouseCodeArr; // 多个仓库代码,数组格式
    private String updateStartTime;   // 最新修改时间
    private Integer isWarning;        // 是否低于预留库存（0全部 1是 2否）
    private String productTitle;      // 产品名称
    private Integer searchType;       // 搜索类型（1精准，0模糊）
}
