package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 盘点方案 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmStockTakingPlanRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "方案编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "STP202603080001")
    @ExcelProperty("方案编码")
    private String code;

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "原料仓月度盘点方案")
    @ExcelProperty("方案名称")
    private String name;

    @Schema(description = "盘点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("盘点类型")
    private Integer type;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalDateTime startTime;

    @Schema(description = "计划结束时间")
    @ExcelProperty("计划结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否盲盘", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @ExcelProperty("是否盲盘")
    private Boolean blindFlag;

    @Schema(description = "是否冻结库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @ExcelProperty("是否冻结库存")
    private Boolean frozen;

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
