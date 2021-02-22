package cn.iocoder.dashboard.framework.sms.client;

import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * 阿里短信实现类
 *
 * @author zzf
 * @date 2021/1/25 14:17
 */
@Slf4j
public class AliyunSmsClient extends AbstractSmsClient<SendSmsResponse> {

    private static final String OK = "OK";

    private static final String PRODUCT = "Dysmsapi";

    private static final String DOMAIN = "dysmsapi.aliyuncs.com";

    private static final String ENDPOINT = "cn-hangzhou";

    private final IAcsClient acsClient;

    /**
     * 构造阿里云短信发送处理
     *
     * @param channelVO 阿里云短信配置
     */
    public AliyunSmsClient(SmsChannelProperty channelVO) {
        super(channelVO);

        String accessKeyId = channelVO.getApiKey();
        String accessKeySecret = channelVO.getApiSecret();

        IClientProfile profile = DefaultProfile.getProfile(ENDPOINT, accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint(ENDPOINT, PRODUCT, DOMAIN);

        acsClient = new DefaultAcsClient(profile);
    }


    @Override
    public SmsResult<SendSmsResponse> send(SmsBody smsBody, Collection<String> targets) {
        SendSmsRequest request = new SendSmsRequest();
        request.setSysMethod(MethodType.POST);
        request.setPhoneNumbers(StringUtils.join(targets, ","));
        request.setSignName(channelVO.getApiSignatureId());
        request.setTemplateCode(channelVO.getTemplateByTemplateCode(smsBody.getTemplateCode()).getApiTemplateId());
        request.setTemplateParam(smsBody.getParamsStr());
        // TODO FROM 芋艿 TO zzf：try catch 咱是不是可以交给 abstract 来做。这样，异常处理，重试，限流等等，都可以酱紫
        try {
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

            boolean result = OK.equals(sendSmsResponse.getCode());
            if (!result) {
                log.debug("send fail[code={}, message={}]", sendSmsResponse.getCode(), sendSmsResponse.getMessage());
            }
            SmsResult<SendSmsResponse> resultBody = new SmsResult<>();
            resultBody.setSuccess(result);
            resultBody.setResult(sendSmsResponse);
            return resultBody;
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return failResult("发送异常: " + e.getMessage());
        }
    }

    SmsResult<SendSmsResponse> failResult(String message) {
        SmsResult<SendSmsResponse> resultBody = new SmsResult<>();
        resultBody.setSuccess(false);
        resultBody.setMessage(message);
        return resultBody;
    }

}
