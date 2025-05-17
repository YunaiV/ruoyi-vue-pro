package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 APP - 微信公众号 JSAPI 签名 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthWeixinJsapiSignatureRespVO {

    @Schema(description = "微信公众号的 appId", requiredMode = Schema.RequiredMode.REQUIRED, example = "hello")
    private String appId;

    @Schema(description = "匿名串", requiredMode = Schema.RequiredMode.REQUIRED, example = "world")
    private String nonceStr;

    @Schema(description = "时间戳", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long timestamp;

    @Schema(description = "URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    private String url;

    @Schema(description = "签名", requiredMode = Schema.RequiredMode.REQUIRED, example = "阿巴阿巴")
    private String signature;

}
