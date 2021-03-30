package cn.iocoder.dashboard.framework.sms.core.client.impl.yunpian;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.iocoder.dashboard.framework.sms.core.SmsConstants;
import cn.iocoder.dashboard.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.framework.sms.core.client.impl.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsSendFailureTypeEnum;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsSendStatusEnum;
import cn.iocoder.dashboard.util.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.constant.YunpianConstant;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static com.yunpian.sdk.constant.Code.*;

/**
 * 云片短信客户端的实现类
 *
 * @author zzf
 * @date 9:48 2021/3/5
 */
@Slf4j
public class YunpianSmsClient extends AbstractSmsClient {

    /**
     * 云信短信客户端
     */
    private volatile YunpianClient client;

    private final TypeReference<List<Map<String, String>>> callbackType = new TypeReference<List<Map<String, String>>>() {
    };

    public YunpianSmsClient(SmsChannelProperties properties) {
        super(properties);
    }

    @Override
    public void doInit() {
        YunpianClient oldClient = client;
        // 初始化新的客户端
        YunpianClient newClient = new YunpianClient(properties.getApiKey());
        newClient.init();
        this.client = newClient;
        // 销毁老的客户端
        if (oldClient != null) {
            oldClient.close();
        }
    }

    @Override
    protected SmsCommonResult doSend(Long sendLogId, String mobile, String apiTemplateId, Map<String, Object> templateParams) throws Throwable {
        // 构建参数
        Map<String, String> request = new HashMap<>();
        request.put(YunpianConstant.APIKEY, properties.getApiKey());
        request.put(YunpianConstant.MOBILE, mobile);
        request.put(YunpianConstant.TPL_ID, apiTemplateId);
        request.put(YunpianConstant.TPL_VALUE, formatTplValue(templateParams));
        request.put(YunpianConstant.UID, String.valueOf(sendLogId));
        request.put(Helper.CALLBACK, properties.getCallbackUrl());

        // 执行发送
        Result<SmsSingleSend> sendResult = client.sms().tpl_single_send(request);
        if (sendResult.getThrowable() != null) {
            throw sendResult.getThrowable();
        }
        // 解析结果
        return SmsCommonResult.success(parseSendFailureType(sendResult), // 将 API 短信平台，解析成统一的错误码
                String.valueOf(sendResult.getCode()), formatResultMsg(sendResult), null, getApiSerialNo(sendResult));
    }

    private static String formatTplValue(Map<String, Object> templateParams) {
        if (CollUtil.isEmpty(templateParams)) {
            return "";
        }
        // 参考 https://www.yunpian.com/official/document/sms/zh_cn/introduction_demos_encode_sample 格式化
        StringJoiner joiner = new StringJoiner("&");
        templateParams.forEach((key, value) -> joiner.add(String.format("#%s#=%s", key, URLUtil.encode(String.valueOf(value)))));
        return joiner.toString();
    }

    private static String formatResultMsg(Result<SmsSingleSend> sendResult) {
        if (StrUtil.isEmpty(sendResult.getDetail())) {
            return sendResult.getMsg();
        }
        return sendResult.getMsg() + " => " + sendResult.getDetail();
    }

    private static SmsSendFailureTypeEnum parseSendFailureType(Result<SmsSingleSend> sendResult) {
        Integer code = sendResult.getCode();
        switch (code) {
            case OK: return null;
            case ARGUMENT_MISSING: return SmsSendFailureTypeEnum.SMS_API_PARAM_ERROR;
            case BAD_ARGUMENT_FORMAT: return SmsSendFailureTypeEnum.SMS_TEMPLATE_PARAM_ERROR;
            case TPL_NOT_FOUND: return SmsSendFailureTypeEnum.SMS_TEMPLATE_NOT_EXISTS;
            case TPL_NOT_VALID: return SmsSendFailureTypeEnum.SMS_TEMPLATE_INVALID;
        }
        return SmsSendFailureTypeEnum.SMS_UNKNOWN;
    }

    private static String getApiSerialNo(Result<SmsSingleSend> sendResult) {
        if (sendResult.getData() == null) {
            return null;
        }
        return String.valueOf(sendResult.getData().getSid());
    }

    /**
     * 云片的比较复杂，又是加密又是套娃的
     */
    @Override
    public SmsResultDetail smsSendCallbackHandle(ServletRequest request) throws UnsupportedEncodingException {
        Map<String, String> map = getRequestParams(request);
        return Helper.getSmsResultDetailByParam(map);
    }

    /**
     * 从 request 中获取请求中传入的短信发送结果信息
     *
     * @param request 回调请求
     * @return 短信发送结果信息
     * @throws UnsupportedEncodingException 解码异常
     */
    private Map<String, String> getRequestParams(ServletRequest request) throws UnsupportedEncodingException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String[] smsStatuses = parameterMap.get(YunpianConstant.SMS_STATUS);
        String encode = URLEncoder.encode(smsStatuses[0], CharsetUtil.UTF_8);
        List<Map<String, String>> paramList = JsonUtils.parseByType(encode, callbackType);
        if (CollectionUtil.isNotEmpty(paramList)) {
            return paramList.get(0);
        }
        throw new IllegalArgumentException("YunpianSmsClient getRequestParams fail! can't format RequestParam: "
                + JsonUtils.toJsonString(request.getParameterMap()));
    }

    /**
     * 云片的回调函数的一些辅助方法
     */
    private static class Helper {

        //短信唯一标识
        private final static String API_ID = "sid";

        //回调地址·
        private final static String CALLBACK = "callback";

        //手机号
        private final static String MOBILE = "mobile";

        //错误信息
        private final static String ERROR_MSG = "error_msg";

        //用户接收时间 字符串 标准格式
        private final static String USER_RECEIVE_TIME = "user_receive_time";

        //发送状态
        private final static String REPORT_STATUS = "report_status";

        private static int getSendStatus(Map<String, String> map) {
            String reportStatus = map.get(REPORT_STATUS);
            return SmsConstants.SUCCESS.equals(reportStatus)
                    ? SysSmsSendStatusEnum.SUCCESS.getStatus()
                    : SysSmsSendStatusEnum.FAILURE.getStatus();
        }

        public static SmsResultDetail getSmsResultDetailByParam(Map<String, String> map) {
            SmsResultDetail detail = new SmsResultDetail();
            detail.setPhone(map.get(MOBILE));
            detail.setMessage(map.get(ERROR_MSG));
            detail.setSendTime(DateUtil.parseTime(map.get(USER_RECEIVE_TIME)));
            detail.setSendStatus(getSendStatus(map));
            detail.setApiId(API_ID);

            detail.setCallbackResponseBody(SmsConstants.SUCCESS);
            return detail;
        }
    }
}
