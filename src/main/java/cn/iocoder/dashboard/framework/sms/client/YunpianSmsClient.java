package cn.iocoder.dashboard.framework.sms.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsConstants;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum;
import cn.iocoder.dashboard.util.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.constant.Code;
import com.yunpian.sdk.constant.YunpianConstant;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsBatchSend;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 云片短信实现类
 *
 * @author zzf
 * @date 9:48 2021/3/5
 */
@Slf4j
public class YunpianSmsClient extends AbstractSmsClient implements HadCallbackSmsClient {

    private final YunpianClient client;

    private final TypeReference<List<Map<String, String>>> callbackType = new TypeReference<List<Map<String, String>>>() {
    };

    /**
     * 构造云片短信发送处理
     *
     * @param channelVO 阿里云短信配置
     */
    public YunpianSmsClient(SmsChannelProperty channelVO) {
        super(channelVO);
        client = new YunpianClient(channelVO.getApiKey());
    }

    @Override
    public SmsResult doSend(String templateApiId, SmsBody smsBody, Collection<String> targets) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("apikey", getProperty().getApiKey());
        paramMap.put("mobile", String.join(SmsConstants.COMMA, targets));
        paramMap.put("text", formatContent(smsBody));
        paramMap.put("callback", getProperty().getCallbackUrl());

        Result<SmsBatchSend> sendResult = client.sms().batch_send(paramMap);
        boolean success = sendResult.getCode().equals(Code.OK);

        if (!success) {
            log.debug("send fail[code={}, message={}]", sendResult.getCode(), sendResult.getDetail());
        }
        return new SmsResult()
                .setSuccess(success)
                .setMessage(sendResult.getDetail())
                .setCode(sendResult.getCode().toString())
                .setApiId(sendResult.getData().getData().get(0).getSid().toString());
    }


    /**
     * 格式化短信内容，将参数注入到模板中
     *
     * @param smsBody 短信信息
     * @return 格式化后的短信内容
     */
    private String formatContent(SmsBody smsBody) {
        StringBuilder result = new StringBuilder(smsBody.getTemplateContent());
        smsBody.getParams().forEach((key, val) -> {
            String param = parseParamToPlaceholder(key);
            result.replace(result.indexOf(param), result.indexOf(param + param.length()), val);
        });
        return result.toString();
    }

    /**
     * 将指定参数改成对应的占位字符
     * <p>
     * 云片的是 #param# 的形式作为占位符
     *
     * @param key 参数名
     * @return 对应的占位字符
     */
    private String parseParamToPlaceholder(String key) {
        return SmsConstants.JING_HAO + key + SmsConstants.JING_HAO;
    }


    @Override
    public List<SmsResultDetail> getSmsSendResult(ServletRequest request) throws UnsupportedEncodingException {
        List<Map<String, String>> stringStringMap = getSendResult(request);
        List<SmsResultDetail> resultDetailList = new ArrayList<>(stringStringMap.size());
        stringStringMap.forEach(map -> {
            SmsResultDetail detail = new SmsResultDetail();

            detail.setPhone(map.get("mobile"));
            detail.setMessage(map.get("error_msg"));
            detail.setSendTime(DateUtil.parseTime(map.get("user_receive_time")));
            String reportStatus = map.get("report_status");
            detail.setSendStatus(reportStatus.equals(SmsConstants.SUCCESS)
                    ? SmsSendStatusEnum.SEND_SUCCESS.getStatus()
                    : SmsSendStatusEnum.SEND_FAIL.getStatus()
            );
            resultDetailList.add(detail);
        });
        return resultDetailList;
    }

    /**
     * 从 request 中获取请求中传入的短信发送结果信息
     *
     * @param request 回调请求
     * @return 短信发送结果信息
     * @throws UnsupportedEncodingException 解码异常
     */
    private List<Map<String, String>> getSendResult(ServletRequest request) throws UnsupportedEncodingException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String[] smsStatuses = parameterMap.get(YunpianConstant.SMS_STATUS);
        String encode = URLEncoder.encode(smsStatuses[0], CharsetUtil.UTF_8);
        return JsonUtils.parseByType(encode, callbackType);
    }
}
