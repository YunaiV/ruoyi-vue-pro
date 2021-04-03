package cn.iocoder.dashboard.framework.sms.core.client;

import cn.iocoder.dashboard.common.core.KeyValue;
import cn.iocoder.dashboard.framework.sms.core.client.dto.SmsResultDetail;
import cn.iocoder.dashboard.framework.sms.core.client.dto.SmsSendRespDTO;

import javax.servlet.ServletRequest;
import java.util.List;

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
     * @param logId 日志编号
     * @param mobile 手机号
     * @param apiTemplateId 短信 API 的模板编号
     * @param templateParams 短信模板参数
     * @return 短信发送结果
     */
    SmsCommonResult<SmsSendRespDTO> send(Long logId, String mobile, String apiTemplateId, List<KeyValue<String, Object>> templateParams);

    // TODO FROM 芋艿 to ZZF：是不是可以改成意图更明确的解析返回结果，例如说 parseXXXX
    /**
     * 短信发送回调请求处理
     *
     * @param request 请求
     * @return 短信发送结果
     */
    SmsResultDetail smsSendCallbackHandle(ServletRequest request) throws Exception;

}
