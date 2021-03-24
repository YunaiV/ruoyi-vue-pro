package cn.iocoder.dashboard.framework.sms.client.impl.yunpian;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsConstants;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsSendStatusEnum;
import cn.iocoder.dashboard.util.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.constant.Code;
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

/**
 * 云片短信实现类
 *
 * @author zzf
 * @date 9:48 2021/3/5
 */
@Slf4j
public class YunpianSmsClient extends AbstractSmsClient {

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
    public SmsResult doSend(String templateApiId, SmsBody smsBody, String targetPhone) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(YunpianConstant.APIKEY, getProperty().getApiKey());
        paramMap.put(YunpianConstant.MOBILE, String.join(SmsConstants.COMMA, targetPhone));
        paramMap.put(YunpianConstant.TEXT, formatContent(smsBody));
        paramMap.put(Helper.CALLBACK, getProperty().getCallbackUrl());

        Result<SmsSingleSend> sendResult = client.sms().single_send(paramMap);
        boolean success = sendResult.getCode().equals(Code.OK);

        if (!success) {
            log.debug("send fail[code={}, message={}]", sendResult.getCode(), sendResult.getDetail());
        }
        return new SmsResult()
                .setSuccess(success)
                .setMessage(sendResult.getDetail())
                .setCode(sendResult.getCode().toString())
                .setApiId(sendResult.getData().getSid().toString());
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
                    ? SysSmsSendStatusEnum.SEND_SUCCESS.getStatus()
                    : SysSmsSendStatusEnum.SEND_FAIL.getStatus();
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
