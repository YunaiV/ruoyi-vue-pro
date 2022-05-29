package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.client;

import lombok.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@ApiModel("管理后台 - OAuth2 客户端分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuth2ClientPageReqVO extends PageParam {

    @ApiModelProperty(value = "应用名", example = "土豆", notes = "模糊匹配")
    private String name;

    @ApiModelProperty(value = "状态", example = "1", notes = "参见 CommonStatusEnum 枚举")
    private Integer status;

}
