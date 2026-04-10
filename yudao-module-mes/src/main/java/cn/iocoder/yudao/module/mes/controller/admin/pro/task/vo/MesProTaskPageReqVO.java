package cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 生产任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesProTaskPageReqVO extends PageParam {

    @Schema(description = "任务编码", example = "PT202503150001")
    private String code;

    @Schema(description = "任务名称", example = "注塑任务")
    private String name;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "工艺路线编号", example = "1")
    private Long routeId;

    @Schema(description = "工序编号", example = "1")
    private Long processId;

    @Schema(description = "工作站编号", example = "1")
    private Long workstationId;

    @Schema(description = "任务状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
