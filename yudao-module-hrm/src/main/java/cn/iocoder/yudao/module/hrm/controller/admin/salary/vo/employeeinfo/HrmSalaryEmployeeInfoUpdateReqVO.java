package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeReasonEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工薪资信息修改 Request VO")
@Data
public class HrmSalaryEmployeeInfoUpdateReqVO {

    @Schema(description = "定薪/调薪记录编号；编辑待生效或已取消记录时传入", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工不能为空")
    private Long employeeId;

    @Schema(description = "记录类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "记录类型不能为空")
    @InEnum(value = HrmSalaryChangeRecordTypeEnum.class, message = "记录类型必须是 {value}")
    private Integer recordType;

    @Schema(description = "调整原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "调整原因不能为空")
    @InEnum(value = HrmSalaryChangeReasonEnum.class, message = "调整原因必须是 {value}")
    private Integer changeReason;

    @Schema(description = "生效日期")
    private LocalDateTime effectTime;

    @Schema(description = "备注", example = "年度调薪")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

    @Schema(description = "薪资项列表")
    @Valid
    private List<HrmSalaryOptionValueVO> salaryOptions;

    @Schema(description = "试用期薪资项列表")
    @Valid
    private List<HrmSalaryOptionValueVO> probationSalaryOptions;

}
