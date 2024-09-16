package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 商品结算信息 Response VO")
@Data
public class AppTradeProductSettlementRespVO {

    @Schema(description = "SPU 商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long spuId;

    @Schema(description = "SKU 价格信息数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Sku> skus;

    @Schema(description = "满减送活动信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private RewardActivity rewardActivity;

    @Schema(description = "满减送活动信息")
    @Data
    public static class RewardActivity {

        @Schema(description = "满减活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "优惠规则描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "满 0.5 元减 0.3")
        private List<String> ruleDescriptions;

    }

    @Schema(description = "SKU 价格信息")
    @Data
    public static class Sku implements Serializable {

        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "优惠后价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer promotionPrice;

        @Schema(description = "营销类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
        private Integer promotionType; // 对应 PromotionTypeEnum 枚举，目前只有 4 和 6 两种

        @Schema(description = "营销编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long promotionId; // 目前只有限时折扣活动的编号

        @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime promotionEndTime;

    }

}
