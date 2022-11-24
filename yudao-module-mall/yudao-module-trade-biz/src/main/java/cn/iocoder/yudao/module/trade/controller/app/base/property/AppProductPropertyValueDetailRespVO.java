package cn.iocoder.yudao.module.trade.controller.app.base.property;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("用户 App - 规格 + 规格值 Response VO")
@Data
public class AppProductPropertyValueDetailRespVO {

    @ApiModelProperty(value = "属性的编号", required = true, example = "1")
    private Long propertyId;

    @ApiModelProperty(value = "属性的名称", required = true, example = "颜色")
    private String propertyName;

    @ApiModelProperty(value = "属性值的编号", required = true, example = "1024")
    private Long valueId;

    @ApiModelProperty(value = "属性值的名称", required = true, example = "红色")
    private String valueName;

}
