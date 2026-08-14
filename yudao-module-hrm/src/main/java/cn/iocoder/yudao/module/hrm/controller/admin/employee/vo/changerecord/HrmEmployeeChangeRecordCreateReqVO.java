package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeReasonEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工异动记录创建 Request VO")
@Data
public class HrmEmployeeChangeRecordCreateReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "异动类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "异动类型不能为空")
    @InEnum(value = HrmEmployeeChangeTypeEnum.class, message = "异动类型必须是 {value}")
    private Integer type;

    @Schema(description = "异动原因", example = "1")
    @InEnum(value = HrmEmployeeChangeReasonEnum.class, message = "异动原因必须是 {value}")
    private Integer reason;

    @Schema(description = "原部门编号", example = "100")
    private Long oldDeptId;

    @Schema(description = "原岗位名称", example = "Java 工程师")
    @Size(max = 255, message = "原岗位名称不能超过 255 个字符")
    private String oldPostName;

    @Schema(description = "原职级", example = "P5")
    @Size(max = 255, message = "原职级不能超过 255 个字符")
    private String oldPostLevel;

    @Schema(description = "原工作地点", example = "杭州")
    @Size(max = 255, message = "原工作地点不能超过 255 个字符")
    private String oldWorkAddress;

    @Schema(description = "原直属上级员工编号", example = "2")
    private Long oldLeaderEmployeeId;

    @Schema(description = "新部门编号", example = "200")
    private Long newDeptId;

    @Schema(description = "新岗位名称", example = "高级 Java 工程师")
    @Size(max = 255, message = "新岗位名称不能超过 255 个字符")
    private String newPostName;

    @Schema(description = "新职级", example = "P6")
    @Size(max = 255, message = "新职级不能超过 255 个字符")
    private String newPostLevel;

    @Schema(description = "新工作地点", example = "上海")
    @Size(max = 255, message = "新工作地点不能超过 255 个字符")
    private String newWorkAddress;

    @Schema(description = "新直属上级员工编号", example = "3")
    private Long newLeaderEmployeeId;

    @Schema(description = "试用期，单位月", example = "3")
    @Min(value = 0, message = "试用期不能小于 0")
    @Max(value = 6, message = "试用期不能超过 6 个月")
    private Integer probation;

    @Schema(description = "生效时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime effectTime;

    @Schema(description = "备注", example = "年度晋升")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

}
