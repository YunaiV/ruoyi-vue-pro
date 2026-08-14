package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - HRM 工资条项 Response VO")
@Data
public class HrmSalarySlipOptionRespVO {

    @Schema(description = "薪资工资条选项名称")
    private String name;

    @Schema(description = "薪资工资条选项类型")
    private Integer type;

    @Schema(description = "编码")
    private Integer code;

    @Schema(description = "值")
    private BigDecimal value;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子工资条项")
    private List<HrmSalarySlipOptionRespVO> children;

}
