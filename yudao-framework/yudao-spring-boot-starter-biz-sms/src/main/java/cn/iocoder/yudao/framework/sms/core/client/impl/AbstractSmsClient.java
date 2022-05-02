package cn.iocoder.yudao.framework.sms.core.client.impl;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.sms.core.client.SmsClient;
import cn.iocoder.yudao.framework.sms.core.client.SmsCodeMapping;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 短信客户端的抽象类，提供模板方法，减少子类的冗余代码
 *
 * @author zzf
 * @since 2021/2/1 9:28
 */
@Slf4j
public abstract class AbstractSmsClient implements SmsClient {

    /**
     * 短信渠道配置
     */
    protected volatile SmsChannelProperties properties;
    /**
     * 错误码枚举类
     */
    protected final SmsCodeMapping codeMapping;

    public AbstractSmsClient(SmsChannelProperties properties, SmsCodeMapping codeMapping) {
        this.properties = prepareProperties(properties);
        this.codeMapping = codeMapping;
    }

    /**
     * 初始化
     */
    public final void init() {
        doInit();
        log.info("[init][配置({}) 初始化完成]", properties);
    }

    /**
     * 自定义初始化
     */
    protected abstract void doInit();

    public final void refresh(SmsChannelProperties properties) {
        // 判断是否更新
        if (properties.equals(this.properties)) {
            return;
        }
        log.info("[refresh][配置({})发生变化，重新初始化]", properties);
        this.properties = prepareProperties(properties);
        // 初始化
        this.init();
    }

    /**
     * 在赋值给{@link this#properties}前，子类可根据需要预处理短信渠道配置
     *
     * @param properties 数据库中存储的短信渠道配置
     * @return 满足子类实现的短信渠道配置
     */
    protected SmsChannelProperties prepareProperties(SmsChannelProperties properties) {
        return properties;
    }

    @Override
    public Long getId() {
        return properties.getId();
    }

    @Override
    public final SmsCommonResult<SmsSendRespDTO> sendSms(Long logId, String mobile,
                                                         String apiTemplateId, List<KeyValue<String, Object>> templateParams) {
        // 执行短信发送
        SmsCommonResult<SmsSendRespDTO> result;
        try {
            result = doSendSms(logId, mobile, apiTemplateId, templateParams);
        } catch (Throwable ex) {
            // 打印异常日志
            log.error("[sendSms][发送短信异常，sendLogId({}) mobile({}) apiTemplateId({}) templateParams({})]",
                    logId, mobile, apiTemplateId, templateParams, ex);
            // 封装返回
            return SmsCommonResult.error(ex);
        }
        return result;
    }

    protected abstract SmsCommonResult<SmsSendRespDTO> doSendSms(Long sendLogId, String mobile,
                                                                 String apiTemplateId, List<KeyValue<String, Object>> templateParams)
            throws Throwable;

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) throws Throwable {
        try {
            return doParseSmsReceiveStatus(text);
        } catch (Throwable ex) {
            log.error("[parseSmsReceiveStatus][text({}) 解析发生异常]", text, ex);
            throw ex;
        }
    }

    protected abstract List<SmsReceiveRespDTO> doParseSmsReceiveStatus(String text) throws Throwable;

    @Override
    public SmsCommonResult<SmsTemplateRespDTO> getSmsTemplate(String apiTemplateId) {
        // 执行短信发送
        SmsCommonResult<SmsTemplateRespDTO> result;
        try {
            result = doGetSmsTemplate(apiTemplateId);
        } catch (Throwable ex) {
            // 打印异常日志
            log.error("[getSmsTemplate][获得短信模板({}) 发生异常]", apiTemplateId, ex);
            // 封装返回
            return SmsCommonResult.error(ex);
        }
        return result;
    }

    protected abstract SmsCommonResult<SmsTemplateRespDTO> doGetSmsTemplate(String apiTemplateId) throws Throwable;

}
