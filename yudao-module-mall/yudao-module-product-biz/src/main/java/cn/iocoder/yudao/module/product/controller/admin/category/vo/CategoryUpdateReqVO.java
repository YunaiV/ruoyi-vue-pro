package cn.iocoder.yudao.module.product.controller.admin.category.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 商品分类更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CategoryUpdateReqVO extends CategoryBaseVO {

    @ApiModelProperty(value = "分类编号", required = true, example = "2")
    @NotNull(message = "分类编号不能为空")
    private Long id;

}
