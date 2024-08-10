package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import jakarta.xml.bind.DatatypeConverter;
import lombok.Data;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static cn.hutool.crypto.digest.DigestUtil.sha256Hex;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

// TODO @scholar 建议参考 AliyunSmsClient 优化下
/**
 * 腾讯云短信功能实现
 *
 * 参见 <a href="https://cloud.tencent.com/document/product/382/52077">文档</a>
 *
 * @author shiwp
 */
public class TencentSmsClient extends AbstractSmsClient {

    /**
     * 调用成功 code
     */
    public static final String API_CODE_SUCCESS = "Ok";

    /**
     * 是否国际/港澳台短信：
     *
     * 0：表示国内短信。
     * 1：表示国际/港澳台短信。
     */
    private static final long INTERNATIONAL_CHINA = 0L;


    public TencentSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
        validateSdkAppId(properties);
    }

    @Override
    protected void doInit() {

    }

    /**
     * 参数校验腾讯云的 SDK AppId
     *
     * 原因是：腾讯云发放短信的时候，需要额外的参数 sdkAppId
     *
     * 解决方案：考虑到不破坏原有的 apiKey + apiSecret 的结构，所以将 secretId 拼接到 apiKey 字段中，格式为 "secretId sdkAppId"。
     *
     * @param properties 配置
     */
    private static void validateSdkAppId(SmsChannelProperties properties) {
        String combineKey = properties.getApiKey();
        Assert.notEmpty(combineKey, "apiKey 不能为空");
        String[] keys = combineKey.trim().split(" ");
        Assert.isTrue(keys.length == 2, "腾讯云短信 apiKey 配置格式错误，请配置 为[secretId sdkAppId]");
    }

    private String getSdkAppId() {
        return StrUtil.subAfter(properties.getApiKey(), " ", true);
    }

    private String getApiKey() {
        return StrUtil.subBefore(properties.getApiKey(), " ", true);
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile,
                                  String apiTemplateId, List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 构建请求
        TreeMap<String, Object> body = new TreeMap<>();
        String[] phones = {mobile};
        body.put("PhoneNumberSet",phones);
        body.put("SmsSdkAppId",getSdkAppId());
        body.put("SignName",properties.getSignature());
        body.put("TemplateId",apiTemplateId);
        body.put("TemplateParamSet",ArrayUtils.toArray(templateParams, e -> String.valueOf(e.getValue())));

        JSONObject JsonResponse = sendSmsRequest(body,"SendSms","2021-01-11","ap-guangzhou");
        SmsResponse smsResponse = getSmsSendResponse(JsonResponse);

        return new SmsSendRespDTO().setSuccess(smsResponse.success).setApiMsg(smsResponse.data.toString());

    }

    JSONObject sendSmsRequest(TreeMap<String, Object> body,String action,String version,String region) throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 注意时区，否则容易出错
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(Long.valueOf(timestamp + "000")));

        // ************* 步骤 1：拼接规范请求串 *************
        String host = "sms.tencentcloudapi.com"; //APP接入地址+接口访问URI
        String httpMethod = "POST"; // 请求方式
        String canonicalUri = "/";
        String canonicalQueryString = "";

        String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                + "host:" + host + "\n" + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String hashedRequestBody = sha256Hex(JSONUtil.toJsonStr(body));
        String canonicalRequest = httpMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestBody;

        // ************* 步骤 2：拼接待签名字符串 *************
        String credentialScope = date + "/" + "sms" + "/" + "tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = "TC3-HMAC-SHA256" + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

        // ************* 步骤 3：计算签名 *************
        byte[] secretDate = hmac256(("TC3" + properties.getApiSecret()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, "sms");
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature = DatatypeConverter.printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();

        // ************* 步骤 4：拼接 Authorization *************
        String authorization = "TC3-HMAC-SHA256" + " " + "Credential=" + getApiKey() + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;

        // ************* 步骤 5：构造HttpRequest 并执行request请求，获得response *************
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authorization);
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Host", host);
        headers.put("X-TC-Action", action);
        headers.put("X-TC-Timestamp", timestamp);
        headers.put("X-TC-Version", version);
        headers.put("X-TC-Region", region);

        HttpResponse response = HttpRequest.post("https://"+host)
                .addHeaders(headers)
                .body(JSONUtil.toJsonStr(body))
                .execute();

        return JSONUtil.parseObj(response.body());
    }

    public static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    private SmsResponse getSmsSendResponse(JSONObject resJson) {
        SmsResponse smsResponse = new SmsResponse();
        JSONArray statusJson =resJson.getJSONObject("Response").getJSONArray("SendStatusSet");
        smsResponse.setSuccess("Ok".equals(statusJson.getJSONObject(0).getStr("Code")));
        smsResponse.setData(resJson);
        return smsResponse;
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        List<SmsReceiveStatus> callback = JsonUtils.parseArray(text, SmsReceiveStatus.class);
        return convertList(callback, status -> new SmsReceiveRespDTO()
                .setSuccess(SmsReceiveStatus.SUCCESS_CODE.equalsIgnoreCase(status.getStatus()))
                .setErrorCode(status.getErrCode()).setErrorMsg(status.getDescription())
                .setMobile(status.getMobile()).setReceiveTime(status.getReceiveTime())
                .setSerialNo(status.getSerialNo()).setLogId(status.getSessionContext().getLogId()));
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {

        // 构建请求
        TreeMap<String, Object> body = new TreeMap<>();
        body.put("International",0);
        Integer[] templateIds = {Integer.valueOf(apiTemplateId)};
        body.put("TemplateIdSet",templateIds);

        JSONObject JsonResponse = sendSmsRequest(body,"DescribeSmsTemplateList","2021-01-11","ap-guangzhou");
        QuerySmsTemplateResponse smsTemplateResponse = getSmsTemplateResponse(JsonResponse);
        String templateId = Integer.toString(smsTemplateResponse.getDescribeTemplateStatusSet().get(0).getTemplateId());
        String content = smsTemplateResponse.getDescribeTemplateStatusSet().get(0).getTemplateContent();
        Integer templateStatus = smsTemplateResponse.getDescribeTemplateStatusSet().get(0).getStatusCode();
        String auditReason = smsTemplateResponse.getDescribeTemplateStatusSet().get(0).getReviewReply();

        return new SmsTemplateRespDTO().setId(templateId).setContent(content)
                .setAuditStatus(convertSmsTemplateAuditStatus(templateStatus)).setAuditReason(auditReason);
    }

    private QuerySmsTemplateResponse getSmsTemplateResponse(JSONObject resJson) {

        QuerySmsTemplateResponse smsTemplateResponse = new QuerySmsTemplateResponse();

        smsTemplateResponse.setRequestId(resJson.getJSONObject("Response").getStr("RequestId"));

        smsTemplateResponse.setDescribeTemplateStatusSet(new ArrayList<>());

        QuerySmsTemplateResponse.TemplateInfo templateInfo = new QuerySmsTemplateResponse.TemplateInfo();

        Object statusObject = resJson.getJSONObject("Response").getJSONArray("DescribeTemplateStatusSet").get(0);

        JSONObject statusJSON = new JSONObject(statusObject);

        templateInfo.setTemplateContent(statusJSON.get("TemplateContent").toString());

        templateInfo.setStatusCode(Integer.parseInt(statusJSON.get("StatusCode").toString()));

        templateInfo.setReviewReply(statusJSON.get("ReviewReply").toString());

        templateInfo.setTemplateId(Integer.parseInt(statusJSON.get("TemplateId").toString()));

        smsTemplateResponse.getDescribeTemplateStatusSet().add(templateInfo);

        return smsTemplateResponse;
    }

    @VisibleForTesting
    Integer convertSmsTemplateAuditStatus(int templateStatus) {
        switch (templateStatus) {
            case 1: return SmsTemplateAuditStatusEnum.CHECKING.getStatus();
            case 0: return SmsTemplateAuditStatusEnum.SUCCESS.getStatus();
            case -1: return SmsTemplateAuditStatusEnum.FAIL.getStatus();
            default: throw new IllegalArgumentException(String.format("未知审核状态(%d)", templateStatus));
        }
    }

    @Data
    public static class SmsResponse {

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 厂商原返回体
         */
        private Object data;

    }


    /**
     * <p>类名: QuerySmsTemplateResponse
     * <p>说明：  sms模板查询返回信息
     *
     * @author :scholar
     * 2024/07/17  0:25
     **/
    @Data
    public static class QuerySmsTemplateResponse {
        private List<TemplateInfo> DescribeTemplateStatusSet;
        private String RequestId;
        @Data
        static class TemplateInfo {
            private String TemplateName;
            private Integer TemplateId;
            private Integer International;
            private String ReviewReply;
            private long CreateTime;
            private String TemplateContent;
            private Integer StatusCode;
        }
    }

    @Data
    private static class SmsReceiveStatus {

        /**
         * 短信接受成功 code
         */
        public static final String SUCCESS_CODE = "SUCCESS";

        /**
         * 用户实际接收到短信的时间
         */
        @JsonProperty("user_receive_time")
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private LocalDateTime receiveTime;

        /**
         * 国家（或地区）码
         */
        @JsonProperty("nationcode")
        private String nationCode;

        /**
         * 手机号码
         */
        private String mobile;

        /**
         * 实际是否收到短信接收状态，SUCCESS（成功）、FAIL（失败）
         */
        @JsonProperty("report_status")
        private String status;

        /**
         * 用户接收短信状态码错误信息
         */
        @JsonProperty("errmsg")
        private String errCode;

        /**
         * 用户接收短信状态描述
         */
        @JsonProperty("description")
        private String description;

        /**
         * 本次发送标识 ID（与发送接口返回的SerialNo对应）
         */
        @JsonProperty("sid")
        private String serialNo;

        /**
         * 用户的 session 内容（与发送接口的请求参数 SessionContext 一致）
         */
        @JsonProperty("ext")
        private SessionContext sessionContext;

    }

    @VisibleForTesting
    @Data
    static class SessionContext {

        /**
         * 发送短信记录id
         */
        private Long logId;

    }

}
