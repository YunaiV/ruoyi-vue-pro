package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import cn.hutool.json.JSONArray;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.utils.HuaweiRequest;
import cn.iocoder.yudao.module.system.framework.sms.core.client.utils.HuaweiSigner;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


import java.time.LocalDateTime;


import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;


/**
 * 华为短信客户端的实现类
 *
 * @author scholar
 * @since 2024/6/02 11:55
 */
@Slf4j
public class HuaweiSmsClient extends AbstractSmsClient {

    /**
     * 调用成功 code
     */
    public static final String API_CODE_SUCCESS = "OK";
    private static final Logger LOGGER = LoggerFactory.getLogger(HuaweiSmsClient.class);
    public HuaweiSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }
    @Override
    protected void doInit() {

    }
    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        String url = "https://smsapi.cn-north-4.myhuaweicloud.com:443/sms/batchSendSms/v1"; //APP接入地址+接口访问URI
        // 相比较阿里短信，华为短信发送的时候需要额外的参数“通道号”，考虑到不破坏原有的的结构
        // 所以将 通道号 拼接到 apiTemplateId 字段中，格式为 "apiTemplateId 通道号"。空格为分隔符。
        String sender = StrUtil.subAfter(apiTemplateId, " ", true); //中国大陆短信签名通道号或全球短信通道号
        String templateId = StrUtil.subBefore(apiTemplateId, " ", true); //模板ID

        //必填,全局号码格式(包含国家码),示例:+86151****6789,多个号码之间用英文逗号分隔
        String receiver = mobile; //短信接收人号码

        //选填,短信状态报告接收地址,推荐使用域名,为空或者不填表示不接收状态报告
        String statusCallBack = properties.getCallbackUrl();

        /**
         * 选填,使用无变量模板时请赋空值 String templateParas = "";
         * 单变量模板示例:模板内容为"您的验证码是${NUM_6}"时,templateParas可填写为"[\"111111\"]"
         * 双变量模板示例:模板内容为"您有${NUM_2}件快递请到${TXT_20}领取"时,templateParas可填写为"[\"3\",\"人民公园正门\"]"
         */
        List<String> templateParas = new ArrayList<>();
        for (KeyValue<String, Object> kv : templateParams) {
            templateParas.add(String.valueOf(kv.getValue()));
        }

        //请求Body,不携带签名名称时,signature请填null
        String body = buildRequestBody(sender, receiver, templateId, templateParas, statusCallBack, null);
        if (null == body || body.isEmpty()) {
            LOGGER.warn("body is null.");
            return null;
        }

        HuaweiRequest request = new HuaweiRequest();
        request.setKey(properties.getApiKey());
        request.setSecret(properties.getApiSecret());
        request.setMethod("POST");
        request.setUrl(url);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setBody(body);
        LOGGER.info("Print the body: {}", body);

        HuaweiSigner signer = new HuaweiSigner("SDK-HMAC-SHA256");
        signer.sign(request);
        HttpUriRequest postMethod = RequestBuilder.post()
                .setUri(url)
                .setEntity(new StringEntity(request.getBody(), StandardCharsets.UTF_8))
                .setHeader("Content-Type","application/x-www-form-urlencoded")
                .setHeader("X-Sdk-Date",request.getHeaders().get("X-Sdk-Date"))
                .setHeader("Authorization",request.getHeaders().get("Authorization"))
                .build();
        CloseableHttpClient client = HttpClientBuilder.create().build();

        HttpResponse response = client.execute(postMethod);
        return new SmsSendRespDTO().setSuccess(Objects.equals(response.getStatusLine().getReasonPhrase(), API_CODE_SUCCESS)).setSerialNo(Integer.toString(response.getStatusLine().getStatusCode()))
                .setApiRequestId(null).setApiCode(null).setApiMsg(null);
    }

    static String buildRequestBody(String sender, String receiver, String templateId, List<String> templateParas,
                                   String statusCallBack, String signature) throws UnsupportedEncodingException {
        if (null == sender || null == receiver || null == templateId || sender.isEmpty() || receiver.isEmpty()
                || templateId.isEmpty()) {
            System.out.println("buildRequestBody(): sender, receiver or templateId is null.");
            return null;
        }

        StringBuilder body = new StringBuilder();
        appendToBody(body, "from=", sender);
        appendToBody(body, "&to=", receiver);
        appendToBody(body, "&templateId=", templateId);
        appendToBody(body, "&templateParas=", new JSONArray(templateParas).toString());
        appendToBody(body, "&statusCallback=", statusCallBack);
        appendToBody(body, "&signature=", signature);
        return body.toString();
    }

    private static void appendToBody(StringBuilder body, String key, String val) throws UnsupportedEncodingException {
        if (null != val && !val.isEmpty()) {
            LOGGER.info("Print appendToBody: {}:{}", key, val);
            body.append(key).append(URLEncoder.encode(val, "UTF-8"));
        }
    }
    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        List<SmsReceiveStatus> statuses = JsonUtils.parseArray(text, SmsReceiveStatus.class);
        return convertList(statuses, status -> new SmsReceiveRespDTO().setSuccess(Objects.equals(status.getStatus(),"DELIVRD"))
                .setErrorCode(status.getStatus()).setErrorMsg(status.getStatus())
                .setMobile(status.getPhoneNumber()).setReceiveTime(status.getUpdateTime())
                .setSerialNo(status.getSmsMsgId()));
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        //华为短信模板查询和发送短信，是不同的两套key和secret，与阿里、腾讯的区别较大，这里模板查询校验暂不实现。
        return new SmsTemplateRespDTO().setId(null).setContent(null)
                .setAuditStatus(SmsTemplateAuditStatusEnum.SUCCESS.getStatus()).setAuditReason(null);

    }

    /**
     * 短信接收状态
     *
     * 参见 <a href="https://support.huaweicloud.com/api-msgsms/sms_05_0003.html">文档</a>
     *
     * @author scholar
     */
    @Data
    public static class SmsReceiveStatus {

        /**
         * 本条状态报告对应的短信的接收方号码，仅当状态报告中携带了extend参数时才会同时携带该参数
         */
        @JsonProperty("to")
        private String phoneNumber;

        /**
         * 短信资源的更新时间，通常为短信平台接收短信状态报告的时间
         */
        @JsonProperty("updateTime")
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private LocalDateTime updateTime;

        /**
         * 短信状态报告枚举值
         */
        @JsonProperty("status")
        private String status;

        /**
         * 发送短信成功时返回的短信唯一标识。
         */
        @JsonProperty("smsMsgId")
        private String smsMsgId;
    }

}
