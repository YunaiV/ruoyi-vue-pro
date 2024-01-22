package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 砍价活动的明细 Response VO")
@Data
public class AppBargainActivityDetailRespVO {

    @Schema(description = "砍价活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "砍价活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "618 大砍价")
    private String name;

    @Schema(description = "活动开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long skuId;

    @Schema(description = "商品价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer price;

    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "我要吃西红柿")
    private String description;

    @Schema(description = "砍价库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "512")
    private Integer stock;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096") // 从 SPU 的 picUrl 读取
    private String picUrl;

    @Schema(description = "商品市场价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "50") // 从 SPU 的 marketPrice 读取
    private Integer marketPrice;

    @Schema(description = "砍价起始价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer bargainFirstPrice;

    @Schema(description = "砍价最低金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer bargainMinPrice;

    @Schema(description = "砍价成功数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer successUserCount;

}
