package cn.iocoder.yudao.module.product.controller.admin.property.vo;

import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.ProductPropertyValueRespVO;
import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 规格名称 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyRespVO extends ProductPropertyBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "属性值")
    private List<ProductPropertyValueRespVO> propertyValueList;

}
