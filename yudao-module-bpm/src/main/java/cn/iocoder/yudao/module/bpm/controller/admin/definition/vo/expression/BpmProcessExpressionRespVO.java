package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.expression;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - BPM 流程表达式 Response VO")
@Data
public class BpmProcessExpressionRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3870")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "表达式名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("表达式名字")
    private String name;

    @Schema(description = "表达式状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expression;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}