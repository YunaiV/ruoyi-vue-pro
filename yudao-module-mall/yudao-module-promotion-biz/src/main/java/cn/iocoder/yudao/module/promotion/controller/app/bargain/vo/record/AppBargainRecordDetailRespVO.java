package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 砍价记录的明细 Response VO")
@Data
public class AppBargainRecordDetailRespVO {

    public static final int HELP_ACTION_NONE = 1; // 帮砍动作 - 未帮砍，可以帮砍
    public static final int HELP_ACTION_FULL = 2; // 帮砍动作 - 未帮砍，无法帮砍（可帮砍次数已满)
    public static final int HELP_ACTION_SUCCESS = 3; // 帮砍动作 - 已帮砍

    // ========== 砍价记录 ==========

    @Schema(description = "砍价记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "666")
    private Long userId;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long spuId;
    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long skuId;

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private Long activityId;

    @Schema(description = "砍价起始价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "23")
    private Integer bargainFirstPrice;

    @Schema(description = "当前砍价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "23")
    private Integer bargainPrice;

    @Schema(description = "砍价记录状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    // ========== 订单相关 ========== 注意：只有是自己的砍价记录，才会返回，保证隐私性

    @Schema(description = "订单编号", example = "1024")
    private Long orderId;

    @Schema(description = "支付状态", example = "true")
    private Boolean payStatus;

    @Schema(description = "支付订单编号", example = "1024")
    private Long payOrderId;

    // ========== 助力记录 ==========

    private Integer helpAction;

}
