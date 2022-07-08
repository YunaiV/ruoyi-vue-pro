package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 品牌 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BrandRespVO extends BrandBaseVO {

    @ApiModelProperty(value = "品牌编号", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
