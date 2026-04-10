package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 盘点任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmStockTakingTaskPageReqVO extends PageParam {

    @Schema(description = "任务编码", example = "ST202603080001")
    private String code;

    @Schema(description = "任务名称", example = "原料仓月度盘点任务")
    private String name;

    @Schema(description = "盘点类型", example = "1")
    private Integer type;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "盘点人 ID", example = "1")
    private Long userId;

    @Schema(description = "来源方案 ID", example = "1")
    private Long planId;

    @Schema(description = "盘点日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] takingDate;

}
