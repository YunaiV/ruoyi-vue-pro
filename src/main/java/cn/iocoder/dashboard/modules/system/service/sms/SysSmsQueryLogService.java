package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;

/**
 * 短信请求日志服务接口
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SysSmsQueryLogService {

    /**
     * 发送短信前的日志处理
     *
     * @param smsBody     短信内容
     * @param targetPhone 发送对象手机号
     * @param client      短信客户端
     * @return 生成的日志id
     */
    void beforeSendLog(SmsBody smsBody, String targetPhone, AbstractSmsClient client);

    /**
     * 发送消息后的日志处理
     *
     * @param logId  日志id
     * @param result 消息结果
     */
    void afterSendLog(Long logId, SmsResult result);

    void updateSendLogByResultDetail(SmsResultDetail smsResultDetail);
}
