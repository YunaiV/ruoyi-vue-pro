package cn.iocoder.dashboard.framework.sms.client;

import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;

import javax.servlet.ServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 需要发送请求获取短信发送结果的短信客户端
 *
 * @author zzf
 * @date 2021/3/4 17:20
 */
public interface HadCallbackSmsClient {

    /**
     * 获取短信发送结果
     *
     * @param request 请求
     * @return 短信发送结果
     */
    List<SmsResultDetail> getSmsSendResult(ServletRequest request) throws Exception;

}
