package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import java.util.List;

@ApiModel("管理后台 - 商品 SPU 创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuCreateReqVO extends ProductSpuBaseVO {

    /**
     * SKU 数组
     */
    @Valid
    private List<ProductSkuCreateOrUpdateReqVO> skus;

}
