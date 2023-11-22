package cn.iocoder.yudao.framework.sms.core.client.impl.aliyun;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.impl.AbstractSmsClient;
import cn.iocoder.yudao.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySmsTemplateRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySmsTemplateResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
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
     * 调用成功 code
     */
    public static final String API_CODE_SUCCESS = "OK";

    /**
     * REGION, 使用杭州
     */
    private static final String ENDPOINT = "cn-hangzhou";

    /**
     * 阿里云客户端
     */
    private volatile IAcsClient client;

    public AliyunSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    @Override
    protected void doInit() {
        IClientProfile profile = DefaultProfile.getProfile(ENDPOINT, properties.getApiKey(), properties.getApiSecret());
        client = new DefaultAcsClient(profile);
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 构建请求
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(mobile);
        request.setSignName(properties.getSignature());
        request.setTemplateCode(apiTemplateId);
        request.setTemplateParam(JsonUtils.toJsonString(MapUtils.convertMap(templateParams)));
        request.setOutId(String.valueOf(sendLogId));
        // 执行请求
        SendSmsResponse response = client.getAcsResponse(request);
        return new SmsSendRespDTO().setSuccess(Objects.equals(response.getCode(), API_CODE_SUCCESS)).setSerialNo(response.getBizId())
                .setApiRequestId(response.getRequestId()).setApiCode(response.getCode()).setApiMsg(response.getMessage());
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
        // 构建请求
        QuerySmsTemplateRequest request = new QuerySmsTemplateRequest();
        request.setTemplateCode(apiTemplateId);
        // 执行请求
        QuerySmsTemplateResponse response = client.getAcsResponse(request);
        if (response.getTemplateStatus() == null) {
            return null;
        }
        return new SmsTemplateRespDTO().setId(response.getTemplateCode()).setContent(response.getTemplateContent())
                .setAuditStatus(convertSmsTemplateAuditStatus(response.getTemplateStatus())).setAuditReason(response.getReason());
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
     * 短信接收状态
     *
     * 参见 <a href="https://help.aliyun.com/document_detail/101867.html">文档</a>
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
