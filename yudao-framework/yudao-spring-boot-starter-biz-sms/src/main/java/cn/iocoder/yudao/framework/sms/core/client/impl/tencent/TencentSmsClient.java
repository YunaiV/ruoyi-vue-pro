package cn.iocoder.yudao.framework.sms.core.client.impl.tencent;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.impl.AbstractSmsClient;
import cn.iocoder.yudao.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 腾讯云短信功能实现
 * <p>
 * 参见 https://cloud.tencent.com/document/product/382/52077
 *
 * @author shiwp
 */
public class TencentSmsClient extends AbstractSmsClient {

    /**
     * 调用成功 code
     */
    public static final String API_SUCCESS_CODE = "Ok";

    /**
     * REGION，使用南京
     */
    private static final String ENDPOINT = "ap-nanjing";

    /**
     * 是否国际/港澳台短信：
     * 0：表示国内短信。
     * 1：表示国际/港澳台短信。
     */
    private static final long INTERNATIONAL = 0L;

    private SmsClient client;

    public TencentSmsClient(SmsChannelProperties properties) {
        super(properties, new TencentSmsCodeMapping());
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    @Override
    protected void doInit() {
        // 实例化一个认证对象，入参需要传入腾讯云账户密钥对 secretId，secretKey
        Credential credential = new Credential(properties.getApiKey(), properties.getApiSecret());
        client = new SmsClient(credential, ENDPOINT);
    }

    @Override
    protected SmsCommonResult<SmsSendRespDTO> doSendSms(Long sendLogId,
                                                        String mobile,
                                                        String apiTemplateId,
                                                        List<KeyValue<String, Object>> templateParams) throws Throwable {
        return invoke(() -> buildSendSmsRequest(sendLogId, mobile, apiTemplateId, templateParams),
                this::doSendSms0,
                response -> {
                    SendStatus sendStatus = response.getSendStatusSet()[0];
                    return SmsCommonResult.build(sendStatus.getCode(), sendStatus.getMessage(), response.getRequestId(),
                            new SmsSendRespDTO().setSerialNo(sendStatus.getSerialNo()), codeMapping);
                });
    }


    /**
     * 腾讯云发放短信的时候，需要额外的参数 sdkAppId。
     * 考虑到不破坏原有的 apiKey + apiSecret 的结构，所以将 secretId 拼接到 apiKey 字段中，格式为 "secretId sdkAppId"。
     * 因此，这边需要使用 TencentSmsChannelProperties 做拆分，重新封装到 properties 内。
     *
     * @param properties 数据库中存储的短信渠道配置
     * @return TencentSmsChannelProperties
     */
    @Override
    protected SmsChannelProperties prepareProperties(SmsChannelProperties properties) {
        return TencentSmsChannelProperties.build(properties);
    }

    /**
     * 调用腾讯云 SDK 发送短信
     *
     * @param request 发送短信请求
     * @return 发送短信响应
     * @throws TencentCloudSDKException SDK 用来封装发送短信失败
     */
    private SendSmsResponse doSendSms0(SendSmsRequest request) throws TencentCloudSDKException {
        return client.SendSms(request);
    }

    /**
     * 封装腾讯云发送短信请求
     *
     * @param sendLogId      日志编号
     * @param mobile         手机号
     * @param apiTemplateId  短信 API 的模板编号
     * @param templateParams 短信模板参数。通过 List 数组，保证参数的顺序
     * @return 腾讯云发送短信请求
     */
    private SendSmsRequest buildSendSmsRequest(Long sendLogId,
                                               String mobile,
                                               String apiTemplateId,
                                               List<KeyValue<String, Object>> templateParams) {
        SendSmsRequest request = new SendSmsRequest();
        request.setSmsSdkAppId(((TencentSmsChannelProperties) properties).getSdkAppId());
        request.setPhoneNumberSet(new String[]{mobile});
        request.setSignName(properties.getSignature());
        request.setTemplateId(apiTemplateId);
        request.setTemplateParamSet(ArrayUtils.toArray(templateParams, e -> String.valueOf(e.getValue())));
        request.setSessionContext(JsonUtils.toJsonString(new SessionContext().setLogId(sendLogId)));
        return request;
    }

    @Override
    protected List<SmsReceiveRespDTO> doParseSmsReceiveStatus(String text) throws Throwable {
        List<SmsReceiveStatus> callback = JsonUtils.parseArray(text, SmsReceiveStatus.class);
        return CollectionUtils.convertList(callback, status -> {
            SmsReceiveRespDTO data = new SmsReceiveRespDTO();
            data.setErrorCode(status.getErrCode()).setErrorMsg(status.getDescription());
            data.setReceiveTime(status.getReceiveTime()).setSuccess(SmsReceiveStatus.SUCCESS_CODE.equalsIgnoreCase(status.getStatus()));
            data.setMobile(status.getMobile()).setSerialNo(status.getSerialNo());
            SessionContext context;
            Long logId;
            Assert.notNull(context = status.getSessionContext(), "回执信息中未解析出 context，请联系腾讯云小助手");
            Assert.notNull(logId = context.getLogId(), "回执信息中未解析出 logId，请联系腾讯云小助手");
            data.setLogId(logId);
            return data;
        });
    }

    @Override
    protected SmsCommonResult<SmsTemplateRespDTO> doGetSmsTemplate(String apiTemplateId) throws Throwable {
        return invoke(() -> this.buildSmsTemplateStatusRequest(apiTemplateId),
                this::doGetSmsTemplate0,
                response -> {
                    SmsTemplateRespDTO data = convertTemplateStatusDTO(response.getDescribeTemplateStatusSet()[0]);
                    return SmsCommonResult.build(API_SUCCESS_CODE, null, response.getRequestId(), data, codeMapping);
                });
    }

    @VisibleForTesting
    SmsTemplateRespDTO convertTemplateStatusDTO(DescribeTemplateListStatus templateStatus) {
        if (templateStatus == null) {
            return null;
        }
        SmsTemplateAuditStatusEnum auditStatus;
        Assert.notNull(templateStatus.getStatusCode(),
                StrUtil.format("短信模版审核状态为 null，模版 id{}", templateStatus.getTemplateId()));
        switch (templateStatus.getStatusCode().intValue()) {
            case -1:
                auditStatus = SmsTemplateAuditStatusEnum.FAIL;
                break;
            case 0:
                auditStatus = SmsTemplateAuditStatusEnum.SUCCESS;
                break;
            case 1:
                auditStatus = SmsTemplateAuditStatusEnum.CHECKING;
                break;
            default:
                throw new IllegalStateException(StrUtil.format("不能解析短信模版审核状态{}，模版 id{}",
                        templateStatus.getStatusCode(), templateStatus.getTemplateId()));
        }
        SmsTemplateRespDTO data = new SmsTemplateRespDTO();
        data.setId(String.valueOf(templateStatus.getTemplateId())).setContent(templateStatus.getTemplateContent());
        data.setAuditStatus(auditStatus.getStatus()).setAuditReason(templateStatus.getReviewReply());
        return data;
    }

    /**
     * 封装查询模版审核状态请求
     * @param apiTemplateId api 的模版 id
     * @return 查询模版审核状态请求
     */
    private DescribeSmsTemplateListRequest buildSmsTemplateStatusRequest(String apiTemplateId) {
        DescribeSmsTemplateListRequest request = new DescribeSmsTemplateListRequest();
        request.setTemplateIdSet(new Long[]{Long.parseLong(apiTemplateId)});
        // 地区 0：表示国内短信。1：表示国际/港澳台短信。
        request.setInternational(INTERNATIONAL);
        return request;
    }

    /**
     * 调用腾讯云 SDK 查询短信模版状态
     *
     * @param request 查询短信模版状态请求
     * @return 查询短信模版状态响应
     * @throws TencentCloudSDKException SDK 用来封装查询短信模版状态失败
     */
    private DescribeSmsTemplateListResponse doGetSmsTemplate0(DescribeSmsTemplateListRequest request) throws TencentCloudSDKException {
        return client.DescribeSmsTemplateList(request);
    }

    <Q, P, R> SmsCommonResult<R> invoke(Supplier<Q> requestSupplier,
                                        SdkFunction<Q, P> responseSupplier,
                                        Function<P, SmsCommonResult<R>> resultGen) {
        // 构建请求body
        Q request = requestSupplier.get();
        P response;
        // 调用腾讯云发送短信
        try {
            response = responseSupplier.apply(request);
        } catch (TencentCloudSDKException e) {
            // 调用异常，封装结果
            return SmsCommonResult.build(e.getErrorCode(), e.getMessage(), e.getRequestId(), null, codeMapping);
        }
        return resultGen.apply(response);
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
         * 用户的 session 内容（与发送接口的请求参数SessionContext一致）
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

    private interface SdkFunction<T, R> {
        R apply(T t) throws TencentCloudSDKException;
    }

}
