package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@ApiModel("管理后台 - 规格 + 规格值 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyAndValueRespVO extends ProductPropertyBaseVO {

    @ApiModelProperty(value = "规格的编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    /**
     * 规格值的集合
     */
    private List<ProductPropertyValueRespVO> values;

}
