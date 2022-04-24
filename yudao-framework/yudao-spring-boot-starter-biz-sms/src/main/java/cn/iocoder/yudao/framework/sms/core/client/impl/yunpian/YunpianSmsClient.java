package cn.iocoder.yudao.framework.sms.core.client.impl.yunpian;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.impl.AbstractSmsClient;
import cn.iocoder.yudao.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.constant.YunpianConstant;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.Template;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 云片短信客户端的实现类
 *
 * @author zzf
 * @since 9:48 2021/3/5
 */
@Slf4j
public class YunpianSmsClient extends AbstractSmsClient {

    /**
     * 云信短信客户端
     */
    private volatile YunpianClient client;

    public YunpianSmsClient(SmsChannelProperties properties) {
        super(properties, new YunpianSmsCodeMapping());
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
    }

    @Override
    public void doInit() {
        YunpianClient oldClient = client;
        // 初始化新的客户端
        YunpianClient newClient = new YunpianClient(properties.getApiKey());
        newClient.init();
        this.client = newClient;
        // 销毁老的客户端
        if (oldClient != null) {
            oldClient.close();
        }
    }

    @Override
    protected SmsCommonResult<SmsSendRespDTO> doSendSms(Long sendLogId, String mobile,
                                                        String apiTemplateId, List<KeyValue<String, Object>> templateParams) throws Throwable {
        return invoke(() -> {
            Map<String, String> request = new HashMap<>();
            request.put(YunpianConstant.MOBILE, mobile);
            request.put(YunpianConstant.TPL_ID, apiTemplateId);
            request.put(YunpianConstant.TPL_VALUE, formatTplValue(templateParams));
            request.put(YunpianConstant.UID, String.valueOf(sendLogId));
            request.put(YunpianConstant.CALLBACK_URL, properties.getCallbackUrl());
            return client.sms().tpl_single_send(request);
        }, response -> new SmsSendRespDTO().setSerialNo(String.valueOf(response.getSid())));
    }

    private static String formatTplValue(List<KeyValue<String, Object>> templateParams) {
        if (CollUtil.isEmpty(templateParams)) {
            return "";
        }
        // 参考 https://www.yunpian.com/official/document/sms/zh_cn/introduction_demos_encode_sample 格式化
        StringJoiner joiner = new StringJoiner("&");
        templateParams.forEach(param -> joiner.add(String.format("#%s#=%s", param.getKey(),
                URLUtil.encode(String.valueOf(param.getValue())))));
        return joiner.toString();
    }

    @Override
    protected List<SmsReceiveRespDTO> doParseSmsReceiveStatus(String text) throws Throwable {
        List<SmsReceiveStatus> statuses = JsonUtils.parseArray(text, SmsReceiveStatus.class);
        return statuses.stream().map(status -> {
            SmsReceiveRespDTO resp = new SmsReceiveRespDTO();
            resp.setSuccess(Objects.equals(status.getReportStatus(), "SUCCESS"));
            resp.setErrorCode(status.getErrorMsg()).setErrorMsg(status.getErrorDetail());
            resp.setMobile(status.getMobile()).setReceiveTime(status.getUserReceiveTime());
            resp.setSerialNo(String.valueOf(status.getSid())).setLogId(status.getUid());
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    protected SmsCommonResult<SmsTemplateRespDTO> doGetSmsTemplate(String apiTemplateId) throws Throwable {
        return invoke(() -> {
            Map<String, String> request = new HashMap<>();
            request.put(YunpianConstant.APIKEY, properties.getApiKey());
            request.put(YunpianConstant.TPL_ID, apiTemplateId);
            return client.tpl().get(request);
        }, response -> {
            Template template = response.get(0);
            return new SmsTemplateRespDTO().setId(String.valueOf(template.getTpl_id())).setContent(template.getTpl_content())
                   .setAuditStatus(convertSmsTemplateAuditStatus(template.getCheck_status())).setAuditReason(template.getReason());
        });
    }

    @VisibleForTesting
    Integer convertSmsTemplateAuditStatus(String checkStatus) {
        switch (checkStatus) {
            case "CHECKING": return SmsTemplateAuditStatusEnum.CHECKING.getStatus();
            case "SUCCESS": return SmsTemplateAuditStatusEnum.SUCCESS.getStatus();
            case "FAIL": return SmsTemplateAuditStatusEnum.FAIL.getStatus();
            default: throw new IllegalArgumentException(String.format("未知审核状态(%s)", checkStatus));
        }
    }

    @VisibleForTesting
    <T, R> SmsCommonResult<R> invoke(Supplier<Result<T>> requestConsumer, Function<T, R> responseConsumer) throws Throwable {
        // 执行请求
        Result<T> result = requestConsumer.get();
        if (result.getThrowable() != null) {
            throw result.getThrowable();
        }
        // 解析结果
        R data = null;
        if (result.getData() != null) {
            data = responseConsumer.apply(result.getData());
        }
        // 拼接结果
        return SmsCommonResult.build(String.valueOf(result.getCode()), formatResultMsg(result), null, data, codeMapping);
    }

    private static String formatResultMsg(Result<?> sendResult) {
        if (StrUtil.isEmpty(sendResult.getDetail())) {
            return sendResult.getMsg();
        }
        return sendResult.getMsg() + " => " + sendResult.getDetail();
    }

    /**
     * 短信接收状态
     *
     * 参见 https://www.yunpian.com/official/document/sms/zh_cn/domestic_push_report 文档
     *
     * @author 芋道源码
     */
    @Data
    public static class SmsReceiveStatus {

        /**
         * 接收状态
         *
         * 目前仅有 SUCCESS / FAIL，所以使用 Boolean 接收
         */
        @JsonProperty("report_status")
        private String reportStatus;
        /**
         * 接收手机号
         */
        private String mobile;
        /**
         * 运营商返回的代码，如："DB:0103"
         *
         * 由于不同运营商信息不同，此字段仅供参考；
         */
        @JsonProperty("error_msg")
        private String errorMsg;
        /**
         * 运营商反馈代码的中文解释
         *
         * 默认不推送此字段，如需推送，请联系客服
         */
        @JsonProperty("error_detail")
        private String errorDetail;
        /**
         * 短信编号
         */
        private Long sid;
        /**
         * 用户自定义 id
         *
         * 这里我们传递的是 SysSmsLogDO 的日志编号
         */
        private Long uid;
        /**
         * 用户接收时间
         */
        @JsonProperty("user_receive_time")
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private Date userReceiveTime;

    }

}
