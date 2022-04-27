package cn.iocoder.yudao.framework.sms.core.client.impl.aliyun;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.impl.AbstractSmsClient;
import cn.iocoder.yudao.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.aliyuncs.AcsRequest;
import com.aliyuncs.AcsResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySmsTemplateRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 阿里短信客户端的实现类
 *
 * @author zzf
 * @since 2021/1/25 14:17
 */
@Slf4j
public class AliyunSmsClient extends AbstractSmsClient {

    /**
     * REGION, 使用杭州
     */
    private static final String ENDPOINT = "cn-hangzhou";

    /**
     * 阿里云客户端
     */
    private volatile IAcsClient client;

    public AliyunSmsClient(SmsChannelProperties properties) {
        super(properties, new AliyunSmsCodeMapping());
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    @Override
    protected void doInit() {
        IClientProfile profile = DefaultProfile.getProfile(ENDPOINT, properties.getApiKey(), properties.getApiSecret());
        client = new DefaultAcsClient(profile);
    }

    @Override
    protected SmsCommonResult<SmsSendRespDTO> doSendSms(Long sendLogId, String mobile,
                                                        String apiTemplateId, List<KeyValue<String, Object>> templateParams) {
        // 构建参数
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(mobile);
        request.setSignName(properties.getSignature());
        request.setTemplateCode(apiTemplateId);
        request.setTemplateParam(JsonUtils.toJsonString(MapUtils.convertMap(templateParams)));
        request.setOutId(String.valueOf(sendLogId));
        // 执行请求
        return invoke(request, response -> new SmsSendRespDTO().setSerialNo(response.getBizId()));
    }

    @Override
    protected List<SmsReceiveRespDTO> doParseSmsReceiveStatus(String text) throws Throwable {
        List<SmsReceiveStatus> statuses = JsonUtils.parseArray(text, SmsReceiveStatus.class);
        return statuses.stream().map(status -> {
            SmsReceiveRespDTO resp = new SmsReceiveRespDTO();
            resp.setSuccess(status.getSuccess());
            resp.setErrorCode(status.getErrCode()).setErrorMsg(status.getErrMsg());
            resp.setMobile(status.getPhoneNumber()).setReceiveTime(status.getReportTime());
            resp.setSerialNo(status.getBizId()).setLogId(Long.valueOf(status.getOutId()));
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    protected SmsCommonResult<SmsTemplateRespDTO> doGetSmsTemplate(String apiTemplateId) {
        // 构建参数
        QuerySmsTemplateRequest request = new QuerySmsTemplateRequest();
        request.setTemplateCode(apiTemplateId);
        // 执行请求
        return invoke(request, response -> {
            SmsTemplateRespDTO data = new SmsTemplateRespDTO();
            data.setId(response.getTemplateCode()).setContent(response.getTemplateContent());
            data.setAuditStatus(convertSmsTemplateAuditStatus(response.getTemplateStatus())).setAuditReason(response.getReason());
            return data;
        });
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

    @VisibleForTesting
    <T extends AcsResponse, R> SmsCommonResult<R> invoke(AcsRequest<T> request, Function<T, R> responseConsumer) {
        try {
            // 执行发送. 由于阿里云 sms 短信没有统一的 Response，但是有统一的 code、message、requestId 属性，所以只好反射
            T sendResult = client.getAcsResponse(request);
            String code = (String) ReflectUtil.getFieldValue(sendResult, "code");
            String message = (String) ReflectUtil.getFieldValue(sendResult, "message");
            String requestId = (String) ReflectUtil.getFieldValue(sendResult, "requestId");
            // 解析结果
            R data = null;
            if (Objects.equals(code, "OK")) { // 请求成功的情况下
                data = responseConsumer.apply(sendResult);
            }
            // 拼接结果
            return SmsCommonResult.build(code, message, requestId, data, codeMapping);
        } catch (ClientException ex) {
            return SmsCommonResult.build(ex.getErrCode(), formatResultMsg(ex), ex.getRequestId(), null, codeMapping);
        }
    }

    private static String formatResultMsg(ClientException ex) {
        if (StrUtil.isEmpty(ex.getErrorDescription())) {
            return ex.getErrMsg();
        }
        return ex.getErrMsg() + " => " + ex.getErrorDescription();
    }

    /**
     * 短信接收状态
     *
     * 参见 https://help.aliyun.com/document_detail/101867.html 文档
     *
     * @author 芋道源码
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
        private Date sendTime;
        /**
         * 状态报告时间
         */
        @JsonProperty("report_time")
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private Date reportTime;
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
