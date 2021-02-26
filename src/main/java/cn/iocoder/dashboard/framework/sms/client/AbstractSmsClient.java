package cn.iocoder.dashboard.framework.sms.client;

import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 抽象短息客户端
 *
 * @author zzf
 * @date 2021/2/1 9:28
 */
@Slf4j
public abstract class AbstractSmsClient implements SmsClient {

    /**
     * 短信渠道参数
     */
    protected final SmsChannelProperty channelVO;

    /**
     * 短信客户端有参构造函数
     *
     * @param property 短信配置
     */
    public AbstractSmsClient(SmsChannelProperty property) {
        this.channelVO = property;
    }

    public SmsChannelProperty getProperty() {
        return channelVO;
    }

    @Override
    public final SmsResult send(String templateApiId, SmsBody smsBody, Collection<String> targets) {
        SmsResult result;
        try {
            beforeSend(templateApiId, smsBody, targets);
            result = doSend(templateApiId, smsBody, targets);
            afterSend(templateApiId, smsBody, targets, result);
        } catch (Exception e) {
            // exception handle
            log.debug(e.getMessage(), e);
            return SmsResult.failResult("发送异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 发送消息
     *
     * @param templateApiId 短信模板唯一标识
     * @param smsBody       消息内容
     * @param targets       发送对象列表
     * @return 短信发送结果
     * @throws Exception 调用发送失败，抛出异常
     */
    public abstract SmsResult doSend(String templateApiId, SmsBody smsBody, Collection<String> targets) throws Exception;

    protected void beforeSend(String templateApiId, SmsBody smsBody, Collection<String> targets) throws Exception {
    }

    protected void afterSend(String templateApiId, SmsBody smsBody, Collection<String> targets, SmsResult result) throws Exception {
    }

}
