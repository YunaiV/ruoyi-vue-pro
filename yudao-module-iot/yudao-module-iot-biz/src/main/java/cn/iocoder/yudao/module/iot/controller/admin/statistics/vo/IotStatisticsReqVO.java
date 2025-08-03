package cn.iocoder.yudao.module.iot.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 统计 Request VO")
@Data
public class IotStatisticsReqVO {

    // TODO @super：前端传递的时候，还是通过 startTime 和 endTime 传递。后端转成 Long

    @Schema(description = "查询起始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1658486600000")
    @NotNull(message = "查询起始时间不能为空")
    private Long startTime;

    @Schema(description = "查询结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1758486600000")
    @NotNull(message = "查询结束时间不能为空")
    private Long endTime;

}
