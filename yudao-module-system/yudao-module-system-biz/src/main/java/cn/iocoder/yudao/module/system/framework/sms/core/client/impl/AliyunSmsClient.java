package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;
import java.text.SimpleDateFormat;

/**
 * 阿里短信客户端的实现类
 *
 * @author zzf
 * @since 2021/1/25 14:17
 */
@Slf4j
public class AliyunSmsClient extends AbstractSmsClient {

    public AliyunSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    @Override
    protected void doInit() {
//        IClientProfile profile = DefaultProfile.getProfile(ENDPOINT, properties.getApiKey(), properties.getApiSecret());
//        client = new DefaultAcsClient(profile);
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {

        TreeMap<String, Object> queryParam = new TreeMap<>();
        queryParam.put("PhoneNumbers",mobile);
        queryParam.put("SignName",properties.getSignature());
        queryParam.put("TemplateCode",apiTemplateId);
        queryParam.put("TemplateParam",JsonUtils.toJsonString(MapUtils.convertMap(templateParams)));

        JSONObject response = sendSmsRequest(queryParam,"sendSms");
        SmsResponse smsResponse = getSmsSendResponse(response);

        return new SmsSendRespDTO().setSuccess(smsResponse.success).setApiMsg(smsResponse.data.toString());
    }

    JSONObject sendSmsRequest(TreeMap<String, Object> queryParam,String apiName) throws IOException, URISyntaxException {

        // ************* 步骤 1：拼接规范请求串 *************
        String url = "https://dysmsapi.aliyuncs.com"; //APP接入地址+接口访问URI
        String httpMethod = "POST"; // 请求方式
        String canonicalUri = "/";
        // 请求参数，当请求的查询字符串为空时，使用空字符串作为规范化查询字符串
        StringBuilder canonicalQueryString = new StringBuilder();
        queryParam.entrySet().stream().map(entry -> percentCode(entry.getKey()) + "=" + percentCode(String.valueOf(entry.getValue()))).forEachOrdered(queryPart -> {
            // 如果canonicalQueryString已经不是空的，则在查询参数前添加"&"
            if (!canonicalQueryString.isEmpty()) {
                canonicalQueryString.append("&");
            }
            canonicalQueryString.append(queryPart);
            System.out.println("canonicalQueryString=========>\n" + canonicalQueryString);
        });

        SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SDF.setTimeZone(new SimpleTimeZone(0, "GMT"));
        String SdfTime = SDF.format(new Date());
        String randomUUID = UUID.randomUUID().toString();

        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("host", "dysmsapi.aliyuncs.com");
        headers.put("x-acs-action", apiName);
        headers.put("x-acs-version", "2017-05-25");
        headers.put("x-acs-date", SdfTime);
        headers.put("x-acs-signature-nonce", randomUUID);
//        headers.put("content-type", "application/json;charset=utf-8");

        // 构造请求头，多个规范化消息头，按照消息头名称（小写）的字符代码顺序以升序排列后拼接在一起
        StringBuilder canonicalHeaders = new StringBuilder();
        // 已签名消息头列表，多个请求头名称（小写）按首字母升序排列并以英文分号（;）分隔
        StringBuilder signedHeadersSb = new StringBuilder();
        headers.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().startsWith("x-acs-") || entry.getKey().equalsIgnoreCase("host") || entry.getKey().equalsIgnoreCase("content-type")).sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String lowerKey = entry.getKey().toLowerCase();
            String value = String.valueOf(entry.getValue()).trim();
            canonicalHeaders.append(lowerKey).append(":").append(value).append("\n");
            signedHeadersSb.append(lowerKey).append(";");
        });
        String signedHeaders = signedHeadersSb.substring(0, signedHeadersSb.length() - 1);

        String body = "";//短信API为RPC接口，query parameters在uri中拼接，因此request body如果没有特殊要求，设置为空。
        String hashedRequestBody = HexUtil.encodeHexStr(DigestUtil.sha256(body));


        String canonicalRequest = httpMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestBody;
        System.out.println("canonicalRequest=========>\n" + canonicalRequest);

        // ************* 步骤 2：拼接待签名字符串 *************
        String hashedCanonicalRequest = HexUtil.encodeHexStr(DigestUtil.sha256(canonicalRequest));
        String stringToSign = "ACS3-HMAC-SHA256" + "\n" + hashedCanonicalRequest;

        // ************* 步骤 3：计算签名 *************
        String signature = SecureUtil.hmacSha256(properties.getApiSecret()).digestHex(stringToSign);

        // ************* 步骤 4：拼接 Authorization *************
        String authorization = "ACS3-HMAC-SHA256" + " " + "Credential=" + properties.getApiKey() + ", "
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;
        headers.put("Authorization", authorization);

        // ************* 步骤 5：构造HttpRequest 并执行request请求，获得response *************
//        url = url + canonicalUri;
        String urlWithParams = url + "?" + URLUtil.buildQuery(queryParam, null);

        HttpResponse response = HttpRequest.post(urlWithParams)
                .addHeaders(headers)
                .body(body)
                .execute();
