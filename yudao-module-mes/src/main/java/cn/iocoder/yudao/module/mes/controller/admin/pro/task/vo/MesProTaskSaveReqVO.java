package cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产任务新增/修改 Request VO")
@Data
public class MesProTaskSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId; // 编辑时，可不传递

    @Schema(description = "工作站编号", example = "1")
    private Long workstationId; // 编辑时，可不传递

    @Schema(description = "工艺路线编号", example = "1")
    private Long routeId; // 编辑时，可不传递

    @Schema(description = "工序编号", example = "1")
    private Long processId; // 编辑时，可不传递

    @Schema(description = "产品物料编号", example = "100")
    private Long itemId; // 编辑时，可不传递

    @Schema(description = "排产数量", example = "100.00")
    @DecimalMin(value = "0", inclusive = false, message = "排产数量必须大于 0")
    private BigDecimal quantity; // 编辑时，可不传递

    @Schema(description = "开始生产时间")
    private LocalDateTime startTime;

    @Schema(description = "生产时长（工作日）", example = "3")
    @Min(value = 1, message = "生产时长必须大于 0")
    private Integer duration;

    @Schema(description = "结束生产时间")
    private LocalDateTime endTime;

    @Schema(description = "甘特图显示颜色", example = "#00AEF3")
    private String colorCode;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
