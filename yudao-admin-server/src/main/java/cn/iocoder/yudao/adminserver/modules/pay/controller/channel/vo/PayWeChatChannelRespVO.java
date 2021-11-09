package cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import java.util.Date;

@ApiModel("支付微信渠道 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayWeChatChannelRespVO extends PayChannelBaseVO {

    @ApiModelProperty(value = "商户编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    /**
     * 微信配置类
     */
    @Valid
    private WeChatConfig weChatConfig;

    /**
     * 微信配置类
     */
    @Data
    @ApiModel("微信配置类")
    public static class WeChatConfig {

        @ApiModelProperty(value = "公众号或者小程序的 appid", required = true, example = "wx041349c6f39b261b")
        private String appId;


        @ApiModelProperty(value = "商户号", required = true, example = "1545083881")
        private String mchId;

        @ApiModelProperty(value = "API 版本", required = true, example = "v2")
        private String apiVersion;

        // ========== V2 版本的参数 ==========

        @ApiModelProperty(value = "商户密钥", required = true, example = "0alL64UDQdaCwiKZ73ib7ypaIjMns06p")
        private String mchKey;

        /// todo @aquan 暂不支持 .p12上传 后期优化
        /// apiclient_cert.p12 证书文件的绝对路径或者以 classpath: 开头的类路径. 对应的字符串
        /// private String keyContent;

        // ========== V3 版本的参数 ==========

        @ApiModelProperty(value = "apiclient_key.pem 证书对应的字符串", required = true, example = "-----BEGIN PRIVATE KEY-----")
        private String privateKeyContent;

        @ApiModelProperty(value = "apiclient_cert.pem 证书对应的字符串", required = true, example = "-----BEGIN CERTIFICATE-----")
        private String privateCertContent;
    }

}
