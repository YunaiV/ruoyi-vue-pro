package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;

import java.util.List;

/**
 * 短信渠道Service接口
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SmsLogService {
    /**
     * 发送短信前的日志处理
     *
     * @param smsBody      短信内容
     * @param targetPhones 发送对象手机号集合
     * @param client       短信客户端
     * @param isAsync      是否异步发送
     * @return 生成的日志id
     */
    Long beforeSendLog(SmsBody smsBody, List<String> targetPhones, AbstractSmsClient<?> client, Boolean isAsync);

    /**
     * 发送消息后的日志处理
     *
     * @param logId  日志id
     * @param result 消息结果
     */
    void afterSendLog(Long logId, SmsResult<?> result);

}
