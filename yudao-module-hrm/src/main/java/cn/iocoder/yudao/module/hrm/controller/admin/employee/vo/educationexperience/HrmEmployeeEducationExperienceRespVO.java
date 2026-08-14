package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.educationexperience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工教育经历 Response VO")
@Data
public class HrmEmployeeEducationExperienceRespVO {

    @Schema(description = "教育经历编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long employeeId;

    @Schema(description = "学历", example = "8")
    private Integer education;

    @Schema(description = "毕业院校", example = "浙江大学")
    private String graduateSchool;

    @Schema(description = "专业", example = "软件工程")
    private String major;

    @Schema(description = "入学日期")
    private LocalDateTime admissionTime;

    @Schema(description = "毕业日期")
    private LocalDateTime graduationTime;

    @Schema(description = "教学方式", example = "1")
    private Integer teachingMethods;

    @Schema(description = "是否第一学历", example = "true")
    private Boolean firstDegree;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
