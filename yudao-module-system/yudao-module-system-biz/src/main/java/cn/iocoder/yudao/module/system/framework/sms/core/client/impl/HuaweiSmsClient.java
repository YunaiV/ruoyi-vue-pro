package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

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
        // TODO @scholar：https://smsapi.cn-north-4.myhuaweicloud.com:443 是不是枚举成静态变量
        String url = "https://smsapi.cn-north-4.myhuaweicloud.com:443/sms/batchSendSms/v1"; //APP接入地址+接口访问URI
        // 相比较阿里短信，华为短信发送的时候需要额外的参数“通道号”，考虑到不破坏原有的的结构
        // 所以将 通道号 拼接到 apiTemplateId 字段中，格式为 "apiTemplateId 通道号"。空格为分隔符。
        // TODO @scholar：暂时只考虑中国大陆，所以不需要 sender 哈
        String sender = StrUtil.subAfter(apiTemplateId, " ", true); //中国大陆短信签名通道号或全球短信通道号
        String templateId = StrUtil.subBefore(apiTemplateId, " ", true); //模板ID

        // 选填,短信状态报告接收地址,推荐使用域名,为空或者不填表示不接收状态报告
        String statusCallBack = properties.getCallbackUrl();

        // TODO @scholar：1）是不是用  LocalDateTimeUtil.format()；这样 3 行变成一行
        // TODO @scholar：singerDate 叫 sdkDate 会更合适哈，这样理解起来简单。另外，singer 应该是 signed 么？
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String singerDate = sdf.format(new Date());

        // TODO @scholar：整个处理加密的过程，是不是应该抽成一个 private 方法哈。这样整个调用的主干更清晰。
        // ************* 步骤 1：拼接规范请求串 *************
        String httpRequestMethod = "POST";
        String canonicalUri = "/sms/batchSendSms/v1/";
        String canonicalQueryString = ""; // 查询参数为空
        String canonicalHeaders = "content-type:application/x-www-form-urlencoded\n"
                + "host:smsapi.cn-north-4.myhuaweicloud.com:443\n"
                + "x-sdk-date:" + singerDate + "\n";
        // TODO @scholar：静态枚举了
        String signedHeaders = "content-type;host;x-sdk-date";
        // TODO @scholar：下面的注释，可以考虑去掉
        /*
         * 选填,使用无变量模板时请赋空值 String templateParas = "";
         * 单变量模板示例:模板内容为"您的验证码是${NUM_6}"时,templateParas可填写为"[\"111111\"]"
         * 双变量模板示例:模板内容为"您有${NUM_2}件快递请到${TXT_20}领取"时,templateParas可填写为"[\"3\",\"人民公园正门\"]"
         */
        // TODO @scholar：CollectionUtils.convertList 可以把 4 行变成 1 行。
        // TODO @scholar：templateParams 拼写错误哈
        List<String> templateParas = new ArrayList<>();
        for (KeyValue<String, Object> kv : templateParams) {
            templateParas.add(String.valueOf(kv.getValue()));
        }

        // 请求Body,不携带签名名称时,signature请填null
        String body = buildRequestBody(sender, mobile, templateId, templateParas, statusCallBack, null);
        // TODO @scholar：Assert 断言，抛出异常
        if (null == body || body.isEmpty()) {
            return null;
        }
        String hashedRequestBody = HexUtil.encodeHexStr(DigestUtil.sha256(body));
        String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
                + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestBody;

        // ************* 步骤 2：拼接待签名字符串 *************
        // TODO @scholar：sha256Hex 是不是更简洁哈
        String hashedCanonicalRequest = HexUtil.encodeHexStr(DigestUtil.sha256(canonicalRequest));
        String stringToSign = "SDK-HMAC-SHA256" + "\n" + singerDate + "\n" + hashedCanonicalRequest;

        // ************* 步骤 3：计算签名 *************
        String signature = SecureUtil.hmacSha256(properties.getApiSecret()).digestHex(stringToSign);

        // ************* 步骤 4：拼接 Authorization *************
        String authorization = "SDK-HMAC-SHA256" + " " + "Access=" + properties.getApiKey() + ", "
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;

        // ************* 步骤 5：构造HttpRequest 并执行request请求，获得response *************
        // TODO @scholar：考虑了下，还是换 hutool 的 httpUtils。因为未来 httpclient 我们可能会移除掉
        HttpUriRequest postMethod = RequestBuilder.post()
                .setUri(url)
                .setEntity(new StringEntity(body, StandardCharsets.UTF_8))
                .setHeader("Content-Type","application/x-www-form-urlencoded")
                .setHeader("X-Sdk-Date", singerDate)
                .setHeader("Authorization", authorization)
                .build();
        // TODO @scholar：这种不太适合一直 new 的哈
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(postMethod);
        // TODO @scholar：失败的情况下的处理
        // TODO @scholar：setSerialNo(Integer.toString(response.getStatusLine().getStatusCode())) 这部分，空一行。一行代码太多了，阅读性不太好哈
        return new SmsSendRespDTO().setSuccess(Objects.equals(response.getStatusLine().getReasonPhrase(), API_CODE_SUCCESS)).setSerialNo(Integer.toString(response.getStatusLine().getStatusCode()))
                .setApiRequestId(null).setApiCode(null).setApiMsg(null);
    }

    static String buildRequestBody(String sender, String receiver, String templateId, List<String> templateParas,
                                   String statusCallBack, @SuppressWarnings("SameParameterValue") String signature) throws UnsupportedEncodingException {
        // TODO @scholar：参数不满足，是不是抛出异常更好哈；通过 hutool 的 Assert 去断言
        if (null == sender || null == receiver || null == templateId || sender.isEmpty() || receiver.isEmpty()
                || templateId.isEmpty()) {
            System.out.println("buildRequestBody(): sender, receiver or templateId is null.");
            return null;
        }

        StringBuilder body = new StringBuilder();
        appendToBody(body, "from=", sender);
        appendToBody(body, "&to=", receiver);
        appendToBody(body, "&templateId=", templateId);
        // TODO @scholar：new JSONArray(templateParas).toString()，是不是 JsonUtils.toString 呀？
        appendToBody(body, "&templateParas=", new JSONArray(templateParas).toString());
        appendToBody(body, "&statusCallback=", statusCallBack);
        appendToBody(body, "&signature=", signature);
        return body.toString();
    }

    private static void appendToBody(StringBuilder body, String key, String val) throws UnsupportedEncodingException {
        // TODO @scholar：StrUtils.isNotEmpty(val)，是不是更简洁哈
        if (null != val && !val.isEmpty()) {
            body.append(key).append(URLEncoder.encode(val, StandardCharsets.UTF_8.name()));
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
        // 华为短信模板查询和发送短信，是不同的两套 key 和 secret，与阿里、腾讯的区别较大，这里模板查询校验暂不实现
        // 对应文档 https://support.huaweicloud.com/api-msgsms/sms_05_0040.html
        return new SmsTemplateRespDTO().setId(apiTemplateId).setContent(null)
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
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private LocalDateTime updateTime;

        /**
         * 短信状态报告枚举值
         */
        private String status;

        /**
         * 发送短信成功时返回的短信唯一标识。
         */
        private String smsMsgId;
    }

}
