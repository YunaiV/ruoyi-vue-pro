package com.somle.eccang.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import java.util.List;

//getAsnList 请求体
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangAsnListReqVo {

    // 必填字段
    @JsonProperty("pageSize")
    private Integer pageSize; // 每页数据长度
    private Integer page; // 当前页

    private Integer receivingId; // 入库单ID

    private String receivingCode; // 入库单号

    private List<String> receivingCodeArr; // 多个入库单号,数组格式

    private String referenceNo; // 参考号

    private List<String> referenceNoArr; // 多个参考号,数组格式

    private String createDateFrom; // 起始时间(创建时间,有入库单号的时候此参数失效)

    private String createDateTo; // 结束时间(创建时间,有入库单号的时候此参数失效)

    private String modifyDateFrom; // 起始时间(修改时间,有入库单号的时候此参数失效)

    private String modifyDateTo; // 结束时间(修改时间,有入库单号的时候此参数失效)

    private Integer businessType; // 保税用户填1,其它可不填

    private Integer isGetInventoryCode; // 是否返回库存批次号字段：0否，1是；默认0
}
