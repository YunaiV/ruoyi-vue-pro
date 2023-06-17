package cn.iocoder.yudao.module.promotion.controller.app.seckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 秒杀活动 Response VO")
@Data
public class AppSeckillActivitiDetailRespVO {

    @Schema(description = "秒杀活动编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "秒杀活动名称", required = true, example = "晚九点限时秒杀")
    private String name;

    @Schema(description = "活动状态", required = true, example = "1")
    private Integer status;

    // TODO @芋艿：开始时间、结束时间，要和场次结合起来；就是要算到当前场次，是几点哈；

    @Schema(description = "活动开始时间", required = true)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", required = true)
    private LocalDateTime endTime;

    @Schema(description = "商品 SPU 编号", required = true, example = "2048")
    private Long spuId;

    @Schema(description = "商品信息数组", required = true)
    private List<Product> products;

    @Schema(description = "商品信息")
    @Data
    public static class Product {

        @Schema(description = "商品 SKU 编号", required = true, example = "4096")
        private Long skuId;

        @Schema(description = "秒杀金额，单位：分", required = true, example = "100")
        private Integer seckillPrice;

        @Schema(description = "秒杀限量库存", required = true, example = "50")
        private Integer quota;

        @Schema(description = "每人限购数量", required = true, example = "10")
        private Integer limitCount;

    }

}
