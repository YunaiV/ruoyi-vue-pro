package cn.iocoder.yudao.module.mp.controller.admin.user.vo;

import lombok.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@ApiModel("管理后台 - 微信公众号粉丝分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpUserPageReqVO extends PageParam {

    @ApiModelProperty(value = "用户标识")
    private String openid;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "微信公众号 ID")
    private Long accountId;

}
