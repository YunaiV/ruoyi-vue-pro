package cn.iocoder.yudao.module.hrm.controller.admin.portal.salary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "管理后台 - HRM 员工端工资条列表 Request VO")
@Data
public class HrmPortalSalarySlipListReqVO {

    @Schema(description = "开始月份", example = "2026-01")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "开始月份格式必须为 yyyy-MM")
    private String startMonth;

    @Schema(description = "结束月份", example = "2026-12")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "结束月份格式必须为 yyyy-MM")
    private String endMonth;

    @Schema(description = "排序类型", example = "1")
    @Range(min = 1, max = 2, message = "排序类型必须在 1 到 2 之间")
    private Integer orderType;

    @Schema(description = "排序方向", example = "1")
    @Range(min = 1, max = 2, message = "排序方向必须在 1 到 2 之间")
    private Integer order;

}
