package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 合同 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContractRespVO extends CrmContractBaseVO {

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "创建人", example = "25682")
    private String creator;

    @Schema(description = "创建人名字", example = "test")
    private String creatorName;

    @Schema(description = "客户名字", example = "test")
    private String customerName;

    @Schema(description = "负责人", example = "test")
    private String ownerUserName;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer auditStatus;

}
