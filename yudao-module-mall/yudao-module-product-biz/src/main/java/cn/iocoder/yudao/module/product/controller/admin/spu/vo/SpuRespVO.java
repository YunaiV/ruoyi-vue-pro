package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@ApiModel("管理后台 - 商品spu Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SpuRespVO extends ProductSpuBaseVO {

    // TODO @franky：注解要完整

    @ApiModelProperty(value = "主键", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * SKU 数组
     */
    List<ProductSkuRespVO> skus;

    @ApiModelProperty(value = "分类id数组，一直递归到一级父节点", example = "[1,2,4]")
    LinkedList<Long> categoryIds;

}
