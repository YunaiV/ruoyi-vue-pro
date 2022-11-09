package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueCreateReqVO;
import lombok.*;
import io.swagger.annotations.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 规格名称创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyCreateReqVO extends ProductPropertyBaseVO {

    // TODO @Luowenfeng：规格值的 CRUD 可以单独；前端 + 后端，改成类似字典类型、字典数据的这种交互；在加一个 ProductPropertyValueController
    @ApiModelProperty(value = "属性值")
    @NotNull(message = "属性值不能为空")
    List<ProductPropertyValueCreateReqVO> propertyValueList;

}
