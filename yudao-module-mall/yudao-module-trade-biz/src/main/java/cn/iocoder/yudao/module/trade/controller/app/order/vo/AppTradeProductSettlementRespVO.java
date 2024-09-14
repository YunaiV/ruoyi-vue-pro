package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 商品结算信息 Response VO")
@Data
@Builder
public class AppTradeProductSettlementRespVO {

    @Schema(description = "商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "满减活动对象", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Reward reward;

    @Schema(description = "sku活动信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Sku> skus;

    /**
     * 满减活动
     */
    @Data
    @Builder
    public static class Reward implements Serializable {
        @Schema(description = "满减活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long rewardId;

        @Schema(description = "满减活动信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private String rewardActivity;
    }

    /**
     * SKU 数组
     */
    @Data
    @Builder
    public static class Sku implements Serializable {
        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long skuId;

        @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer price;

        @Schema(description = "营销类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1") //PromotionTypeEnum
        private Integer type;

        @Schema(description = "限时优惠id", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long discountId;

        @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime endTime;
    }
}
