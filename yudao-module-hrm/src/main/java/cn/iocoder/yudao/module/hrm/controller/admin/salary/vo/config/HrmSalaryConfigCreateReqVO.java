package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalarySocialSecurityMonthTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 计薪配置创建 Request VO")
@Data
public class HrmSalaryConfigCreateReqVO {

    @Schema(description = "计薪周期开始日", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计薪周期开始日不能为空")
    @Min(value = 1, message = "计薪周期开始日必须在 1 到 31 之间")
    @Max(value = 31, message = "计薪周期开始日必须在 1 到 31 之间")
    private Integer cycleStartDay;

    @Schema(description = "社保对应月份类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "社保对应月份类型不能为空")
    @InEnum(value = HrmSalarySocialSecurityMonthTypeEnum.class, message = "社保对应月份类型必须是 {value}")
    private Integer socialSecurityMonthType;

    @Schema(description = "工资开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "工资开始年份不能为空")
    @Min(value = 2000, message = "工资开始年份不能小于 2000")
    @Max(value = 2100, message = "工资开始年份不能大于 2100")
    private Integer startYear;

    @Schema(description = "工资开始月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "工资开始月份不能为空")
    @Min(value = 1, message = "工资开始月份必须在 1 到 12 之间")
    @Max(value = 12, message = "工资开始月份必须在 1 到 12 之间")
    private Integer startMonth;

}
