package cn.iocoder.yudao.module.trade.controller.admin.base.property;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 商品规格 Request VO")
@Data
public class ProductPropertyRespVO {

    @ApiModelProperty(value = "属性编号", required = true, example = "1")
    private Long propertyId;

    @ApiModelProperty(value = "属性值编号", required = true, example = "2")
    private Long valueId;

}
