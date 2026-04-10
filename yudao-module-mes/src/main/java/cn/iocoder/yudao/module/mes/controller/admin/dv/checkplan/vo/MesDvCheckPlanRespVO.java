package cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 点检保养方案 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesDvCheckPlanRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "方案编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "CHP001")
    @ExcelProperty("方案编码")
    private String code;

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "注塑机日检方案")
    @ExcelProperty("方案名称")
    private String name;

    @Schema(description = "方案类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("方案类型")
    private Integer type;

    @Schema(description = "开始日期")
    @ExcelProperty("开始日期")
    private LocalDateTime startDate;

    @Schema(description = "结束日期")
    @ExcelProperty("结束日期")
    private LocalDateTime endDate;

    @Schema(description = "周期类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("周期类型")
    private Integer cycleType;

    @Schema(description = "周期数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("周期数量")
    private Integer cycleCount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
