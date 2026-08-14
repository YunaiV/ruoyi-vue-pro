package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.beforeOrEqualNow;

@Schema(description = "管理后台 - HRM 员工再入职 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmEmployeeRehireReqVO extends HrmEmployeeSaveReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @AssertTrue(message = "入职时间不能晚于当前时间")
    @JsonIgnore
    public boolean isEntryTimeValid() {
        return getEntryTime() == null || beforeOrEqualNow(getEntryTime());
    }

}
