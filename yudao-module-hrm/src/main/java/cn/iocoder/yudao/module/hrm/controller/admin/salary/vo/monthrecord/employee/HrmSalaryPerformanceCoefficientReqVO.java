package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 绩效系数查询 Request VO")
@Data
public class HrmSalaryPerformanceCoefficientReqVO {

    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "年份不能为空")
    private Integer year;

    @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "月份不能为空")
    private Integer month;

    @Schema(description = "员工编号列表", example = "[1024, 1025]")
    private List<Long> employeeIds;

}
