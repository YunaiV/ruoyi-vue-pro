package cn.iocoder.yudao.module.iot.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - Iot统计 Request VO")
@Data
public class IotStatisticsReqVO {

    // TODO @supper：times 直接传递哈；
    // TODO 2super：private 不要丢了

    @Schema(description = "查询起始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1658486600000")
    @NotNull(message = "查询起始时间不能为空")
    private Long startTime;

    @Schema(description = "查询结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1758486600000")
    @NotNull(message = "查询结束时间不能为空")
    private Long endTime;

}
