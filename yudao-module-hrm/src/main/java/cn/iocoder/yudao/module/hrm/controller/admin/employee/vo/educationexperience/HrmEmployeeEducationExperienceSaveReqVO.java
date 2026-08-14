package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.educationexperience;

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

@Schema(description = "管理后台 - HRM 员工教育经历保存 Request VO")
@Data
public class HrmEmployeeEducationExperienceSaveReqVO {

    @Schema(description = "教育经历编号", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "学历", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    @NotNull(message = "学历不能为空")
    @DiffLogField(name = "学历")
    private Integer education;

    @Schema(description = "毕业院校", requiredMode = Schema.RequiredMode.REQUIRED, example = "浙江大学")
    @NotBlank(message = "毕业院校不能为空")
    @Size(max = 255, message = "毕业院校长度不能超过 255 个字符")
    @DiffLogField(name = "毕业院校")
    private String graduateSchool;

    @Schema(description = "专业", requiredMode = Schema.RequiredMode.REQUIRED, example = "软件工程")
    @NotBlank(message = "专业不能为空")
    @Size(max = 255, message = "专业长度不能超过 255 个字符")
    @DiffLogField(name = "专业")
    private String major;

    @Schema(description = "入学日期")
    @DiffLogField(name = "入学日期")
    private LocalDateTime admissionTime;

    @Schema(description = "毕业日期")
    @DiffLogField(name = "毕业日期")
    private LocalDateTime graduationTime;

    @Schema(description = "教学方式", example = "1")
    @DiffLogField(name = "教学方式")
    private Integer teachingMethods;

    @Schema(description = "是否第一学历", example = "true")
    @DiffLogField(name = "是否第一学历")
    private Boolean firstDegree;

    @Schema(description = "排序", example = "1")
    @DiffLogField(name = "排序")
    private Integer sort;

    @AssertTrue(message = "毕业日期不能早于入学日期")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return admissionTime == null || graduationTime == null
                || isAfterOrEqual(graduationTime, admissionTime);
    }

}
