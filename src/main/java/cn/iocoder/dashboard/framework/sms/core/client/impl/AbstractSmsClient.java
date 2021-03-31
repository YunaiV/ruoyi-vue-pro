package cn.iocoder.dashboard.framework.sms.core.client.impl;

import cn.iocoder.dashboard.framework.sms.core.client.SmsCodeMapping;
import cn.iocoder.dashboard.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.dashboard.framework.sms.core.client.SmsClient;
import cn.iocoder.dashboard.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 短信客户端抽象类
 *
 * @author zzf
 * @date 2021/2/1 9:28
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

    /**
     * 短信客户端有参构造函数
     *
     * @param properties 短信配置
     */
    public AbstractSmsClient(SmsChannelProperties properties, SmsCodeMapping codeMapping) {
        this.properties = properties;
        this.codeMapping = codeMapping;
    }

    /**
     * 初始化
     */
    public final void init() {
        doInit();
        log.info("[init][配置({}) 初始化完成]", properties);
    }

    public final void refresh(SmsChannelProperties properties) {
        // 判断是否更新
        if (!properties.equals(this.properties)) {
            return;
        }
        log.info("[refresh][配置({})发生变化，重新初始化]", properties);
        this.properties = properties;
        // 初始化
        this.init();
    }

    /**
     * 自定义初始化
     */
    protected abstract void doInit();

    @Override
    public Long getId() {
        return properties.getId();
    }

    @Override
    public final SmsCommonResult<SmsSendRespDTO> send(Long sendLogId, String mobile,
                                                      String apiTemplateId, Map<String, Object> templateParams) {
        // 执行短信发送
        SmsCommonResult<SmsSendRespDTO> result;
        try {
            result = doSend(sendLogId, mobile, apiTemplateId, templateParams);
        } catch (Throwable ex) {
            // 打印异常日志
            log.error("[send][发送短信异常，sendLogId({}) mobile({}) apiTemplateId({}) templateParams({})]",
                    sendLogId, mobile, apiTemplateId, templateParams, ex);
            // 封装返回
            return SmsCommonResult.error(ex);
        }
        return result;
    }

    /**
     * 发送消息
     *
     * @param sendLogId 发送日志编号
     * @param mobile 手机号
     * @param apiTemplateId 短信 API 的模板编号
     * @param templateParams 短信模板参数
     * @return 短信发送结果
     */
    protected abstract SmsCommonResult<SmsSendRespDTO> doSend(Long sendLogId, String mobile,
                                                              String apiTemplateId, Map<String, Object> templateParams) throws Throwable;

}
