package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - ERP采购申请单子新增/修改 Request VO")
@Data
public class ErpPurchaseRequestItemsSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "15389")
    private Long id;

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "10555")
    @NotNull(message = "商品id不能为空")
    private Long productId;

    @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请数量不能为空")
    private Integer count;

}