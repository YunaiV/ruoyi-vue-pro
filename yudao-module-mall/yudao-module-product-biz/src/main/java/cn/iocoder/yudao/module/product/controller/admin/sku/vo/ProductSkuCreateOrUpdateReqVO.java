package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 商品 SKU 创建/更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSkuCreateOrUpdateReqVO extends ProductSkuBaseVO {

}
