package cn.iocoder.yudao.module.product.controller.app.recommend.vo;

import cn.iocoder.yudao.module.product.service.recommend.bo.ProductRecommendBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 智能推荐商品 Response VO")
@Data
public class AppProductRecommendRespVO {

    @Schema(description = "商品 SPU 编号")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "商品主图")
    private String picUrl;

    @Schema(description = "零售价（分）")
    private Integer retailPrice;

    @Schema(description = "批发价（分）")
    private Integer wholesalePrice;

    @Schema(description = "折扣百分比（0-100）")
    private Integer discountPercent;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "销量")
    private Integer salesCount;

    @Schema(description = "综合推荐分（0-1）")
    private Double score;

    @Schema(description = "推荐理由")
    private String reason;

    @Schema(description = "各维度推荐分")
    private ProductRecommendBO.DimensionScore dimensionScore;

}
