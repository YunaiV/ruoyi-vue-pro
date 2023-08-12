package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 砍价商品 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class BargainProductBaseVO {

    @Schema(description = "商品 spuId", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @NotNull(message = "商品 spuId 不能为空")
    private Long spuId;

    @Schema(description = "商品 skuId", requiredMode = Schema.RequiredMode.REQUIRED, example = "44")
    @NotNull(message = "商品 skuId 不能为空")
    private Long skuId;

    @Schema(description = "砍价起始价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "33")
    @NotNull(message = "砍价起始价格不能为空")
    private Integer bargainFirstPrice;

    @Schema(description = "砍价底价", requiredMode = Schema.RequiredMode.REQUIRED, example = "22")
    @NotNull(message = "砍价底价不能为空")
    private Integer bargainPrice;

    @Schema(description = "活动库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "活动库存不能为空")
    private Integer stock;

}
