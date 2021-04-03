package cn.iocoder.dashboard.framework.sms.core.client.impl.yunpian;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.iocoder.dashboard.common.core.KeyValue;
import cn.iocoder.dashboard.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.dashboard.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.dashboard.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.dashboard.framework.sms.core.client.impl.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.dashboard.util.date.DateUtils;
import cn.iocoder.dashboard.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.constant.YunpianConstant;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

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
        super(properties, new YunpianSmsCodeMapping());
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
    protected SmsCommonResult<SmsSendRespDTO> doSendSms(Long sendLogId, String mobile,
                                                        String apiTemplateId, List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 构建参数
        Map<String, String> request = new HashMap<>();
        request.put(YunpianConstant.APIKEY, properties.getApiKey());
        request.put(YunpianConstant.MOBILE, mobile);
        request.put(YunpianConstant.TPL_ID, apiTemplateId);
        request.put(YunpianConstant.TPL_VALUE, formatTplValue(templateParams));
        request.put(YunpianConstant.UID, String.valueOf(sendLogId));
        request.put(YunpianConstant.CALLBACK_URL, properties.getCallbackUrl());

        // 执行发送
        Result<SmsSingleSend> sendResult = client.sms().tpl_single_send(request);
        if (sendResult.getThrowable() != null) {
            throw sendResult.getThrowable();
        }
        // 解析结果
        SmsSendRespDTO data = null;
        if (sendResult.getData() != null) {
            data = new SmsSendRespDTO().setSerialNo(String.valueOf(sendResult.getData().getSid()));
        }
        return SmsCommonResult.build(String.valueOf(sendResult.getCode()), formatResultMsg(sendResult), null,
                data, codeMapping);
    }

    private static String formatTplValue(List<KeyValue<String, Object>> templateParams) {
        if (CollUtil.isEmpty(templateParams)) {
            return "";
        }
        // 参考 https://www.yunpian.com/official/document/sms/zh_cn/introduction_demos_encode_sample 格式化
        StringJoiner joiner = new StringJoiner("&");
        templateParams.forEach(param -> joiner.add(String.format("#%s#=%s", param.getKey(), URLUtil.encode(String.valueOf(param.getValue())))));
        return joiner.toString();
    }

    private static String formatResultMsg(Result<SmsSingleSend> sendResult) {
        if (StrUtil.isEmpty(sendResult.getDetail())) {
            return sendResult.getMsg();
        }
        return sendResult.getMsg() + " => " + sendResult.getDetail();
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
        List<Map<String, String>> paramList = JsonUtils.parseObject(encode, callbackType);
        if (CollectionUtil.isNotEmpty(paramList)) {
            return paramList.get(0);
        }
        throw new IllegalArgumentException("YunpianSmsClient getRequestParams fail! can't format RequestParam: "
                + JsonUtils.toJsonString(request.getParameterMap()));
    }

    @Override
    protected SmsCommonResult<SmsReceiveRespDTO> doParseSmsReceiveStatus(String text) throws Throwable {
        return null;
    }

    /**
     * 短信接收状态
     *
     * 参见 https://www.yunpian.com/official/document/sms/zh_cn/domestic_push_report 文档
     *
     * @author 芋道源码
     */
    @Data
    public static class SmsReceiveStatus {

        /**
         * 运营商反馈代码的中文解释
         *
         * 默认不推送此字段，如需推送，请联系客服
         */
        @JsonProperty("error_detail")
        private String errorDetail;
        /**
         * 短信编号
         */
        private Long sid;
        /**
         * 用户自定义 id
         *
         * 这里我们传递的是 SysSmsLogDO 的日志编号
         */
        private Long uid;
        /**
         * 用户接收时间
         */
        @JsonProperty("user_receive_time")
        @JsonFormat(pattern = DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private Date userReceiveTime;
        /**
         * 运营商返回的代码，如："DB:0103"
         *
         * 由于不同运营商信息不同，此字段仅供参考；
         */
        @JsonProperty("error_msg")
        private String errorMsg;
        /**
         * 接收手机号
         */
        private String mobile;
        /**
         * 接收状态
         *
         * 目前仅有 SUCCESS / FAIL，所以使用 Boolean 接收
         */
        @JsonProperty("report_status")
        private String reportStatus;

    }

}
