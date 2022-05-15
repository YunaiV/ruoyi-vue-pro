package cn.iocoder.yudao.module.product.controller.admin.attrkey.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 规格名称更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AttrKeyUpdateReqVO extends AttrKeyBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空")
    private Integer id;

}
