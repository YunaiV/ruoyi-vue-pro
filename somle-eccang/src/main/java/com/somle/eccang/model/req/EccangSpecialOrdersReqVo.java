package com.somle.eccang.model.req;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

//getSpecialOrdersList 请求体
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangSpecialOrdersReqVo {

    // 必填字段
    @JsonProperty("pageSize")
    private int pageSize; // 每页数据长度，最大值100
    private int page;     // 当前页

    // 可选字段
    private List<String> codeArr; // 单号数组：['R001', 'R002']
    private Integer spoStatus;    // 状态 0:已作废 1:待确认 2:在途 3:到货 4:到货异常 5:已完成
    private Integer spoType;      // 退件类型 1:买家退件 2:物流退件 3:认领
    private String warehouseCode; // 仓库代码
    private Integer spoStorageType; // 入库类型：1:仓内 2:外仓 3:两者均有
    private Integer spoProcessType; // 处理类型：1:重新上架销售 2:退回国内 3:不良品 4:销毁 5:待检查 6:换标 8:产品升级
    private String searchType;    // 搜索类型：searchCode退件单号 searchSku搜索SKU
    private String code;          // 搜索值，配合上面的searchType条件使用，代表对应类型的单号或者sku代码
    private Integer stockCheckType; // 库存类型：0标准 1:不良 2:暂存
    private Integer spoAttr;      // 退件标识：1:标准退件 2:回邮退件
    private Integer spoLabelServices; // Label服务 1:是 0:否
    private String spoDescLike;   // 退件原因,模糊搜索

    // 时间范围字段
    private LocalDateTime spoAddTimeFrom;  // 创建开始时间，格式YYYY-MM-DD HH:II:SS
    private LocalDateTime spoAddTimeTo;    // 创建结束时间，格式YYYY-MM-DD HH:II:SS
    private LocalDateTime spoConfirmTimeFrom; // 审核开始时间，格式YYYY-MM-DD HH:II:SS
    private LocalDateTime spoConfirmTimeTo;   // 审核结束时间，格式YYYY-MM-DD HH:II:SS
    private LocalDateTime spoCompleteTimeFrom; // 完成开始时间，格式YYYY-MM-DD HH:II:SS
    private LocalDateTime spoCompleteTimeTo;   // 完成结束时间，格式YYYY-MM-DD HH:II:SS
    private LocalDateTime spoUpdateTimeFrom;   // 更新开始时间，格式YYYY-MM-DD HH:II:SS
    private LocalDateTime spoUpdateTimeTo;     // 更新结束时间，格式YYYY-MM-DD HH:II:SS
}
