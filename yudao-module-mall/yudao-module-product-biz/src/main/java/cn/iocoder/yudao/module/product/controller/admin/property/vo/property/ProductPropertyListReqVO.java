package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@ApiModel("管理后台 - 规格名称 List Request VO")
@Data
@ToString(callSuper = true)
public class ProductPropertyListReqVO {

    @ApiModelProperty(value = "规格名称", example = "颜色")
    private String name;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举")
    private Integer status;

}
