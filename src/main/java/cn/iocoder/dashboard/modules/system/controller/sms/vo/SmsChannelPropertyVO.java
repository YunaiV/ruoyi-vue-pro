package cn.iocoder.dashboard.modules.system.controller.sms.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class SmsChannelPropertyVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 编码(来自枚举类 阿里、华为、七牛等)
     */
    private String code;

    /**
     * 渠道账号id
     */
    private String apiKey;

    /**
     * 渠道账号秘钥
     */
    private String apiSecret;

    /**
     * 实际渠道签名唯一标识
     */
    private String apiSignatureId;

    /**
     * 签名值
     */
    private String signature;

    /**
     * 该渠道名下的短信模板集合
     */
    private List<SmsTemplateVO> templateList;

    public SmsTemplateVO getTemplateByTemplateCode(String tempCode) {
        return templateList.stream().filter(s -> s.getCode().equals(tempCode)).findFirst().get();
    }

}
