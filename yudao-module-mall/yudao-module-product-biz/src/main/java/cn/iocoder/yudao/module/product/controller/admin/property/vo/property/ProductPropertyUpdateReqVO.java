package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 属性项更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyUpdateReqVO extends ProductPropertyBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "1")
    @NotNull(message = "主键不能为空")
    private Long id;

}
