package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 盘点任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmStockTakingTaskRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "任务编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ST202603080001")
    @ExcelProperty("任务编码")
    private String code;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "原料仓月度盘点任务")
    @ExcelProperty("任务名称")
    private String name;

    @Schema(description = "盘点日期")
    @ExcelProperty("盘点日期")
    private LocalDateTime takingDate;

    @Schema(description = "盘点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("盘点类型")
    private Integer type;

    @Schema(description = "盘点人 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "盘点人昵称", example = "管理员")
    @ExcelProperty("盘点人")
    private String userNickname;

    @Schema(description = "来源方案 ID", example = "1")
    private Long planId;

    @Schema(description = "来源方案编码", example = "STP202603080001")
    @ExcelProperty("方案编码")
    private String planCode;

    @Schema(description = "来源方案名称", example = "原料仓月度盘点方案")
    @ExcelProperty("方案名称")
    private String planName;

    @Schema(description = "是否盲盘", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @ExcelProperty("是否盲盘")
    private Boolean blindFlag;

    @Schema(description = "是否冻结库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @ExcelProperty("是否冻结库存")
    private Boolean frozen;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
