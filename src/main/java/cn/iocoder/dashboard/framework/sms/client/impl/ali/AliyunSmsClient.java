package cn.iocoder.dashboard.framework.sms.client.impl.ali;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum;
import cn.iocoder.dashboard.util.json.JsonUtils;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阿里短信实现类
 *
 * @author zzf
 * @date 2021/1/25 14:17
 */
@Slf4j
public class AliyunSmsClient extends AbstractSmsClient {

    private static final String OK = "OK";

    private static final String PRODUCT = "Dystopi";

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
    public SmsResult doSend(String templateApiId, SmsBody smsBody, String targetPhone) throws Exception {
        SendSmsRequest request = new SendSmsRequest();
        request.setSysMethod(MethodType.POST);
        request.setPhoneNumbers(targetPhone);
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
                .setApiId(sendSmsResponse.getBizId());
    }

    /**
     * [{
     * "send_time" : "2017-08-30 00:00:00",
     * "report_time" : "2017-08-30 00:00:00",
     * "success" : true,
     * "err_msg" : "用户接收成功",
     * "err_code" : "DELIVERED",
     * "phone_number" : "18612345678",
     * "sms_size" : "1",
     * "biz_id" : "932702304080415357^0",
     * "out_id" : "1184585343"
     * }]
     *
     * @param request 请求
     * @return
     * @throws Exception
     */
    @Override
    public SmsResultDetail smsSendCallbackHandle(ServletRequest request) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String paramStr = reader.readLine();
        List<Map<String, Object>> params = JsonUtils.parseByType(paramStr, new TypeReference<List<Map<String, Object>>>() {
        });
        if (CollectionUtil.isNotEmpty(params)) {
            Map<String, Object> sendResultParamMap = params.get(0);
            return CallbackHelper.of(sendResultParamMap).toResultDetail();
        }
        return null;
    }

    /**
     * 短信发送回调辅助类
     */
    private static class CallbackHelper {

        private final Map<String, Object> sendResultParamMap;

        private CallbackHelper(Map<String, Object> sendResultParamMap) {
            this.sendResultParamMap = sendResultParamMap;
        }

        public static CallbackHelper of(Map<String, Object> sendResultParamMap) {
            return new CallbackHelper(sendResultParamMap);
        }

        public Integer getSendStatus() {
            return ((Boolean) sendResultParamMap.get(CallbackField.SUCCESS))
                    ? SmsSendStatusEnum.SEND_SUCCESS.getStatus()
                    : SmsSendStatusEnum.SEND_FAIL.getStatus();
        }

        public String getBizId() {
            return sendResultParamMap.get(CallbackField.BIZ_ID).toString();
        }

        public String getErrMsg() {
            return sendResultParamMap.get(CallbackField.ERR_MSG).toString();
        }

        public String getErrCode() {
            return sendResultParamMap.get(CallbackField.ERR_CODE).toString();
        }

        public Date getSendTime() {
            return DateUtil.parseTime(sendResultParamMap.get(CallbackField.SEND_TIME).toString());
        }

        public String getPhoneNumber() {
            return sendResultParamMap.get(CallbackField.PHONE_NUMBER).toString();
        }

        public String getOutId() {
            return sendResultParamMap.get(CallbackField.OUT_ID).toString();
        }

        public SmsResultDetail toResultDetail() {
            SmsResultDetail resultDetail = new SmsResultDetail();
            resultDetail.setSendStatus(getSendStatus());
            resultDetail.setApiId(getBizId());
            resultDetail.setSendTime(getSendTime());
            resultDetail.setPhone(getPhoneNumber());
            resultDetail.setMessage(getErrMsg());

            resultDetail.setCallbackResponseBody(generateSuccessResponseBody());
            return resultDetail;
        }

        /**
         * 生成回调成功的返回对象
         */
        private Map<String, Object> generateSuccessResponseBody() {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 0);
            result.put("msg", "成功");
            return result;
        }

    }

    /**
     * 回调接口字段定义
     */
    private interface CallbackField {
        //是否成功 boolean
        String SUCCESS = "success";

        //发送时间
        String SEND_TIME = "send_time";

        //错误信息
        String ERR_MSG = "err_msg";

        //错误编码
        String ERR_CODE = "err_code";

        //手机号
        String PHONE_NUMBER = "phone_number";

        //用户序列号 out_id
        String OUT_ID = "out_id";

        //biz_id 即 apiId 唯一标识
        String BIZ_ID = "biz_id";
    }

}
