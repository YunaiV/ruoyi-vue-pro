package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateReqVO;
import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 商品spu更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SpuUpdateReqVO extends ProductSpuBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空")
    private Long id;

    @ApiModelProperty(value = "sku组合")
    @Valid
    List<ProductSkuCreateReqVO> skus;

}
