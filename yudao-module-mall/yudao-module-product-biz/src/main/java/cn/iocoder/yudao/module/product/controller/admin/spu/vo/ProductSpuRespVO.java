package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 商品 SPU Response VO
 * TODO 移除ProductSpuPageRespVO相关应用跟换为ProductSpuRespVO已继承ProductSpuBaseVO 补全表格展示所需属性
 * @author HUIHUI
 */
@Schema(description = "管理后台 - 商品 SPU Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuRespVO extends ProductSpuBaseVO {

    @Schema(description = "spuId")
    private Long id;

    @Schema(description = "商品价格")
    private Integer price;

    @Schema(description = "商品销量")
    private Integer salesCount;

    @Schema(description = "市场价，单位使用：分")
    private Integer marketPrice;

    @Schema(description = "成本价，单位使用：分")
    private Integer costPrice;

    @Schema(description = "商品库存")
    private Integer stock;

    @Schema(description = "商品创建时间")
    private LocalDateTime createTime;

    @Schema(description = "商品状态")
    private Integer status;

}
