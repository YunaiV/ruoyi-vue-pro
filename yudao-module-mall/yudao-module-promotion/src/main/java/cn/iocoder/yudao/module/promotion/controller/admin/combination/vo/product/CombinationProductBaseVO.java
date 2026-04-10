package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 拼团商品 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CombinationProductBaseVO {

    @Schema(description = "商品 spuId", requiredMode = Schema.RequiredMode.REQUIRED, example = "30563")
    @NotNull(message = "商品 spuId 不能为空")
    private Long spuId;

    @Schema(description = "商品 skuId", requiredMode = Schema.RequiredMode.REQUIRED, example = "30563")
    @NotNull(message = "商品 skuId 不能为空")
    private Long skuId;

    @Schema(description = "拼团价格，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "27682")
    @NotNull(message = "拼团价格不能为空")
    private Integer combinationPrice;

}
