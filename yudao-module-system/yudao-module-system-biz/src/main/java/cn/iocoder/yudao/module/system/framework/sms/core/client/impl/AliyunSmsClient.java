package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.google.common.annotations.VisibleForTesting;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 阿里短信客户端的实现类
 *
 * @author zzf
 * @since 2021/1/25 14:17
 */
@Slf4j
public class AliyunSmsClient extends AbstractSmsClient {

    private static final String URL = "https://dysmsapi.aliyuncs.com";
    private static final String HOST = "dysmsapi.aliyuncs.com";
    private static final String VERSION = "2017-05-25";

    private static final String RESPONSE_CODE_SUCCESS = "OK";

    public AliyunSmsClient(SmsChannelProperties properties) {
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
        Assert.notBlank(properties.getSignature(), "短信签名不能为空");
        // 1. 执行请求
        // 参考链接 https://api.aliyun.com/document/Dysmsapi/2017-05-25/SendSms
        TreeMap<String, Object> queryParam = new TreeMap<>();
        queryParam.put("PhoneNumbers", mobile);
        queryParam.put("SignName", properties.getSignature());
        queryParam.put("TemplateCode", apiTemplateId);
        queryParam.put("TemplateParam", JsonUtils.toJsonString(MapUtils.convertMap(templateParams)));
        queryParam.put("OutId", sendLogId);
        JSONObject response = request("SendSms", queryParam);

        // 2. 解析请求
        return new SmsSendRespDTO()
                .setSuccess(Objects.equals(response.getStr("Code"), RESPONSE_CODE_SUCCESS))
                .setSerialNo(response.getStr("BizId"))
                .setApiRequestId(response.getStr("RequestId"))
                .setApiCode(response.getStr("Code"))
                .setApiMsg(response.getStr("Message"));
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        JSONArray statuses = JSONUtil.parseArray(text);
        // 字段参考
        return convertList(statuses, status -> {
            JSONObject statusObj = (JSONObject) status;
            return new SmsReceiveRespDTO()
                    .setSuccess(statusObj.getBool("success")) // 是否接收成功
                    .setErrorCode(statusObj.getStr("err_code")) // 状态报告编码
                    .setErrorMsg(statusObj.getStr("err_msg")) // 状态报告说明
                    .setMobile(statusObj.getStr("phone_number")) // 手机号
                    .setReceiveTime(statusObj.getLocalDateTime("report_time", null)) // 状态报告时间
                    .setSerialNo(statusObj.getStr("biz_id")) // 发送序列号
                    .setLogId(statusObj.getLong("out_id")); // 用户序列号
        });
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        // 1. 执行请求
        // 参考链接 https://api.aliyun.com/document/Dysmsapi/2017-05-25/QuerySmsTemplate
        TreeMap<String, Object> queryParam = new TreeMap<>();
        queryParam.put("TemplateCode", apiTemplateId);
        JSONObject response = request("QuerySmsTemplate", queryParam);

        // 2.1 请求失败
        String code = response.getStr("Code");
        if (ObjectUtil.notEqual(code, RESPONSE_CODE_SUCCESS)) {
            log.error("[getSmsTemplate][模版编号({}) 响应不正确({})]", apiTemplateId, response);
            return null;
        }
        // 2.2 请求成功
        return new SmsTemplateRespDTO()
                .setId(response.getStr("TemplateCode"))
                .setContent(response.getStr("TemplateContent"))
                .setAuditStatus(convertSmsTemplateAuditStatus(response.getInt("TemplateStatus")))
                .setAuditReason(response.getStr("Reason"));
    }

    @VisibleForTesting
    Integer convertSmsTemplateAuditStatus(Integer templateStatus) {
        switch (templateStatus) {
            case 0: return SmsTemplateAuditStatusEnum.CHECKING.getStatus();
            case 1: return SmsTemplateAuditStatusEnum.SUCCESS.getStatus();
            case 2: return SmsTemplateAuditStatusEnum.FAIL.getStatus();
            default: throw new IllegalArgumentException(String.format("未知审核状态(%d)", templateStatus));
        }
    }

    /**
     * 请求阿里云短信
     *
     * @see <a href="https://help.aliyun.com/zh/sdk/product-overview/v3-request-structure-and-signature">V3 版本请求体&签名机制</>
     * @param apiName 请求的 API 名称
     * @param queryParams 请求参数
     * @return 请求结果
     */
    private JSONObject request(String apiName, TreeMap<String, Object> queryParams) {
        // 1. 请求参数
        String queryString = queryParams.entrySet().stream()
                .map(entry -> percentCode(entry.getKey()) + "=" + percentCode(String.valueOf(entry.getValue())))
                .collect(Collectors.joining("&"));

        // 2.1 请求 Header
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("host", HOST);
        headers.put("x-acs-version", VERSION);
        headers.put("x-acs-action", apiName);
        headers.put("x-acs-date", FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("GMT")).format(new Date()));
        headers.put("x-acs-signature-nonce", IdUtil.randomUUID());

        // 2.2 构建签名 Header
        StringBuilder canonicalHeaders = new StringBuilder(); // 构造请求头，多个规范化消息头，按照消息头名称（小写）的字符代码顺序以升序排列后拼接在一起
        StringBuilder signedHeadersBuilder = new StringBuilder(); // 已签名消息头列表，多个请求头名称（小写）按首字母升序排列并以英文分号（;）分隔
        headers.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().startsWith("x-acs-")
                        || entry.getKey().equalsIgnoreCase("host")
                        || entry.getKey().equalsIgnoreCase("content-type"))
                .sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    String lowerKey = entry.getKey().toLowerCase();
                    canonicalHeaders.append(lowerKey).append(":").append(String.valueOf(entry.getValue()).trim()).append("\n");
                    signedHeadersBuilder.append(lowerKey).append(";");
                });
        String signedHeaders = signedHeadersBuilder.substring(0, signedHeadersBuilder.length() - 1);

        // 3. 请求 Body
        String requestBody = ""; // 短信 API 为 RPC 接口，query parameters 在 uri 中拼接，因此 request body 如果没有特殊要求，设置为空。
        String hashedRequestBody = DigestUtil.sha256Hex(requestBody);

        // 4. 构建 Authorization 签名
        String canonicalRequest = "POST" + "\n" + "/" + "\n" + queryString + "\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestBody;
        String hashedCanonicalRequest = DigestUtil.sha256Hex(canonicalRequest);
        String stringToSign = "ACS3-HMAC-SHA256" + "\n" + hashedCanonicalRequest;
        String signature = SecureUtil.hmacSha256(properties.getApiSecret()).digestHex(stringToSign); // 计算签名
        headers.put("Authorization", "ACS3-HMAC-SHA256" + " " + "Credential=" + properties.getApiKey()
                + ", " + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature);

        // 5. 发起请求
        String responseBody = HttpUtils.post(URL + "?" + queryString, headers, requestBody);
        return JSONUtil.parseObj(responseBody);
    }

    /**
     * 对指定的字符串进行 URL 编码，并对特定的字符进行替换，以符合URL编码规范
     *
     * @param str 需要进行 URL 编码的字符串
     * @return 编码后的字符串
     */
    @SneakyThrows
    private static String percentCode(String str) {
        Assert.notNull(str, "str 不能为空");
        return URLEncoder.encode(str, StandardCharsets.UTF_8.name())
                .replace("+", "%20") // 加号 "+" 被替换为 "%20"
                .replace("*", "%2A") // 星号 "*" 被替换为 "%2A"
                .replace("%7E", "~"); // 波浪号 "%7E" 被替换为 "~"
    }

}