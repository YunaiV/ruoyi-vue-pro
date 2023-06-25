package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 砍价活动 Response VO")
@Data
public class AppBargainActivityRespVO {

    @Schema(description = "砍价活动编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "砍价活动名称", required = true, example = "618 大砍价")
    private String name;

    @Schema(description = "活动开始时间", required = true)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", required = true)
    private LocalDateTime endTime;

    @Schema(description = "商品 SPU 编号", required = true, example = "2048")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", required = true, example = "1024")
    private Long skuId;

    @Schema(description = "砍价库存", required = true, example = "512")
    private Integer stock;

    @Schema(description = "商品图片", required = true, example = "4096") // 从 SPU 的 picUrl 读取
    private String picUrl;

    @Schema(description = "商品市场价，单位：分", required = true, example = "50") // 从 SPU 的 marketPrice 读取
    private Integer marketPrice;

    @Schema(description = "砍价最低金额，单位：分", required = true, example = "100") // 从砍价商品里取最低价
    private Integer bargainPrice;

}
