package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 从后台用户创建员工 Request VO")
@Data
public class HrmEmployeeCreateFromUserReqVO {

    @Schema(description = "后台用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "后台用户编号不能为空")
    private Long userId;

    @Schema(description = "工号", requiredMode = Schema.RequiredMode.REQUIRED, example = "HRM001")
    @NotBlank(message = "工号不能为空")
    @Size(max = 64, message = "工号不能超过 64 个字符")
    private String jobNumber;

    @Schema(description = "员工手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    @NotBlank(message = "员工手机号不能为空")
    @Mobile
    private String mobile;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "直属上级员工编号", example = "1")
    private Long leaderEmployeeId;

    @Schema(description = "聘用形式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "聘用形式不能为空")
    @InEnum(value = HrmEmployeeTypeEnum.class, message = "聘用形式必须是 {value}")
    private Integer type;

    @Schema(description = "员工状态；非正式员工必填", example = "3")
    @InEnum(value = HrmEmployeeStatusEnum.class, message = "员工状态必须是 {value}")
    private Integer status;

    @Schema(description = "入职时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入职时间不能为空")
    private LocalDateTime entryTime;

    @Schema(description = "试用期，单位：月；正式员工必填", example = "3")
    @Min(value = 0, message = "试用期不能小于 0")
    @Max(value = 6, message = "试用期不能超过 6 个月")
    private Integer probation;

    @Schema(description = "职位名称", example = "Java 工程师")
    @Size(max = 255, message = "职位名称不能超过 255 个字符")
    private String postName;

    @Schema(description = "岗位职级", example = "P6")
    @Size(max = 255, message = "岗位职级不能超过 255 个字符")
    private String postLevel;

    @Schema(description = "工作城市", example = "杭州")
    @Size(max = 64, message = "工作城市不能超过 64 个字符")
    private String workCity;

    @Schema(description = "工作地点", example = "西湖区")
    @Size(max = 255, message = "工作地点不能超过 255 个字符")
    private String workAddress;

    @Schema(description = "备注", example = "从后台用户批量建档")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

    @AssertTrue(message = "正式员工试用期不能为空")
    @JsonIgnore
    public boolean isProbationValid() {
        return ObjUtil.notEqual(HrmEmployeeTypeEnum.FORMAL.getType(), type) || probation != null;
    }

    @AssertTrue(message = "非正式员工状态不能为空")
    @JsonIgnore
    public boolean isStatusRequired() {
        return ObjUtil.notEqual(HrmEmployeeTypeEnum.INFORMAL.getType(), type) || status != null;
    }

    @AssertTrue(message = "员工状态与聘用形式不匹配")
    @JsonIgnore
    public boolean isStatusValid() {
        return ObjUtil.notEqual(HrmEmployeeTypeEnum.INFORMAL.getType(), type)
                || status == null || HrmEmployeeStatusEnum.INFORMAL_STATUSES.contains(status);
    }

}
