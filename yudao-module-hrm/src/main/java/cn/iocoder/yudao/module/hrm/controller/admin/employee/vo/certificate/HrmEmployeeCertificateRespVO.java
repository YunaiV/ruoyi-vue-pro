package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.certificate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工证书 Response VO")
@Data
public class HrmEmployeeCertificateRespVO {

    @Schema(description = "证书编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long employeeId;

    @Schema(description = "证书名称", example = "高级工程师")
    private String name;

    @Schema(description = "证书级别", example = "高级")
    private String level;

    @Schema(description = "证书编号", example = "CERT-001")
    private String no;

    @Schema(description = "有效起始日期")
    private LocalDateTime startTime;

    @Schema(description = "有效结束日期")
    private LocalDateTime endTime;

    @Schema(description = "发证机构", example = "人社局")
    private String issuingAuthority;

    @Schema(description = "发证日期")
    private LocalDateTime issuingTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
