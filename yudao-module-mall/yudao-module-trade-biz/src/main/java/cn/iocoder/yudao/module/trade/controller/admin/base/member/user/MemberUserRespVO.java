package cn.iocoder.yudao.module.trade.controller.admin.base.member.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 会员用户 Response VO")
@Data
public class MemberUserRespVO {

    @ApiModelProperty(value = "用户 ID", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "用户昵称", required = true, example = "芋道源码")
    private String nickname;

    @ApiModelProperty(value = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

}
