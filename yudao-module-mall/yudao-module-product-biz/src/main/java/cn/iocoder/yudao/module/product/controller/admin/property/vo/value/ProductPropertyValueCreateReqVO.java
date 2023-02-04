package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 商品属性值创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyValueCreateReqVO extends ProductPropertyValueBaseVO {

}
