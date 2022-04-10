package cn.iocoder.yudao.framework.sms.core.client.impl.tencent;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import lombok.Data;

/**
 * 腾讯云短信配置实现类
 * 腾讯云发送短信时，需要额外的参数 sdkAppId,
 *
 * @author shiwp
 */
@Data
public class TencentSmsChannelProperties extends SmsChannelProperties {

    /**
     * 应用 id
     */
    private String sdkAppId;

    /**
     * 考虑到不破坏原有的 apiKey + apiSecret 的结构，
     * 所以腾讯云短信存储时，将 secretId 拼接到 apiKey 字段中，格式为 "secretId sdkAppId"。
     * 因此在使用时，需要将 secretId 和 sdkAppId 解析出来，分别存储到对应字段中。
     */
    public static TencentSmsChannelProperties build(SmsChannelProperties properties) {
        if (properties instanceof TencentSmsChannelProperties) {
            return (TencentSmsChannelProperties) properties;
        }
        TencentSmsChannelProperties result = BeanUtil.toBean(properties, TencentSmsChannelProperties.class);
        String combineKey = properties.getApiKey();
        Assert.notEmpty(combineKey, "apiKey 不能为空");
        String[] keys = combineKey.trim().split(" ");
        Assert.isTrue(keys.length == 2, "腾讯云短信 apiKey 配置格式错误，请配置 为[secretId sdkAppId]");
        Assert.notBlank(keys[0], "腾讯云短信 secretId 不能为空");
        Assert.notBlank(keys[1], "腾讯云短信 sdkAppId 不能为空");
        result.setSdkAppId(keys[1]).setApiKey(keys[0]);
        return result;
    }
}
