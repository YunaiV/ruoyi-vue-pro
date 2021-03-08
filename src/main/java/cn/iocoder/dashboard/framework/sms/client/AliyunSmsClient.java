package cn.iocoder.dashboard.framework.sms.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 阿里短信实现类
 *
 * @author zzf
 * @date 2021/1/25 14:17
 */
@Slf4j
public class AliyunSmsClient extends AbstractSmsClient implements NeedQuerySendResultSmsClient {

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
    public SmsResult doSend(String templateApiId, SmsBody smsBody, Collection<String> targets) throws Exception {
        SendSmsRequest request = new SendSmsRequest();
        request.setSysMethod(MethodType.POST);
        request.setPhoneNumbers(ArrayUtil.join(targets, ","));
        request.setSignName(channelVO.getApiSignatureId());
        request.setTemplateCode(templateApiId);
        request.setTemplateParam(smsBody.getParamsStr());
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

        boolean success = OK.equals(sendSmsResponse.getCode());
        if (!success) {
            log.debug("send fail[code={}, message={}]", sendSmsResponse.getCode(), sendSmsResponse.getMessage());
        }
        return new SmsResult()
                .setSuccess(success)
                .setMessage(sendSmsResponse.getMessage())
                .setCode(sendSmsResponse.getCode())
                .setApiId(sendSmsResponse.getBizId())
                .setSendResultParam(sendSmsResponse.getBizId());
    }


    @Override
    public List<SmsResultDetail> getSmsSendResult(String param) throws ClientException {
        QuerySendDetailsRequest querySendDetailsRequest = new QuerySendDetailsRequest();
        querySendDetailsRequest.setBizId(param);
        // TODO FROM 芋艿 to zzf：发送完之后，基于短信平台回调，去更新回执状态。短信发送是否成功，和最终用户收到，是两个维度。这块有困惑，可以微信，我给个截图哈。 DONE
        QuerySendDetailsResponse acsResponse = acsClient.getAcsResponse(querySendDetailsRequest);
        List<SmsResultDetail> resultDetailList = new ArrayList<>(Integer.parseInt(acsResponse.getTotalCount()));
        acsResponse.getSmsSendDetailDTOs().forEach(s -> {
            SmsResultDetail resultDetail = new SmsResultDetail();
            resultDetail.setSendTime(DateUtil.parseDateTime(s.getSendDate()));
            resultDetail.setMessage(s.getContent());
            resultDetail.setPhone(s.getPhoneNum());
            resultDetail.setSendStatus(statusConvert(s.getSendStatus()));
            resultDetailList.add(resultDetail);
        });
        return resultDetailList;
    }

    private int statusConvert(Long aliSendStatus) {
        if (aliSendStatus == 1L) {
            return SmsSendStatusEnum.SEND_SUCCESS.getStatus();
        }
        if (aliSendStatus == 2L) {
            return SmsSendStatusEnum.SEND_FAIL.getStatus();
        }
        return SmsSendStatusEnum.WAITING.getStatus();
    }

}
