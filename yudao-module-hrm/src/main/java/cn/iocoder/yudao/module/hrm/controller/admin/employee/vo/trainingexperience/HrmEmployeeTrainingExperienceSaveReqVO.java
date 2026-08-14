package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.trainingexperience;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isAfterOrEqual;

@Schema(description = "管理后台 - HRM 员工培训经历保存 Request VO")
@Data
public class HrmEmployeeTrainingExperienceSaveReqVO {

    @Schema(description = "培训经历编号", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "培训课程", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目管理")
    @NotBlank(message = "培训课程不能为空")
    @Size(max = 128, message = "培训课程长度不能超过 128 个字符")
    @DiffLogField(name = "培训课程")
    private String course;

    @Schema(description = "培训机构名称", example = "内部学院")
    @Size(max = 128, message = "培训机构名称长度不能超过 128 个字符")
    @DiffLogField(name = "培训机构名称")
    private String organizationName;

    @Schema(description = "培训开始日期")
    @DiffLogField(name = "培训开始日期")
    private LocalDateTime startTime;

    @Schema(description = "培训结束日期")
    @DiffLogField(name = "培训结束日期")
    private LocalDateTime endTime;

    @Schema(description = "培训时长", example = "16 小时")
    @Size(max = 64, message = "培训时长长度不能超过 64 个字符")
    @DiffLogField(name = "培训时长")
    private String duration;

    @Schema(description = "培训成绩", example = "优秀")
    @Size(max = 64, message = "培训成绩长度不能超过 64 个字符")
    @DiffLogField(name = "培训成绩")
    private String result;

    @Schema(description = "培训证书名称", example = "PMP")
    @Size(max = 128, message = "培训证书名称长度不能超过 128 个字符")
    @DiffLogField(name = "培训证书名称")
    private String certificateName;

    @Schema(description = "备注", example = "公司内部培训")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "排序", example = "1")
    @DiffLogField(name = "排序")
    private Integer sort;

    @AssertTrue(message = "培训经历的结束日期不能早于开始日期")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || isAfterOrEqual(endTime, startTime);
    }

}
