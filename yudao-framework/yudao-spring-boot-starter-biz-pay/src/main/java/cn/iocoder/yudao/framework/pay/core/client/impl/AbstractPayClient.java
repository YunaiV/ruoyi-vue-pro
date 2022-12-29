package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.iocoder.yudao.framework.pay.core.client.AbstractPayCodeMapping;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedRespDTO;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Validation;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

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
    protected AbstractPayCodeMapping codeMapping;
    /**
     * 支付配置
     */
    protected Config config;

    public AbstractPayClient(Long channelId, String channelCode, Config config, AbstractPayCodeMapping codeMapping) {
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

    protected Double calculateAmount(Integer amount) {
        return amount / 100.0;
    }

    @Override
    public Long getId() {
        return channelId;
    }

    @Override
    public final PayCommonResult<?> unifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        Validation.buildDefaultValidatorFactory().getValidator().validate(reqDTO);
        // 执行短信发送
        PayCommonResult<?> result;
        try {
            result = doUnifiedOrder(reqDTO);
        } catch (Throwable ex) {
            // 打印异常日志
            log.error("[unifiedOrder][request({}) 发起支付失败]", toJsonString(reqDTO), ex);
            // 封装返回
            return PayCommonResult.error(ex);
        }
        return result;
    }

    protected abstract PayCommonResult<?> doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO)
            throws Throwable;

    @Override
    public PayCommonResult<PayRefundUnifiedRespDTO> unifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        PayCommonResult<PayRefundUnifiedRespDTO> resp;
        try {
            resp = doUnifiedRefund(reqDTO);
        }  catch (Throwable ex) {
            // 记录异常日志
            log.error("[unifiedRefund][request({}) 发起退款失败]", toJsonString(reqDTO), ex);
            resp = PayCommonResult.error(ex);
        }
        return resp;
    }

    protected abstract PayCommonResult<PayRefundUnifiedRespDTO> doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable;

}
