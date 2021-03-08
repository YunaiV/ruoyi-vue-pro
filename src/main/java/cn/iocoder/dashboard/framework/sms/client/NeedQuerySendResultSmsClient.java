package cn.iocoder.dashboard.framework.sms.client;

import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import com.aliyuncs.exceptions.ClientException;

import java.util.List;

/**
 * 需要发送请求获取短信发送结果的短信客户端
 *
 * @author zzf
 * @date 2021/3/4 17:20
 */
public interface NeedQuerySendResultSmsClient {

    /**
     * 获取短信发送结果
     *
     * @param param 参数
     * @return 短信发送结果
     */
    List<SmsResultDetail> getSmsSendResult(String param) throws Exception;

}
