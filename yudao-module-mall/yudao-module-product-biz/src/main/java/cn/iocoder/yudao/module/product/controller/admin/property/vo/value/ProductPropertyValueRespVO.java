package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 规格值 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyValueRespVO extends ProductPropertyValueBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "10")
    private Long id;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

}
