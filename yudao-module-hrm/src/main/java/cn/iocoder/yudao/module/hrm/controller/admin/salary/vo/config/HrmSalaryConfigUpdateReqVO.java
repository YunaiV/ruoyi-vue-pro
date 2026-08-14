package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalarySocialSecurityMonthTypeEnum;
import cn.iocoder.yudao.module.hrm.framework.operatelog.core.HrmSalarySocialSecurityMonthTypeParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 计薪配置更新 Request VO")
@Data
public class HrmSalaryConfigUpdateReqVO {

    @Schema(description = "社保对应月份类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "社保对应月份类型不能为空")
    @InEnum(value = HrmSalarySocialSecurityMonthTypeEnum.class, message = "社保对应月份类型必须是 {value}")
    @DiffLogField(name = "社保对应月份", function = HrmSalarySocialSecurityMonthTypeParseFunction.NAME)
    private Integer socialSecurityMonthType;

}
