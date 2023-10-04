package cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 秒杀活动的详细 Response VO")
@Data
public class AppSeckillActivityDetailRespVO {

    @Schema(description = "秒杀活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "秒杀活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "晚九点限时秒杀")
    private String name;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "活动开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long spuId;

    @Schema(description = "总共限购数量", example = "10")
    private Integer totalLimitCount;

    @Schema(description = "单次限购数量", example = "5")
    private Integer singleLimitCount;

    @Schema(description = "秒杀库存（剩余）", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer stock;

    @Schema(description = "秒杀库存（总计）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalStock;

    @Schema(description = "商品信息数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Product> products;

    @Schema(description = "商品信息")
    @Data
    public static class Product {

        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
        private Long skuId;

        @Schema(description = "秒杀金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer seckillPrice;

        @Schema(description = "秒杀限量库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        private Integer stock;

    }

}
