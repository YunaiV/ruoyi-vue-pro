package cn.iocoder.yudao.framework.pay.core.client.dto.order;

import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import lombok.Data;

/**
 * 统一下单 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class PayOrderUnifiedRespDTO {

    /**
     * 展示模式
     *
     * 枚举 {@link PayOrderDisplayModeEnum} 类
     */
    private String displayMode;
    /**
     * 展示内容
     */
    private String displayContent;

    /**
     * 渠道支付订单
     *
     * 只有在订单直接支付成功时，才会进行返回。
     * 目前只有 bar 条码支付才会出现，它是支付发起时，直接返回是否支付成功的，而其它支付还是异步通知
     */
    private PayOrderRespDTO order;

    public PayOrderUnifiedRespDTO(String displayMode, String displayContent) {
        this.displayMode = displayMode;
        this.displayContent = displayContent;
    }

}
