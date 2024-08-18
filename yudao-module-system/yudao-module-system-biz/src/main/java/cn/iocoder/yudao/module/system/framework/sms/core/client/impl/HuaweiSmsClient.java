package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;


import java.time.LocalDateTime;

import static cn.hutool.crypto.digest.DigestUtil.sha256Hex;

// todo @scholar：参考阿里云在优化下
/**
 * 华为短信客户端的实现类
 *
 * @author scholar
 * @since 2024/6/02 11:55
 */
@Slf4j
public class HuaweiSmsClient extends AbstractSmsClient {

    public static final String URL = "https://smsapi.cn-north-4.myhuaweicloud.com:443/sms/batchSendSms/v1";//APP接入地址+接口访问URI
    public static final String HOST = "smsapi.cn-north-4.myhuaweicloud.com:443";
    public static final String SIGNEDHEADERS = "content-type;host;x-sdk-date";

    @Override
    protected void doInit() {
    }

    public HuaweiSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 参考链接 https://support.huaweicloud.com/api-msgsms/sms_05_0001.html
        // 相比较阿里短信，华为短信发送的时候需要额外的参数“通道号”，考虑到不破坏原有的的结构
        // 所以将 通道号 拼接到 apiTemplateId 字段中，格式为 "apiTemplateId 通道号"。空格为分隔符。
        String sender = StrUtil.subAfter(apiTemplateId, " ", true); //中国大陆短信签名通道号或全球短信通道号
        String templateId = StrUtil.subBefore(apiTemplateId, " ", true); //模板ID

        //选填,短信状态报告接收地址,推荐使用域名,为空或者不填表示不接收状态报告
        String statusCallBack = properties.getCallbackUrl();

        List<String> templateParas = CollectionUtils.convertList(templateParams, kv -> String.valueOf(kv.getValue()));

        JSONObject JsonResponse = request(sendLogId,sender,mobile,templateId,templateParas,statusCallBack);

        return new SmsSendRespDTO().setSuccess("000000".equals(JsonResponse.getStr("code")))
                .setSerialNo(JsonResponse.getJSONArray("result").getJSONObject(0).getStr("smsMsgId"))
                .setApiCode(JsonResponse.getJSONArray("result").getJSONObject(0).getStr("status"));
    }

    JSONObject request(Long sendLogId,String sender,String mobile,String templateId,List<String> templateParas,String statusCallBack) throws UnsupportedEncodingException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String sdkDate = sdf.format(new Date());

        // ************* 步骤 1：拼接规范请求串 *************
        String httpRequestMethod = "POST";
        String canonicalUri = "/sms/batchSendSms/v1/";
        String canonicalQueryString = "";//查询参数为空
        String canonicalHeaders = "content-type:application/x-www-form-urlencoded\n"
                + "host:"+ HOST +"\n"
                + "x-sdk-date:" + sdkDate + "\n";
        String body = buildRequestBody(sender, mobile, templateId, templateParas, statusCallBack, sendLogId);
        if (null == body || body.isEmpty()) {
            return null;
        }
        String hashedRequestBody = sha256Hex(body);
        String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
                + canonicalHeaders + "\n" + SIGNEDHEADERS + "\n" + hashedRequestBody;

        // ************* 步骤 2：拼接待签名字符串 *************
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = "SDK-HMAC-SHA256" + "\n" + sdkDate + "\n" + hashedCanonicalRequest;

        // ************* 步骤 3：计算签名 *************
        String signature = SecureUtil.hmacSha256(properties.getApiSecret()).digestHex(stringToSign);

        // ************* 步骤 4：拼接 Authorization *************
        String authorization = "SDK-HMAC-SHA256" + " " + "Access=" + properties.getApiKey() + ", "
                + "SignedHeaders=" + SIGNEDHEADERS + ", " + "Signature=" + signature;

        // ************* 步骤 5：构造HttpRequest 并执行request请求，获得response *************
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("X-Sdk-Date", sdkDate);
        headers.put("host", HOST);
        headers.put("Authorization", authorization);

        String responseBody = HttpUtils.post(URL, headers, body);
        return JSONUtil.parseObj(responseBody);
//
//
//        HttpResponse response = HttpRequest.post(URL)
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .header("X-Sdk-Date", sdkDate)
//                .header("host",HOST)
//                .header("Authorization", authorization)
//                .body(body)
//                .execute();
//
//        return JSONUtil.parseObj(response.body());
    }

    static String buildRequestBody(String sender, String receiver, String templateId, List<String> templateParas,
                                   String statusCallBack, Long sendLogId) throws UnsupportedEncodingException {
        if (null == sender || null == receiver || null == templateId || sender.isEmpty() || receiver.isEmpty()
                || templateId.isEmpty()) {
            System.out.println("buildRequestBody(): sender, receiver or templateId is null.");
            return null;
        }

        StringBuilder body = new StringBuilder();
        appendToBody(body, "from=", sender);
        appendToBody(body, "&to=", receiver);
        appendToBody(body, "&templateId=", templateId);
        appendToBody(body, "&templateParas=", JsonUtils.toJsonString(templateParas));
        appendToBody(body, "&statusCallback=", statusCallBack);
        appendToBody(body, "&signature=", null);
        appendToBody(body, "&extend=", String.valueOf(sendLogId));

        return body.toString();
    }

    private static void appendToBody(StringBuilder body, String key, String val) throws UnsupportedEncodingException {
        if (null != val && !val.isEmpty()) {
            body.append(key).append(URLEncoder.encode(val, "UTF-8"));
        }
    }
    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String requestBody) {

        System.out.println("text in parseSmsReceiveStatus===== " + requestBody);

        Map<String, String> params = new HashMap<>();
        try {
            String[] pairs = requestBody.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                params.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<SmsReceiveRespDTO> respDTOS = new ArrayList<>();
        respDTOS.add(new SmsReceiveRespDTO()
                        .setSuccess("DELIVRD".equals(params.get("status"))) // 是否接收成功
                        .setErrorCode(params.get("status")) // 状态报告编码
                        .setErrorMsg(params.get("statusDesc"))
                        .setMobile(params.get("to")) // 手机号
                        .setReceiveTime(LocalDateTime.ofInstant(Instant.parse(params.get("updateTime")), ZoneId.of("UTC"))) // 状态报告时间
                        .setSerialNo(params.get("smsMsgId")) // 发送序列号
                        .setLogId(Long.valueOf(params.get("extend")))//logId
        );

        return respDTOS;
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        //华为短信模板查询和发送短信，是不同的两套key和secret，与阿里、腾讯的区别较大，这里模板查询校验暂不实现。
        return new SmsTemplateRespDTO().setId(null).setContent(null)
                .setAuditStatus(SmsTemplateAuditStatusEnum.SUCCESS.getStatus()).setAuditReason(null);
    }
}