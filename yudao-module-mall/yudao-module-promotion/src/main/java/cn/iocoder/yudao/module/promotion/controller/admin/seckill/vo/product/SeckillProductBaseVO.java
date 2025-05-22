package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 秒杀参与商品 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author HUIHUI
 */
@Data
public class SeckillProductBaseVO {

    @Schema(description = "商品sku_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30563")
    @NotNull(message = "商品sku_id不能为空")
    private Long skuId;

    @Schema(description = "秒杀金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "6689")
    @NotNull(message = "秒杀金额，单位：分不能为空")
    private Integer seckillPrice;

    @Schema(description = "秒杀库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "秒杀库存不能为空")
    private Integer stock;

}
