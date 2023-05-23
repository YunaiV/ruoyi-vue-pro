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

    /**
     * 属性数组
     */
    private List<Property> properties;

}
