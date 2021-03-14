package cn.iocoder.dashboard.framework.sms.client;

import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;

import javax.servlet.ServletRequest;

/**
 * 短信父接口
 *
 * @author zzf
 * @date 2021/1/25 14:14
 */
public interface SmsClient {

    /**
     * 发送消息
     *
     * @param templateApiId 短信模板唯一标识
     * @param smsBody       消息内容
     * @param targets       发送对象列表
     * @return 短信发送结果
     */
    SmsResult send(String templateApiId, SmsBody smsBody, String targets);

    // TODO FROM 芋艿 to ZZF：是不是可以改成意图更明确的解析返回结果，例如说 parseXXXX
    /**
     * 短信发送回调请求处理
     *
     * @param request 请求
     * @return 短信发送结果
     */
    SmsResultDetail smsSendCallbackHandle(ServletRequest request) throws Exception;

}
