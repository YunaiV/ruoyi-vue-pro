package cn.iocoder.yudao.module.promotion.controller.app.point.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 积分商城活动的详细 Response VO")
@Data
public class AppPointActivityDetailRespVO {

    @Schema(description = "积分商城活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11373")
    private Long id;

    @Schema(description = "积分商城活动商品", requiredMode = Schema.RequiredMode.REQUIRED, example = "19509")
    private Long spuId;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    @Schema(description = "积分商城活动库存(剩余库存积分兑换时扣减)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer stock;

    @Schema(description = "积分商城活动总库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer totalStock;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "商品信息数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Product> products;

    //======================= 显示所需兑换积分最少的 sku 信息 =======================

    @Schema(description = "兑换积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer point;

    @Schema(description = "兑换金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "15860")
    private Integer price;

    @Schema(description = "商品信息")
    @Data
    public static class Product {

        @Schema(description = "积分商城商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31718")
        private Long id;

        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2736")
        private Long skuId;

        @Schema(description = "可兑换数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3926")
        private Integer count;

        @Schema(description = "兑换积分", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer point;

        @Schema(description = "兑换金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "15860")
        private Integer price;

        @Schema(description = "积分商城商品库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer stock;

    }

}
