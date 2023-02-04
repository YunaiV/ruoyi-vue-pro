package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 商品 SKU Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSkuRespVO extends ProductSkuBaseVO {

    @Schema(description = "主键", required = true, example = "1024")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 属性数组
     */
    private List<Property> properties;

}
