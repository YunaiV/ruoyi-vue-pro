package cn.iocoder.yudao.module.im.controller.admin.manager.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IM 数据看板群规模分布项 Response VO")
@Data
public class ImStatisticsManagerGroupSizeRespVO {

    @Schema(description = "区间名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1-9 人")
    private String range;

    @Schema(description = "群数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "320")
    private Long count;

}