//        URIBuilder uriBuilder = new URIBuilder(url);
//        // 添加请求参数
//        for (Map.Entry<String, Object> entry : queryParam.entrySet()) {
//            uriBuilder.addParameter(entry.getKey(), String.valueOf(entry.getValue()));
//        }
//        HttpUriRequest httpRequest = new HttpPost(uriBuilder.build());
////        HttpPost httpPost = new HttpPost(uriBuilder.build());
////        httpRequest = httpPost;
//
//        // 添加http请求头
//        for (Map.Entry<String, Object> entry : headers.entrySet()) {
//            httpRequest.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
//        }
//
//        // 发送请求
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = httpClient.execute(httpRequest);
        System.out.println("getEntity====="+response.body());
        System.out.println("response====="+response);

        return JSONUtil.parseObj(response.body());
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        List<SmsReceiveStatus> statuses = JsonUtils.parseArray(text, SmsReceiveStatus.class);
        return convertList(statuses, status -> new SmsReceiveRespDTO().setSuccess(status.getSuccess())
                .setErrorCode(status.getErrCode()).setErrorMsg(status.getErrMsg())
                .setMobile(status.getPhoneNumber()).setReceiveTime(status.getReportTime())
                .setSerialNo(status.getBizId()).setLogId(Long.valueOf(status.getOutId())));
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {

        TreeMap<String, Object> queryParam = new TreeMap<>();
        queryParam.put("TemplateCode",apiTemplateId);

        JSONObject response = sendSmsRequest(queryParam,"QuerySmsTemplate");
        QuerySmsTemplateResponse smsTemplateResponse = getSmsTemplateResponse(response);
        return new SmsTemplateRespDTO().setId(smsTemplateResponse.getTemplateCode()).setContent(smsTemplateResponse.getTemplateContent())
                .setAuditStatus(convertSmsTemplateAuditStatus(smsTemplateResponse.getTemplateStatus())).setAuditReason(smsTemplateResponse.getReason());

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
     * 对指定的字符串进行URL编码。
     * 使用UTF-8编码字符集对字符串进行编码，并对特定的字符进行替换，以符合URL编码规范。
     *
     * @param str 需要进行URL编码的字符串。
     * @return 编码后的字符串。其中，加号"+"被替换为"%20"，星号"*"被替换为"%2A"，波浪号"%7E"被替换为"~"。
     */
    public static String percentCode(String str) {
        if (str == null) {
            throw new IllegalArgumentException("输入字符串不可为null");
        }
        try {
            return URLEncoder.encode(str, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8编码不被支持", e);
        }
    }

    private SmsResponse getSmsSendResponse(JSONObject resJson) {
        SmsResponse smsResponse = new SmsResponse();
        smsResponse.setSuccess("OK".equals(resJson.getStr("Code")));
        smsResponse.setData(resJson);
//        smsResponse.setConfigId(getConfigId());
        return smsResponse;
    }

    private QuerySmsTemplateResponse getSmsTemplateResponse(JSONObject resJson) {

        QuerySmsTemplateResponse smsTemplateResponse = new QuerySmsTemplateResponse();

        smsTemplateResponse.setRequestId(resJson.getStr("RequestId"));
        smsTemplateResponse.setTemplateContent(resJson.getStr("TemplateContent"));
        smsTemplateResponse.setReason(resJson.getStr("Reason"));
        smsTemplateResponse.setTemplateStatus(resJson.getInt("TemplateStatus"));

        return smsTemplateResponse;
    }

    /**
     * <p>类名: SmsResponse
     * <p>说明：  发送短信返回信息
     *
     * @author :scholar
     * 2024/07/17  0:25
     **/
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

        /**
         * 配置标识名 如未配置取对应渠道名例如 Alibaba
         */
        private String configId;
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
        private String requestId;
        private String code;
        private String message;
        private Integer templateStatus;
        private String reason;
        private String templateCode;
        private Integer templateType;
        private String templateName;
        private String templateContent;
        private String createDate;
    }

    /**
     * 短信接收状态
     *
     * 参见 <a href="https://help.aliyun.com/document_detail/101867.html">文档</a>
     *
     * @author 润普源码
     */
    @Data
    public static class SmsReceiveStatus {

        /**
         * 手机号
         */
        @JsonProperty("phone_number")
        private String phoneNumber;
        /**
         * 发送时间
         */
        @JsonProperty("send_time")
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private LocalDateTime sendTime;
        /**
         * 状态报告时间
         */
        @JsonProperty("report_time")
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private LocalDateTime reportTime;
        /**
         * 是否接收成功
         */
        private Boolean success;
        /**
         * 状态报告说明
         */
        @JsonProperty("err_msg")
        private String errMsg;
        /**
         * 状态报告编码
         */
        @JsonProperty("err_code")
        private String errCode;
        /**
         * 发送序列号
         */
        @JsonProperty("biz_id")
        private String bizId;
        /**
         * 用户序列号
         *
         * 这里我们传递的是 SysSmsLogDO 的日志编号
         */
        @JsonProperty("out_id")
        private String outId;
        /**
         * 短信长度，例如说 1、2、3
         *
         * 140 字节算一条短信，短信长度超过 140 字节时会拆分成多条短信发送
         */
        @JsonProperty("sms_size")
        private Integer smsSize;

    }

}
