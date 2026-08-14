package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contract;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeContractStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeContractTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isAfterOrEqual;

@Schema(description = "管理后台 - HRM 员工合同保存 Request VO")
@Data
public class HrmEmployeeContractSaveReqVO {

    @Schema(description = "合同编号", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "合同编号", example = "HT-2026-001")
    @Size(max = 128, message = "合同编号长度不能超过 128 个字符")
    @DiffLogField(name = "合同编号")
    private String no;

    @Schema(description = "合同类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "合同类型不能为空")
    @InEnum(value = HrmEmployeeContractTypeEnum.class, message = "合同类型必须是 {value}")
    @DiffLogField(name = "合同类型")
    private Integer type;

    @Schema(description = "合同开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合同开始日期不能为空")
    @DiffLogField(name = "合同开始日期")
    private LocalDateTime startTime;

    @Schema(description = "合同结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合同结束日期不能为空")
    @DiffLogField(name = "合同结束日期")
    private LocalDateTime endTime;

    @Schema(description = "期限，单位：年", example = "3")
    @Min(value = 1, message = "合同期限不能小于 1 年")
    @Max(value = 10, message = "合同期限不能超过 10 年")
    @DiffLogField(name = "合同期限")
    private Integer term;

    @Schema(description = "合同状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "合同状态不能为空")
    @InEnum(value = HrmEmployeeContractStatusEnum.class, message = "合同状态必须是 {value}")
    @DiffLogField(name = "合同状态")
    private Integer status;

    @Schema(description = "签约公司", example = "示例科技有限公司")
    @Size(max = 255, message = "签约公司长度不能超过 255 个字符")
    @DiffLogField(name = "签约公司")
    private String signCompany;

    @Schema(description = "合同签订日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合同签订日期不能为空")
    @DiffLogField(name = "合同签订日期")
    private LocalDateTime signTime;

    @Schema(description = "备注", example = "首次签订劳动合同")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "是否到期提醒", example = "true")
    @DiffLogField(name = "是否到期提醒")
    private Boolean expireRemind;

    @Schema(description = "附件地址数组", example = "[https://example.com/contract.pdf]")
    @DiffLogField(name = "附件")
    private List<String> fileUrls;

    @Schema(description = "排序", example = "1")
    @DiffLogField(name = "排序")
    private Integer sort;

    @AssertTrue(message = "合同结束日期不能早于开始日期")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || isAfterOrEqual(endTime, startTime);
    }

    @AssertTrue(message = "合同期限不能为空")
    @JsonIgnore
    public boolean isTermValid() {
        return type == null
                || HrmEmployeeContractTypeEnum.NON_FIXED_TERM_LABOR_CONTRACT.getType().equals(type)
                || term != null;
    }

}
