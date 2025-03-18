package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class ErpPurchaseInPayReqVO {

    @Schema(description = "采购入库项ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采购入库项ID不能为空")
    Collection<Long> inItemIds;

    @NotNull(message = "付款与否不能为空")
    @Schema(description = "付款/撤销付款")
    private Boolean pass;
}
