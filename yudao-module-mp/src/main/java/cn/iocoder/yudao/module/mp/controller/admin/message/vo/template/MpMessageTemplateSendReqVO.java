package cn.iocoder.yudao.module.mp.controller.admin.message.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - 公众号消息模版发送 Request VO") // 关联 https://developers.weixin.qq.com/doc/service/api/notify/template/api_sendtemplatemessage.html 文档
@Data
public class MpMessageTemplateSendReqVO {

    @Schema(description = "模版主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "7019")
    @NotNull(message = "模版主键不能为空")
    private Long id;

    @Schema(description = "公众号粉丝的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "公众号粉丝的编号不能为空")
    private Long userId;

    @Schema(description = "模板跳转链接")
    private String url;

    @Schema(description = "跳转小程序时填写")
    private String miniprogram;

    @Schema(description = "模板内容")
    private Map<String, String> data;

}