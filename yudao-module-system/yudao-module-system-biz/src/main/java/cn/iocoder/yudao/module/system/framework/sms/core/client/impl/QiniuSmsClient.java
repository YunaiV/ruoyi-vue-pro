package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 七牛云短信客户端的实现类
 *
 * @author scholar
 * @since 2024/08/26 15:35
 */
@Slf4j
public class QiniuSmsClient extends AbstractSmsClient {

    private static final String HOST = "sms.qiniuapi.com";

    private static final String PATH = "/v1/message/single";

    private static final String TEMPLATE_PATH  = "/v1/template";

    public QiniuSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    protected void doInit() {
    }

    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 1. 执行请求
        // 参考链接 https://developer.qiniu.com/sms/5824/through-the-api-send-text-messages
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("template_id", apiTemplateId);
        body.put("mobile", mobile);
        body.put("parameters", CollStreamUtil.toMap(templateParams, KeyValue::getKey, KeyValue::getValue));
        body.put("seq", Long.toString(sendLogId));

        JSONObject response = request("POST", body, PATH);
        // 2. 解析请求
        if (ObjectUtil.isNotEmpty(response.getStr("error"))){//短信请求失败
            return new SmsSendRespDTO().setSuccess(false)
                    .setApiCode(response.getStr("error"))
                    .setApiRequestId(response.getStr("request_id"))
                    .setApiMsg(response.getStr("message"));
        }

        return new SmsSendRespDTO().setSuccess(response.containsKey("message_id"))
                .setSerialNo(response.getStr("message_id"));
    }

    /**
     * 请求七牛云短信
     *
     * @see <a href="https://developer.qiniu.com/sms/5842/sms-api-authentication"</>
     * @param httpMethod http请求方法
     * @param body http请求消息体
     * @param path URL path
     * @return 请求结果
     */
    private JSONObject request(String httpMethod, LinkedHashMap<String, Object> body, String path) {
        String signDate = DateUtil.date().setTimeZone(TimeZone.getTimeZone("UTC")).toString("yyyyMMdd'T'HHmmss'Z'");
        //请求头
        Map<String, String> header = new HashMap<>(4);
        header.put("HOST", HOST);
        header.put("Authorization", getSignature(httpMethod, HOST, path, body != null ? JSONUtil.toJsonStr(body) : "", signDate));
        header.put("Content-Type", "application/json");
        header.put("X-Qiniu-Date", signDate);

        String responseBody ="";
        if (Objects.equals(httpMethod, "POST")){// POST 发送短消息用POST请求
            responseBody = HttpUtils.post("https://" + HOST + path, header, JSONUtil.toJsonStr(body));
        }else { // GET 查询template状态用GET请求
            responseBody = HttpUtils.get("https://" + HOST + path, header);
        }
        return JSONUtil.parseObj(responseBody);
    }

    public String getSignature(String method, String host, String path, String body, String signDate) {
        StringBuilder dataToSign = new StringBuilder();
        dataToSign.append(method.toUpperCase()).append(" ").append(path);
        dataToSign.append("\nHost: ").append(host);
        dataToSign.append("\n").append("Content-Type").append(": ").append("application/json");
        dataToSign.append("\n").append("X-Qiniu-Date").append(": ").append(signDate);
        dataToSign.append("\n\n");
        if (ObjectUtil.isNotEmpty(body)) {
            dataToSign.append(body);
        }
        String encodedSignature = SecureUtil.hmac(HmacAlgorithm.HmacSHA1, properties.getApiSecret()).digestBase64(dataToSign.toString(), true);

        return "Qiniu " + properties.getApiKey() + ":" + encodedSignature;
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        JSONObject status = JSONUtil.parseObj(text);
        // 字段参考 https://developer.qiniu.com/sms/5910/message-push
        return ListUtil.of(new SmsReceiveRespDTO()
                .setSuccess("DELIVRD".equals(status.getJSONArray("items").getJSONObject(0).getStr("status"))) // 是否接收成功
                .setErrorMsg(status.getJSONArray("items").getJSONObject(0).getStr("status"))
                .setMobile(status.getJSONArray("items").getJSONObject(0).getStr("mobile")) // 手机号
                .setReceiveTime(LocalDateTimeUtil.of(status.getJSONArray("items").getJSONObject(0).getLong("delivrd_at")*1000L))
                .setSerialNo(status.getJSONArray("items").getJSONObject(0).getStr("message_id")) // 发送序列号
                .setLogId(Long.valueOf(status.getJSONArray("items").getJSONObject(0).getStr("seq")))); // logId
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        // 1. 执行请求
        // 参考链接 https://developer.qiniu.com/sms/5969/query-a-single-template
        JSONObject response = request("GET", null, TEMPLATE_PATH + "/" + apiTemplateId);
        // 2.1 请求失败
        if (ObjUtil.notEqual(response.getStr("audit_status"), "passed")) {
            log.error("[getSmsTemplate][模版编号({}) 响应不正确({})]", apiTemplateId, response);
            return null;
        }

        // 2.2 请求成功
        return new SmsTemplateRespDTO()
                .setId(response.getStr("id"))
                .setContent(response.getStr("template"))
                .setAuditStatus(convertSmsTemplateAuditStatus(response.getStr("audit_status")))
                .setAuditReason(response.getStr("reject_reason"));
    }

    @VisibleForTesting
    Integer convertSmsTemplateAuditStatus(String templateStatus) {
        return switch (templateStatus) {
            case "passed" -> SmsTemplateAuditStatusEnum.SUCCESS.getStatus();
            case "reviewing" -> SmsTemplateAuditStatusEnum.CHECKING.getStatus();
            case "rejected" -> SmsTemplateAuditStatusEnum.FAIL.getStatus();
            case null, default ->
                    throw new IllegalArgumentException(String.format("未知审核状态(%str)", templateStatus));
        };
    }
}
