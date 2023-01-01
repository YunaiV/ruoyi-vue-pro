package cn.iocoder.yudao.module.mp.controller.admin.accountfanstag.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 粉丝标签关联 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class WxAccountFansTagBaseVO {

    @ApiModelProperty(value = "用户标识")
    private String openid;

    @ApiModelProperty(value = "标签ID")
    private String tagId;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

}
