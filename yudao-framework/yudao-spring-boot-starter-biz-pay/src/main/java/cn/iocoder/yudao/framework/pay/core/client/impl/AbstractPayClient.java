package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.exception.PayException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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
    @SuppressWarnings("FieldCanBeLocal")
    private final String channelCode;
    /**
     * 支付配置
     */
    @Getter
    protected Config config;

    public AbstractPayClient(Long channelId, String channelCode, Config config) {
        this.channelId = channelId;
        this.channelCode = channelCode;
        this.config = config;
    }

    /**
     * 初始化
     */
    public final void init() {
        doInit();
        log.info("[init][客户端({}) 初始化完成]", getId());
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
        log.info("[refresh][客户端({})发生变化，重新初始化]", getId());
        this.config = config;
        // 初始化
        this.init();
    }

    @Override
    public Long getId() {
        return channelId;
    }

    // ============ 支付相关 ==========

    @Override
    public final PayOrderRespDTO unifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        ValidationUtils.validate(reqDTO);
        // 执行统一下单
        PayOrderRespDTO resp;
        try {
            resp = doUnifiedOrder(reqDTO);
        } catch (ServiceException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
            throw ex;
        } catch (Throwable ex) {
            // 系统异常，则包装成 PayException 异常抛出
            log.error("[unifiedOrder][客户端({}) request({}) 发起支付异常]",
                    getId(), toJsonString(reqDTO), ex);
            throw buildPayException(ex);
        }
        return resp;
    }

    protected abstract PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO)
            throws Throwable;

    @Override
    public final PayOrderRespDTO parseOrderNotify(Map<String, String> params, String body) {
        try {
            return doParseOrderNotify(params, body);
        } catch (ServiceException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
            throw ex;
        } catch (Throwable ex) {
            log.error("[parseOrderNotify][客户端({}) params({}) body({}) 解析失败]",
                    getId(), params, body, ex);
            throw buildPayException(ex);
        }
    }

    protected abstract PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body)
            throws Throwable;

    @Override
    public final PayOrderRespDTO getOrder(String outTradeNo) {
        try {
            return doGetOrder(outTradeNo);
        } catch (ServiceException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
            throw ex;
        } catch (Throwable ex) {
            log.error("[getOrder][客户端({}) outTradeNo({}) 查询支付单异常]",
                    getId(), outTradeNo, ex);
            throw buildPayException(ex);
        }
    }

    protected abstract PayOrderRespDTO doGetOrder(String outTradeNo)
            throws Throwable;

    // ============ 退款相关 ==========

    @Override
    public final PayRefundRespDTO unifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        ValidationUtils.validate(reqDTO);
        // 执行统一退款
        PayRefundRespDTO resp;
        try {
            resp = doUnifiedRefund(reqDTO);
        } catch (ServiceException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
            throw ex;
        } catch (Throwable ex) {
            // 系统异常，则包装成 PayException 异常抛出
            log.error("[unifiedRefund][客户端({}) request({}) 发起退款异常]",
                    getId(), toJsonString(reqDTO), ex);
            throw buildPayException(ex);
        }
        return resp;
    }

    protected abstract PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable;

    @Override
    public final PayRefundRespDTO parseRefundNotify(Map<String, String> params, String body) {
        try {
            return doParseRefundNotify(params, body);
        } catch (ServiceException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
            throw ex;
        } catch (Throwable ex) {
            log.error("[parseRefundNotify][客户端({}) params({}) body({}) 解析失败]",
                    getId(), params, body, ex);
            throw buildPayException(ex);
        }
    }

    protected abstract PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body)
            throws Throwable;

    @Override
    public final PayRefundRespDTO getRefund(String outTradeNo, String outRefundNo) {
        try {
            return doGetRefund(outTradeNo, outRefundNo);
        } catch (ServiceException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
            throw ex;
        } catch (Throwable ex) {
            log.error("[getRefund][客户端({}) outTradeNo({}) outRefundNo({}) 查询退款单异常]",
                    getId(), outTradeNo, outRefundNo, ex);
            throw buildPayException(ex);
        }
    }

    protected abstract PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo)
            throws Throwable;

    // ========== 各种工具方法 ==========

    private PayException buildPayException(Throwable ex) {
        if (ex instanceof PayException) {
            return (PayException) ex;
        }
        throw new PayException(ex);
    }

}
