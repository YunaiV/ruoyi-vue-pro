package cn.iocoder.yudao.module.wms.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - WMS 首页单据状态统计 Response VO")
@Data
public class WmsHomeOrderStatusRespVO {

    @Schema(description = "状态值", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "单据数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long count;

}
