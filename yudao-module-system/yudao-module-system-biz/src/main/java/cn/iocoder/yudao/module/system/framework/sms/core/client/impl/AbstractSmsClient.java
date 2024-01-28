package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.iocoder.yudao.module.system.framework.sms.core.client.SmsClient;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import lombok.extern.slf4j.Slf4j;

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

    public AbstractSmsClient(SmsChannelProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化
     */
    public final void init() {
        doInit();
        log.debug("[init][配置({}) 初始化完成]", properties);
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
        this.properties = properties;
        // 初始化
        this.init();
    }

    @Override
    public Long getId() {
        return properties.getId();
    }

}
