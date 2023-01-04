package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 商品属性值的明细 Response VO")
@Data
public class ProductPropertyValueDetailRespVO {

    @ApiModelProperty(value = "属性的编号", required = true, example = "1")
    private Long propertyId;

    @ApiModelProperty(value = "属性的名称", required = true, example = "颜色")
    private String propertyName;

    @ApiModelProperty(value = "属性值的编号", required = true, example = "1024")
    private Long valueId;

    @ApiModelProperty(value = "属性值的名称", required = true, example = "红色")
    private String valueName;

}
