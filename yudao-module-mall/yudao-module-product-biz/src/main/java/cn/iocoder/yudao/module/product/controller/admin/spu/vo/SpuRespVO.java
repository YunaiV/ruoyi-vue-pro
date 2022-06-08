package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 商品spu Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SpuRespVO extends ProductSpuBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    List<ProductSkuRespVO> skus;

}
