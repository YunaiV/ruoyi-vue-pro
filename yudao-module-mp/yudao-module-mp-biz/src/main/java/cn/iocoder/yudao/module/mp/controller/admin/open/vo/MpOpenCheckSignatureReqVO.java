package cn.iocoder.yudao.module.mp.controller.admin.open.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 公众号校验签名 Request VO")
@Data
public class MpOpenCheckSignatureReqVO {

    @Schema(description = "微信加密签名", required = true, example = "490eb57f448b87bd5f20ccef58aa4de46aa1908e")
    @NotEmpty(message = "微信加密签名不能为空")
    private String signature;

    @Schema(description = "时间戳", required = true, example = "1672587863")
    @NotEmpty(message = "时间戳不能为空")
    private String timestamp;

    @Schema(description = "随机数", required = true, example = "1827365808")
    @NotEmpty(message = "随机数不能为空")
    private String nonce;

    @Schema(description = "随机字符串", required = true, example = "2721154047828672511")
    @NotEmpty(message = "随机字符串不能为空")
    @SuppressWarnings("SpellCheckingInspection")
    private String echostr;

}
