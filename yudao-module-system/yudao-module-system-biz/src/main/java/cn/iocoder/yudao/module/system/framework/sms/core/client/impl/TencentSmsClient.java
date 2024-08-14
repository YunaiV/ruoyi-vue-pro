package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.google.common.annotations.VisibleForTesting;
import jakarta.xml.bind.DatatypeConverter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.hutool.crypto.digest.DigestUtil.sha256Hex;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 腾讯云短信功能实现
 *
 * 参见 <a href="https://cloud.tencent.com/document/product/382/52077">文档</a>
 *
 * @author shiwp
 */
public class TencentSmsClient extends AbstractSmsClient {

    private static final String VERSION = "2021-01-11";
    private static final String REGION = "ap-guangzhou";

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
        // 1. 执行请求
        // 参考链接 https://cloud.tencent.com/document/product/382/55981
        TreeMap<String, Object> body = new TreeMap<>();
        body.put("PhoneNumberSet", new String[]{mobile});
        body.put("SmsSdkAppId", getSdkAppId());
        body.put("SignName", properties.getSignature());
        body.put("TemplateId",apiTemplateId);
        body.put("TemplateParamSet", ArrayUtils.toArray(templateParams, param -> String.valueOf(param.getValue())));
        JSONObject response = request("SendSms", body);

        // 2. 解析请求
        JSONObject responseResult = response.getJSONObject("Response");
        JSONObject error = responseResult.getJSONObject("Error");
        if (error != null) {
            return new SmsSendRespDTO().setSuccess(false)
                    .setApiRequestId(responseResult.getStr("RequestId"))
                    .setApiCode(error.getStr("Code"))
                    .setApiMsg(error.getStr("Message"));
        }
        JSONObject responseData = responseResult.getJSONArray("SendStatusSet").getJSONObject(0);
        return new SmsSendRespDTO().setSuccess(Objects.equals(API_CODE_SUCCESS, responseData.getStr("Code")))
                .setApiRequestId(responseResult.getStr("RequestId"))
                .setSerialNo(responseData.getStr("SerialNo"))
                .setApiMsg(responseData.getStr("Message"));
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        JSONArray statuses = JSONUtil.parseArray(text);
        // 字段参考
        return convertList(statuses, status -> {
            JSONObject statusObj = (JSONObject) status;
            return new SmsReceiveRespDTO()
                    .setSuccess("SUCCESS".equals(statusObj.getStr("report_status"))) // 是否接收成功
                    .setErrorCode(statusObj.getStr("errmsg")) // 状态报告编码
                    .setMobile(statusObj.getStr("mobile")) // 手机号
                    .setReceiveTime(statusObj.getLocalDateTime("user_receive_time", null)) // 状态报告时间
                    .setSerialNo(statusObj.getStr("sid")); // 发送序列号
        });
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        // 1. 构建请求
        // 参考链接 https://cloud.tencent.com/document/product/382/52067
        TreeMap<String, Object> body = new TreeMap<>();
        body.put("International", INTERNATIONAL_CHINA);
        body.put("TemplateIdSet", new Integer[]{Integer.valueOf(apiTemplateId)});
        JSONObject response = request("DescribeSmsTemplateList", body);

        // TODO @scholar：会有请求失败的情况么？类似发送的（那块逻辑我补充了）
        JSONObject TemplateStatusSet = response.getJSONObject("Response").getJSONArray("DescribeTemplateStatusSet").getJSONObject(0);
        String content = TemplateStatusSet.get("TemplateContent").toString();
        int templateStatus = Integer.parseInt(TemplateStatusSet.get("StatusCode").toString());
        String auditReason = TemplateStatusSet.get("ReviewReply").toString();

        return new SmsTemplateRespDTO().setId(apiTemplateId).setContent(content)
                .setAuditStatus(convertSmsTemplateAuditStatus(templateStatus)).setAuditReason(auditReason);
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

    /**
     * 请求腾讯云短信
     *
     * @see <a href="https://cloud.tencent.com/document/product/382/52072">签名方法 v3</a>
     *
     * @param action 请求的 API 名称
     * @param body 请求参数
     * @return 请求结果
     */
    private JSONObject request(String action, TreeMap<String, Object> body) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        // TODO @scholar：这个 format，看看怎么写的可以简化点
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 注意时区，否则容易出错
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(Long.valueOf(timestamp + "000")));

        // TODO @scholar：这个步骤，看看怎么参考阿里云 client，归类下；1. 2.1 2.2 这种
        // ************* 步骤 1：拼接规范请求串 *************
        // TODO @scholar：这个 hsot 枚举下；
        String host = "sms.tencentcloudapi.com"; //APP接入地址+接口访问URI
        String httpMethod = "POST"; // 请求方式
        String canonicalUri = "/";
        String canonicalQueryString = "";

        String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                + "host:" + host + "\n" + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String hashedRequestBody = sha256Hex(JSONUtil.toJsonStr(body));
        // TODO @scholar：换行下，不然单行太长了
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
        headers.put("X-TC-Version", VERSION);
        headers.put("X-TC-Region", REGION);

        String responseBody = HttpUtils.post("https://" + host, headers, JSONUtil.toJsonStr(body));

        return JSONUtil.parseObj(responseBody);
    }

    // TODO @scholar：使用 hutool 简化下
    private static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }
}