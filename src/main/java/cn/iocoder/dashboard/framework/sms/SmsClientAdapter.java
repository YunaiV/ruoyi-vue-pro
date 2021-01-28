package cn.iocoder.dashboard.framework.sms;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.dashboard.common.exception.ServiceException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.SMS_CHANNEL_NOT_INIT;

/**
 * 抽象短信客户端工厂
 *
 * @author zzf
 * @date 2021/1/28 14:01
 */
public class SmsClientAdapter {

    private final Map<Long, SmsClient<?>> smsSenderMap;

    public SmsClientAdapter(Map<Long, SmsClient<?>> smsSenderMap) {
        if (ObjectUtil.isEmpty(smsSenderMap)) {
            throw new ServiceException(SMS_CHANNEL_NOT_INIT);
        }
        this.smsSenderMap = smsSenderMap;
    }

    public void flushClient(Map<Long, SmsClient<?>> smsSenderMap) {
        this.smsSenderMap.clear();
        smsSenderMap.putAll(Collections.unmodifiableMap(smsSenderMap));
    }

    public SmsResult<?> send(Long channelId, SmsBody smsBody, Collection<String> targetPhone) {
        SmsClient<?> smsClient = getSmsSender(channelId);
        return smsClient.send(smsBody, targetPhone);
    }

    private SmsClient<?> getSmsSender(Long channelId) {
        return smsSenderMap.get(channelId);
    }
}
