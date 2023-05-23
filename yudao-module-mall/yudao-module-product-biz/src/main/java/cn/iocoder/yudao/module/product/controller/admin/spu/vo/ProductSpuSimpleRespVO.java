package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * 商品 SPU 精简 Response VO
 *  TODO 商品 SPU 精简 VO 暂时没有使用到，用到的时候再按需添加\修改属性
 * @author HUIHUI
 */
@Schema(description = "管理后台 - 商品 SPU 精简 Response VO")
@Data
@ToString(callSuper = true)
public class ProductSpuSimpleRespVO {

    // TODO @puhui999：swagger 的 required 和 example 写下

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品价格，单位使用：分")
    private Integer price;

    @Schema(description = "商品市场价，单位使用：分")
    private Integer marketPrice;

    @Schema(description = "商品成本价，单位使用：分")
    private Integer costPrice;

    @Schema(description = "商品库存")
    private Integer stock;

    // ========== 统计相关字段 =========

    @Schema(description = "商品销量")
    private Integer salesCount;

    @Schema(description = "商品虚拟销量")
    private Integer virtualSalesCount;

    @Schema(description = "商品浏览量")
    private Integer browseCount;

}
