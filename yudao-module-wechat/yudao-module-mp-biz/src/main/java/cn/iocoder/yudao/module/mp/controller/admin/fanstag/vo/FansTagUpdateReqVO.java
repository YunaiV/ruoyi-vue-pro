package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import lombok.*;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

/**
 * @author fengdan
 */
@ApiModel("管理后台 - 粉丝标签更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FansTagUpdateReqVO extends FansTagBaseVO {

    @ApiModelProperty(value = "标签id，由微信分配", required = true)
    @NotNull(message = "主键不能为空")
    private Long id;

    @NotBlank(message = "公众号appId不能为空")
    @ApiModelProperty("微信公众号appId")
    private String appId;

}
