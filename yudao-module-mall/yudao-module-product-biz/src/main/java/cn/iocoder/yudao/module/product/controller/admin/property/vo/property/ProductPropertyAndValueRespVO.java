package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel("管理后台 - 商品属性项 + 属性值 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyAndValueRespVO extends ProductPropertyBaseVO {

    @ApiModelProperty(value = "属性项的编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    /**
     * 属性值的集合
     */
    private List<ProductPropertyValueRespVO> values;

}
