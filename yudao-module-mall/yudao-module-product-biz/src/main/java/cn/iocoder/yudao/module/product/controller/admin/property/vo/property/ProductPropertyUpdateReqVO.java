package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueCreateReqVO;
import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import java.util.List;

@ApiModel("管理后台 - 规格名称更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyUpdateReqVO extends ProductPropertyBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "1")
    @NotNull(message = "主键不能为空")
    private Long id;

    @ApiModelProperty(value = "属性值")
    @NotNull(message = "属性值不能为空")
    List<ProductPropertyValueCreateReqVO> propertyValueList;

}
