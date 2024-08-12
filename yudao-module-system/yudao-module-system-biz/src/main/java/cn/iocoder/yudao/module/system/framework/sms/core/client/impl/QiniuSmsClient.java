package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.qiniu.sms.SmsManager;
import com.qiniu.util.Auth;
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
 * 七牛云短信客户端的实现类
 */
@Slf4j
public class QiniuSmsClient extends AbstractSmsClient {

    /**
     * 七牛云短信管理器
     */
    private volatile SmsManager smsManager;

    public QiniuSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    @Override
    protected void doInit() {
        Auth auth = Auth.create(properties.getApiKey(), properties.getApiSecret());
        smsManager = new SmsManager(auth);
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 构建请求
        String[] mobiles = {mobile};
        String templateId = apiTemplateId;
        String templateParam = JsonUtils.toJsonString(MapUtils.convertMap(templateParams));
        // 执行请求
        com.qiniu.sms.model.SmsResponse response = smsManager.sendMessage(templateId, mobiles, templateParam);
        return new SmsSendRespDTO().setSuccess(response.isSuccessful()).setSerialNo(response.getJobId())
                .setApiRequestId(response.getRequestId()).setApiCode(response.getCode()).setApiMsg(response.getMessage());
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        List<SmsReceiveStatus> statuses = JsonUtils.parseArray(text, SmsReceiveStatus.class);
        return convertList(statuses, status -> new SmsReceiveRespDTO().setSuccess(status.getSuccess())
                .setErrorCode(status.getErrCode()).setErrorMsg(status.getErrMsg())
                .setMobile(status.getPhoneNumber()).setReceiveTime(status.getReportTime())
                .setSerialNo(status.getJobId()).setLogId(Long.valueOf(status.getOutId())));
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        // 七牛云不支持查询短信模板，直接返回 null
        return null;
    }

    /**
     * 短信接收状态
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
        @JsonProperty("job_id")
        private String jobId;
        /**
         * 用户序列号
         */
        @JsonProperty("out_id")
        private String outId;
        /**
         * 短信长度
         */
        @JsonProperty("sms_size")
        private Integer smsSize;

    }

}
