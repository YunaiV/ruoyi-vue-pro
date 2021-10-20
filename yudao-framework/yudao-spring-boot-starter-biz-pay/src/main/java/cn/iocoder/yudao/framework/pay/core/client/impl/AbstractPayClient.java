package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayCodeMapping;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付客户端的抽象类，提供模板方法，减少子类的冗余代码
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class AbstractPayClient<Config extends PayClientConfig> implements PayClient {

    /**
     * 渠道编号
     */
    private final Long channelId;
    /**
     * 渠道编码
     */
    private final String channelCode;
    /**
     * 错误码枚举类
     */
    protected PayCodeMapping codeMapping;
    /**
     * 支付配置
     */
    protected Config config;

    protected Double calculateAmount(Integer amount) {
        return amount / 100.0;
    }

    public AbstractPayClient(Long channelId, String channelCode, Config config, PayCodeMapping codeMapping) {
        this.channelId = channelId;
        this.channelCode = channelCode;
        this.codeMapping = codeMapping;
        this.config = config;
    }

    /**
     * 初始化
     */
    public final void init() {
        doInit();
        log.info("[init][配置({}) 初始化完成]", config);
    }

    /**
     * 自定义初始化
     */
    protected abstract void doInit();

    public final void refresh(Config config) {
        // 判断是否更新
        if (config.equals(this.config)) {
            return;
        }
        log.info("[refresh][配置({})发生变化，重新初始化]", config);
        this.config = config;
        // 初始化
        this.init();
    }

    @Override
    public Long getId() {
        return channelId;
    }

}
