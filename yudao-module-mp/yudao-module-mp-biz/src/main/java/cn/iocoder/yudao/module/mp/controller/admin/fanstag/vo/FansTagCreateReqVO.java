package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author fengdan
 */
@ApiModel("管理后台 - 粉丝标签创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FansTagCreateReqVO extends FansTagBaseVO {
    @NotBlank(message = "公众号appId不能为空")
    @ApiModelProperty("微信公众号appId")
    private String appId;
}
