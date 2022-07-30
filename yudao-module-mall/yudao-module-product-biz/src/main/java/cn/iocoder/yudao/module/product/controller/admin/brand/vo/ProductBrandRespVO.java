package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("管理后台 - 品牌 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductBrandRespVO extends ProductBrandBaseVO {

    @ApiModelProperty(value = "品牌编号", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
