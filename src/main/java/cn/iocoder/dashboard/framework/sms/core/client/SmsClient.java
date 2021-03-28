package cn.iocoder.dashboard.framework.sms.core.client;

import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;

import javax.servlet.ServletRequest;
import java.util.Map;

/**
 * 短信客户端接口
 *
 * @author zzf
 * @date 2021/1/25 14:14
 */
public interface SmsClient {

    /**
     * 获得渠道编号
     *
     * @return 渠道编号
     */
    Long getId();

    /**
     * 发送消息
     *
     * @param sendLogId 发送日志编号
     * @param mobile 手机号
     * @param apiTemplateId 短信 API 的模板编号
     * @param templateParams 短信模板参数
     * @return 短信发送结果
     */
    SmsResult send(Long sendLogId, String mobile, String apiTemplateId, Map<String, Object> templateParams);

    // TODO FROM 芋艿 to ZZF：是不是可以改成意图更明确的解析返回结果，例如说 parseXXXX
    /**
     * 短信发送回调请求处理
     *
     * @param request 请求
     * @return 短信发送结果
     */
    SmsResultDetail smsSendCallbackHandle(ServletRequest request) throws Exception;

}
