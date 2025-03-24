package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采购合同入参VO
 */
@Data
public class ErpPurchaseOrderGenerateContractReqVO {
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "采购订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采购订单编号不能为空")
    private Long orderId;

}
