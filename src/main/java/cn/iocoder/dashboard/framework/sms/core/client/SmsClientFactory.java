package cn.iocoder.dashboard.framework.sms.core.client;

import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;

/**
 * 短信客户端工厂接口
 *
 * @author zzf
 * @date 2021/1/28 14:01
 */
public interface SmsClientFactory {

    /**
     * 获得短信 Client
     *
     * @param channelId 渠道编号
     * @return 短信 Client
     */
    SmsClient getSmsClient(Long channelId);

    /**
     * 创建短信 Client
     *
     * @param properties 配置对象
     */
    void createOrUpdateSmsClient(SmsChannelProperties properties);

}
