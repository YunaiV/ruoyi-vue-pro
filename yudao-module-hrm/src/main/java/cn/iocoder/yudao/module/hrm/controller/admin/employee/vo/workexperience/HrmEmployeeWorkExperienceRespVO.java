package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.workexperience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工工作经历 Response VO")
@Data
public class HrmEmployeeWorkExperienceRespVO {

    @Schema(description = "工作经历编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long employeeId;

    @Schema(description = "工作单位", example = "示例科技")
    private String workUnit;

    @Schema(description = "职务", example = "开发工程师")
    private String postName;

    @Schema(description = "工作开始日期")
    private LocalDateTime startTime;

    @Schema(description = "工作结束日期")
    private LocalDateTime endTime;

    @Schema(description = "离职原因")
    private String reason;

    @Schema(description = "证明人")
    private String witnessName;

    @Schema(description = "证明人手机号")
    private String witnessPhone;

    @Schema(description = "工作备注")
    private String remark;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
