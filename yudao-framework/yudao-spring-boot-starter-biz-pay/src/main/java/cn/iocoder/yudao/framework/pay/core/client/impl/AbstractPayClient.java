package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedRespDTO;
import com.alipay.api.AlipayResponse;import lombok.extern.slf4j.Slf4j;

import javax.validation.Validation;
import java.time.LocalDateTime;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;
import static cn.hutool.core.date.DatePattern.NORM_DATETIME_MS_FORMATTER;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.pay.core.enums.PayFrameworkErrorCodeConstants.PAY_EXCEPTION;

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
     * 支付配置
     */
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

    @Override
    public final PayOrderUnifiedRespDTO unifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        Validation.buildDefaultValidatorFactory().getValidator().validate(reqDTO);
        // 执行短信发送
        PayOrderUnifiedRespDTO result;
        try {
            result = doUnifiedOrder(reqDTO);
        } catch (Throwable ex) {
            // 打印异常日志
            log.error("[unifiedOrder][request({}) 发起支付失败]", toJsonString(reqDTO), ex);
            throw buildException(ex);
        }
        return result;
    }

    protected abstract PayOrderUnifiedRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO)
            throws Throwable;

    @Override
    public PayRefundUnifiedRespDTO unifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        PayRefundUnifiedRespDTO resp;
        try {
            resp = doUnifiedRefund(reqDTO);
        }  catch (Throwable ex) {
            // 记录异常日志
            log.error("[unifiedRefund][request({}) 发起退款失败]", toJsonString(reqDTO), ex);
            throw buildException(ex);
        }
        return resp;
    }

    protected abstract PayRefundUnifiedRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable;

    // ========== 各种工具方法 ==========

    private RuntimeException buildException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        }
        throw new RuntimeException(ex);
    }

    protected void validateSuccess(AlipayResponse response) {
        if (response.isSuccess()) {
            return;
        }
        throw exception0(PAY_EXCEPTION.getCode(), response.getSubMsg());
    }

    protected String formatAmount(Integer amount) {
        return String.valueOf(amount / 100.0);
    }

    protected String formatTime(LocalDateTime time) {
        // "yyyy-MM-dd HH:mm:ss"
        return LocalDateTimeUtil.format(time, NORM_DATETIME_FORMATTER);
    }

    protected LocalDateTime parseTime(String str) {
        // "yyyy-MM-dd HH:mm:ss"
        return LocalDateTimeUtil.parse(str, NORM_DATETIME_FORMATTER);
    }

}
