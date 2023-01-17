package cn.iocoder.yudao.module.mp.controller.admin.open.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@ApiModel("管理后台 - 公众号处理消息 Request VO")
@Data
public class MpOpenHandleMessageReqVO {

    public static final String ENCRYPT_TYPE_AES = "aes";

    @ApiModelProperty(value = "微信加密签名", required = true, example = "490eb57f448b87bd5f20ccef58aa4de46aa1908e")
    @NotEmpty(message = "微信加密签名不能为空")
    private String signature;

    @ApiModelProperty(value = "时间戳", required = true, example = "1672587863")
    @NotEmpty(message = "时间戳不能为空")
    private String timestamp;

    @ApiModelProperty(value = "随机数", required = true, example = "1827365808")
    @NotEmpty(message = "随机数不能为空")
    private String nonce;

    @ApiModelProperty(value = "粉丝 openid", required = true, example = "oz-Jdtyn-WGm4C4I5Z-nvBMO_ZfY")
    @NotEmpty(message = "粉丝 openid 不能为空")
    private String openid;

    @ApiModelProperty(value = "消息加密类型", example = "aes")
    private String encrypt_type;

    @ApiModelProperty(value = "微信签名", example = "QW5kcm9pZCBUaGUgQmFzZTY0IGlzIGEgZ2VuZXJhdGVkIHN0cmluZw==")
    private String msg_signature;

}
