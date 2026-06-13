package cn.iocoder.yudao.module.wms.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - WMS 首页单据汇总统计 Response VO")
@Data
public class WmsHomeOrderSummaryRespVO {

    @Schema(description = "单据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "单据总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long total;

    @Schema(description = "状态分布")
    private List<WmsHomeOrderStatusRespVO> statuses;

}
