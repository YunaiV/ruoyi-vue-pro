package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采购合同入参VO
 */
@Data
public class ErpPurchaseOrderGenerateContractReqVO {
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;

    @Schema(description = "采购订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    //签订合同地点
    @Schema(description = "签订合同地点", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contractAddress;

    //订立合同日期
    @Schema(description = "订立合同日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime contractDate;
    //计价单位
    @Schema(description = "计价单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String valuationUnit;
}
