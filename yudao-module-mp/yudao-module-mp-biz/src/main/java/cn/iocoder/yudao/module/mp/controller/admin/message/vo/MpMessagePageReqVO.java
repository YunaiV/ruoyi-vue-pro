package cn.iocoder.yudao.module.mp.controller.admin.message.vo;

import lombok.*;

import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@ApiModel("管理后台 - 粉丝消息表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpMessagePageReqVO extends PageParam {

    @ApiModelProperty(value = "用户标识")
    private String openId;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "微信账号ID")
    private Long accountId;

    @ApiModelProperty(value = "消息类型")
    private String type;

}
