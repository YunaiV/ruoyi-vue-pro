package cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 规格值 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PropertyValueRespVO extends PropertyValueBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    private Integer id;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
