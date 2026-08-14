package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeReasonEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工转为全职 Request VO")
@Data
public class HrmEmployeeConvertToFullTimeReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "异动原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "异动原因不能为空")
    @InEnum(value = HrmEmployeeChangeReasonEnum.class, message = "异动原因必须是 {value}")
    private Integer reason;

    @Schema(description = "试用期，单位：月；0 表示无试用期", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "3")
    @NotNull(message = "试用期不能为空")
    @Min(value = 0, message = "试用期不能小于 0")
    @Max(value = 6, message = "试用期不能超过 6 个月")
    private Integer probation;

    @Schema(description = "新部门编号；未填写表示不变", example = "200")
    private Long newDeptId;

    @Schema(description = "新岗位名称；未填写表示不变", example = "Java 工程师")
    @Size(max = 255, message = "新岗位名称不能超过 255 个字符")
    private String newPostName;

    @Schema(description = "新职级；未填写表示不变", example = "P6")
    @Size(max = 255, message = "新职级不能超过 255 个字符")
    private String newPostLevel;

    @Schema(description = "新工作地点；未填写表示不变", example = "上海")
    @Size(max = 255, message = "新工作地点不能超过 255 个字符")
    private String newWorkAddress;

    @Schema(description = "新直属上级员工编号；未填写表示不变", example = "3")
    private Long newLeaderEmployeeId;

    @Schema(description = "生效日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生效日期不能为空")
    private LocalDateTime effectTime;

    @Schema(description = "备注", example = "转为全职员工")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

}
