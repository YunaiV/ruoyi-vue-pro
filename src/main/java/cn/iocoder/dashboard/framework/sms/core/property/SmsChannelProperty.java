package cn.iocoder.dashboard.framework.sms.core.property;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 渠道(包含模板)信息VO类
 *
 * @author zzf
 * @date 2021/1/25 17:01
 */
@Data
@EqualsAndHashCode
public class SmsChannelProperty implements Serializable {

    /**
     * id
     */
    @NotNull(message = "短信渠道ID不能为空")
    private Long id;

    /**
     * 编码(来自枚举类 阿里、华为、七牛等)
     */
    @NotEmpty(message = "短信渠道编码不能为空")
    private String code;

    /**
     * 渠道账号id
     */
    @NotEmpty(message = "渠道账号id不能为空")
    private String apiKey;

    /**
     * 渠道账号秘钥
     */
    @NotEmpty(message = "渠道账号秘钥不能为空")
    private String apiSecret;

    /**
     * 实际渠道签名唯一标识
     */
    @NotEmpty(message = "实际渠道签名唯一标识不能为空")
    private String apiSignatureId;

    /**
     * 签名值
     */
    @NotEmpty(message = "签名值不能为空")
    private String signature;

}
