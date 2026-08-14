package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.certificate;

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

@Schema(description = "管理后台 - HRM 员工证书保存 Request VO")
@Data
public class HrmEmployeeCertificateSaveReqVO {

    @Schema(description = "证书编号", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "证书名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "高级工程师")
    @NotBlank(message = "证书名称不能为空")
    @Size(max = 255, message = "证书名称长度不能超过 255 个字符")
    @DiffLogField(name = "证书名称")
    private String name;

    @Schema(description = "证书级别", example = "高级")
    @Size(max = 255, message = "证书级别长度不能超过 255 个字符")
    @DiffLogField(name = "证书级别")
    private String level;

    @Schema(description = "证书编号", example = "CERT-001")
    @Size(max = 255, message = "证书编号长度不能超过 255 个字符")
    @DiffLogField(name = "证书编号")
    private String no;

    @Schema(description = "有效起始日期")
    @DiffLogField(name = "有效起始日期")
    private LocalDateTime startTime;

    @Schema(description = "有效结束日期")
    @DiffLogField(name = "有效结束日期")
    private LocalDateTime endTime;

    @Schema(description = "发证机构", example = "人社局")
    @Size(max = 255, message = "发证机构长度不能超过 255 个字符")
    @DiffLogField(name = "发证机构")
    private String issuingAuthority;

    @Schema(description = "发证日期")
    @DiffLogField(name = "发证日期")
    private LocalDateTime issuingTime;

    @Schema(description = "备注", example = "专业技术资格证书")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "排序", example = "1")
    @DiffLogField(name = "排序")
    private Integer sort;

    @AssertTrue(message = "证书有效期的结束日期不能早于开始日期")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || isAfterOrEqual(endTime, startTime);
    }

}
