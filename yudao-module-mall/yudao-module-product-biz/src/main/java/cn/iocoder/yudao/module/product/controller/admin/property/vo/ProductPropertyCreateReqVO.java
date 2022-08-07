package cn.iocoder.yudao.module.product.controller.admin.property.vo;

import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.ProductPropertyValueCreateReqVO;
import lombok.*;
import io.swagger.annotations.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 规格名称创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyCreateReqVO extends ProductPropertyBaseVO {

    @ApiModelProperty(value = "属性值")
    @NotNull(message = "属性值不能为空")
    List<ProductPropertyValueCreateReqVO> propertyValueList;

}
