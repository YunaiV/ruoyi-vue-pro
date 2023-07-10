package cn.iocoder.yudao.framework.pay.core.client.dto.order;

import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
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
     * 支付状态
     *
     * 枚举 {@link PayOrderStatusRespEnum} 类
     */
    private Integer status;

    public PayOrderUnifiedRespDTO(String displayMode, String displayContent) {
        this(displayMode, displayContent, PayOrderStatusRespEnum.WAITING.getStatus());
    }

    public PayOrderUnifiedRespDTO(String displayMode, String displayContent, Integer status) {
        this.displayMode = displayMode;
        this.displayContent = displayContent;
        this.status = status;
    }
}
