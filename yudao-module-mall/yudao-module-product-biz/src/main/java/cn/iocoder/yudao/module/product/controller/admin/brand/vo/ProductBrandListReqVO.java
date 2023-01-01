package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "管理后台 - 商品品牌分页 Request VO")
@Data
public class ProductBrandListReqVO {

    @ApiModelProperty(value = "品牌名称", example = "芋道")
    private String name;

}
