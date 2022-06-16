package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import lombok.*;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

@ApiModel("管理后台 - 粉丝标签更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FansTagUpdateReqVO extends FansTagBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空")
    private Integer id;

}
