package com.somle.eccang.model.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

//getReceivingDetailList 请求体
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangReceivingDetailReqVO {
    // 必填字段
    private LocalDateTime dateFor;  // 入库单开始日期
    private LocalDateTime dateTo;    // 入库单截止日期

    // 可选字段
    private String receivingCode;          // 入库单号
    private List<Integer> warehouseIds;    // 仓库ID数组
    private List<String> warehouseCodes;   // 仓库代码数组(最大1000个元素)
    private String productBarcode;         // 产品代码
    private Integer productBarcodeType;    // 产品代码查询方式(1:模糊,0:精确)
    private String operationUserType;      // 产品负责人类型
    private String applicationCode;        // 操作类型
    private Integer personId;              // 产品负责人ID
    private Integer productCategoryId;     // 产品品类ID

    // 分页相关
    private Integer page;             // 当前页，默认1
    private Integer pageSize;         // 每页显示条数，默认50，最大100
    private Integer rlId;              // 上一次分页的返回值，默认0
}
