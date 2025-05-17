package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "用户 App - 商品结算信息 Response VO")
@Data
public class AppTradeProductSettlementRespVO {

    @Schema(description = "SPU 商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long spuId;

    @Schema(description = "SKU 价格信息数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Sku> skus;

    @Schema(description = "满减送活动信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private RewardActivity rewardActivity;

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

    @Schema(description = "满减送活动信息")
    @Data
    public static class RewardActivity {

        @Schema(description = "满减活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "条件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer conditionType;

        @Schema(description = "优惠规则的数组", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<RewardActivityRule> rules;

    }

    @Schema(description = "优惠规则")
    @Data
    public static class RewardActivityRule {

        @Schema(description = "优惠门槛", requiredMode = Schema.RequiredMode.REQUIRED, example = "100") // 1. 满 N 元，单位：分; 2. 满 N 件
        private Integer limit;

        @Schema(description = "优惠价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer discountPrice;

        @Schema(description = "是否包邮", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean freeDelivery;

        @Schema(description = "赠送的积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer point;

        @Schema(description = "赠送的优惠劵编号的数组")
        private Map<Long, Integer> giveCouponTemplateCounts;

    }

}
