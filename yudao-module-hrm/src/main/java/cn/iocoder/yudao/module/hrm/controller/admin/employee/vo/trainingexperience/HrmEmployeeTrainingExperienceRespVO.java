package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.trainingexperience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工培训经历 Response VO")
@Data
public class HrmEmployeeTrainingExperienceRespVO {

    @Schema(description = "培训经历编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long employeeId;

    @Schema(description = "培训课程", example = "项目管理")
    private String course;

    @Schema(description = "培训机构名称", example = "内部学院")
    private String organizationName;

    @Schema(description = "培训开始日期")
    private LocalDateTime startTime;

    @Schema(description = "培训结束日期")
    private LocalDateTime endTime;

    @Schema(description = "培训时长", example = "16 小时")
    private String duration;

    @Schema(description = "培训成绩", example = "优秀")
    private String result;

    @Schema(description = "培训证书名称", example = "PMP")
    private String certificateName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
