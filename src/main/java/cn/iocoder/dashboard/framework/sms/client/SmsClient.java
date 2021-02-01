package cn.iocoder.dashboard.framework.sms.client;

import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;

import java.util.Collection;

/**
 * 短信父接口
 *
 * @author zzf
 * @date 2021/1/25 14:14
 */
public interface SmsClient<R> {

    /**
     * 发送消息
     *
     * @param smsBody 消息内容
     * @param targets 发送对象列表
     * @return 是否发送成功
     */
    SmsResult<R> send(SmsBody smsBody, Collection<String> targets);

}