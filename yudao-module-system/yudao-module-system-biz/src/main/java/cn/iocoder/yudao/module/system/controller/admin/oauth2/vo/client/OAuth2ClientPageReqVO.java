package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(title = "管理后台 - OAuth2 客户端分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuth2ClientPageReqVO extends PageParam {

    @Schema(title = "应用名", example = "土豆", description = "模糊匹配")
    private String name;

    @Schema(title = "状态", example = "1", description = "参见 CommonStatusEnum 枚举")
    private Integer status;

}
