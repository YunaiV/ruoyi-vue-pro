package cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 规格值更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyValueUpdateReqVO extends ProductPropertyValueBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空")
    private Integer id;

}
