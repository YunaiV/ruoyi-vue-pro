package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 商品 SKU 创建/更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSkuCreateOrUpdateReqVO extends ProductSkuBaseVO {
    @Schema(description = "商品属性")
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Property {

        @Schema(description = "属性编号", required = true, example = "1")
        @NotNull(message = "属性编号不能为空")
        private Long propertyId;

        @Schema(description = "属性值编号", required = true, example = "1024")
        @NotNull(message = "属性值编号不能为空")
        private Long valueId;

    }

    /**
     * 属性数组
     */
    private List<Property> properties;

}
