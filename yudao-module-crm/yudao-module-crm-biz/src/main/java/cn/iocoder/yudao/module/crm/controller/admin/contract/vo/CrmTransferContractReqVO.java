package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

// TODO @puhui999：CrmContractTransferReqVO，模块名字要放前面；看看还有没其它类似的
@Schema(description = "管理后台 - CRM 合同转移 Request VO")
@Data
public class CrmTransferContractReqVO {

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @NotNull(message = "合同编号不能为空")
    private Long id;

    @Schema(description = "新负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @NotNull(message = "新负责人的用户编号不能为空")
    private Long ownerUserId;

    @Schema(description = "原负责人移除方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @NotNull(message = "原负责人移除方式不能为空")
    private Integer transferType;

    @Schema(description = "权限类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;

}
