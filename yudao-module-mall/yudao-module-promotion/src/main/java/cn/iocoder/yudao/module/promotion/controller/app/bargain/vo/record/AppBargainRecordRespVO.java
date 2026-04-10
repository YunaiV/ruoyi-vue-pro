package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 砍价记录的 Response VO")
@Data
public class AppBargainRecordRespVO {

    @Schema(description = "砍价记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long spuId;
    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long skuId;

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22901")
    private Long activityId;

    @Schema(description = "砍价记录状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "当前价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "102")
    private Integer bargainPrice;

    // ========== 活动相关 ==========

    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String activityName;

    @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED,  // 从 SPU 的 picUrl 读取
            example = "https://www.iocoder.cn/xx.png")
    private String picUrl;

    // ========== 订单相关 ==========

    @Schema(description = "订单编号", example = "1024")
    private Long orderId;

    @Schema(description = "支付状态", example = "true")
    private Boolean payStatus;

    @Schema(description = "支付订单编号", example = "1024")
    private Long payOrderId;

}
