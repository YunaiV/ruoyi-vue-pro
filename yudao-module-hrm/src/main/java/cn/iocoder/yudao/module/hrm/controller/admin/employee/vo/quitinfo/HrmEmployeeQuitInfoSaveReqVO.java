package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.quitinfo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeQuitReasonEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeQuitTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工离职信息保存 Request VO")
@Data
public class HrmEmployeeQuitInfoSaveReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "计划离职时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划离职时间不能为空")
    private LocalDateTime planQuitTime;

    @Schema(description = "申请离职时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请离职时间不能为空")
    private LocalDateTime applyQuitTime;

    @Schema(description = "薪资结算时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "薪资结算时间不能为空")
    private LocalDateTime salarySettlementTime;

    @Schema(description = "离职类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "离职类型不能为空")
    @InEnum(value = HrmEmployeeQuitTypeEnum.class, message = "离职类型必须是 {value}")
    private Integer type;

    @Schema(description = "离职原因", example = "1")
    @InEnum(value = HrmEmployeeQuitReasonEnum.class, message = "离职原因必须是 {value}")
    private Integer reason;

    @Schema(description = "备注", example = "个人原因离职")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

    @Schema(description = "离职前员工状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "离职前员工状态不能为空")
    @InEnum(value = HrmEmployeeStatusEnum.class, message = "离职前员工状态必须是 {value}")
    private Integer oldEmployeeStatus;

}
