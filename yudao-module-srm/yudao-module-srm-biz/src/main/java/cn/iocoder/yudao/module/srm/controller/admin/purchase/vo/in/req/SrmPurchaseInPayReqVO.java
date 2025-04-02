package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collection;

@Data
@Builder
public class SrmPurchaseInPayReqVO {

    @NotNull(message = "付款与否不能为空")
    @Schema(description = "付款/撤销付款")
    private Boolean pass;

    @Schema(description = "付款项", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, message = "付款项最少一个")
    private Collection<@Valid Item> items;

    @Data
    public static class Item {
        @Schema(description = "采购入库项ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "采购入库项ID不能为空")
        private Long id;

        @Schema(description = "付款金额", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "付款金额不能为空")
        private BigDecimal payPrice;
    }
}
