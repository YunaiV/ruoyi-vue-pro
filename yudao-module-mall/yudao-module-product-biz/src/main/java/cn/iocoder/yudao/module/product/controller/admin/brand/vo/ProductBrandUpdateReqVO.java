package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 商品品牌更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductBrandUpdateReqVO extends ProductBrandBaseVO {

    @ApiModelProperty(value = "品牌编号", required = true, example = "1")
    @NotNull(message = "品牌编号不能为空")
    private Long id;

}
