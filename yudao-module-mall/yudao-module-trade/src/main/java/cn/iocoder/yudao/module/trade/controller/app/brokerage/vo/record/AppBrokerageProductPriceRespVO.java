package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 商品的分销金额 Response VO")
@Data
public class AppBrokerageProductPriceRespVO {

    @Schema(description = "是否开启", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Boolean enabled;

    @Schema(description = "分销最小金额，单位：分", example = "100")
    private Integer brokerageMinPrice;

    @Schema(description = "分销最大金额，单位：分", example = "100")
    private Integer brokerageMaxPrice;

}
