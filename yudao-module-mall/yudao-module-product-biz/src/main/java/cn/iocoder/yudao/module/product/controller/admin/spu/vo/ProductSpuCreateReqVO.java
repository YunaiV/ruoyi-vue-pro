package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateReqVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import java.util.List;

@ApiModel("管理后台 - 商品spu创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuCreateReqVO extends ProductSpuBaseVO {

    @ApiModelProperty(value = "sku组合")
    @Valid
    List<ProductSkuCreateReqVO> skus;

}
