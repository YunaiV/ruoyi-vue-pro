package cn.iocoder.dashboard.framework.sms.core.property;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * 渠道模板VO类
 *
 * @author zzf
 * @date 2021/1/25 17:03
 */
@Data
@EqualsAndHashCode
public class SmsTemplateProperty {

    /**
     * 渠道id
     */
    @NotEmpty(message = "短信渠道编码不能为空")
    private Long channelId;

    /**
     * 业务编码(来自数据字典, 用户自定义业务场景 一个场景可以有多个模板)
     */
    private String bizCode;

    /**
     * 编码
     */
    @NotEmpty(message = "短信模板编码不能为空")
    private String code;

    /**
     * 实际渠道模板唯一标识
     */
    @NotEmpty(message = "短信模板唯一标识不能为空")
    private String apiTemplateId;

    /**
     * 内容
     */
    @NotEmpty(message = "短信模板内容不能为空")
    private String content;

}
