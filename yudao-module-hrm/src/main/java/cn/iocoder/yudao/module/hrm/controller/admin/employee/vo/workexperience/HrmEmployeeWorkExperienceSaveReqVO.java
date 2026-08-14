package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.workexperience;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isAfterOrEqual;

@Schema(description = "管理后台 - HRM 员工工作经历保存 Request VO")
@Data
public class HrmEmployeeWorkExperienceSaveReqVO {

    @Schema(description = "工作经历编号", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "工作单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例科技")
    @NotBlank(message = "工作单位不能为空")
    @Size(max = 255, message = "工作单位长度不能超过 255 个字符")
    @DiffLogField(name = "工作单位")
    private String workUnit;

    @Schema(description = "职务", requiredMode = Schema.RequiredMode.REQUIRED, example = "开发工程师")
    @NotBlank(message = "职务不能为空")
    @Size(max = 255, message = "职务长度不能超过 255 个字符")
    @DiffLogField(name = "职务")
    private String postName;

    @Schema(description = "工作开始日期")
    @DiffLogField(name = "工作开始日期")
    private LocalDateTime startTime;

    @Schema(description = "工作结束日期")
    @DiffLogField(name = "工作结束日期")
    private LocalDateTime endTime;

    @Schema(description = "离职原因", example = "职业发展")
    @Size(max = 1024, message = "离职原因长度不能超过 1024 个字符")
    @DiffLogField(name = "离职原因")
    private String reason;

    @Schema(description = "证明人", example = "李四")
    @Size(max = 255, message = "证明人长度不能超过 255 个字符")
    @DiffLogField(name = "证明人")
    private String witnessName;

    @Schema(description = "证明人手机号", example = "15601691301")
    @Size(max = 32, message = "证明人手机号长度不能超过 32 个字符")
    @DiffLogField(name = "证明人手机号")
    private String witnessPhone;

    @Schema(description = "工作备注", example = "负责核心业务研发")
    @Size(max = 500, message = "工作备注长度不能超过 500 个字符")
    @DiffLogField(name = "工作备注")
    private String remark;

    @Schema(description = "排序", example = "1")
    @DiffLogField(name = "排序")
    private Integer sort;

    @AssertTrue(message = "工作经历的结束日期不能早于开始日期")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || isAfterOrEqual(endTime, startTime);
    }

}
