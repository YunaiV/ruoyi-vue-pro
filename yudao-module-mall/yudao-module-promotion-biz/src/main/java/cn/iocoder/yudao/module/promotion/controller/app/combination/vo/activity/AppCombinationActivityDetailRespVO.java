package cn.iocoder.yudao.module.promotion.controller.app.combination.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 拼团活动明细 Response VO")
@Data
public class AppCombinationActivityDetailRespVO {

    @Schema(description = "拼团活动编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "拼团活动名称", required = true, example = "618 大拼团")
    private String name;

    @Schema(description = "活动状态", required = true, example = "1")
    private Integer status;

    @Schema(description = "活动开始时间", required = true)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", required = true)
    private LocalDateTime endTime;

    @Schema(description = "拼团人数", required = true, example = "3")
    private Integer userSize;

    @Schema(description = "成功的拼团数量", required = true, example = "100")
    private Integer successCount;

    @Schema(description = "商品 SPU 编号", required = true, example = "2048")
    private Long spuId;

    @Schema(description = "商品信息数组", required = true)
    private List<Product> products;

    @Schema(description = "商品信息")
    @Data
    public static class Product {

        @Schema(description = "商品 SKU 编号", required = true, example = "4096")
        private Long skuId;

        @Schema(description = "拼团金额，单位：分", required = true, example = "100")
        private Integer combinationPrice;

        @Schema(description = "拼团限量库存", required = true, example = "50")
        private Integer quota;

        @Schema(description = "每人限购数量", required = true, example = "10")
        private Integer limitCount;

    }

}
