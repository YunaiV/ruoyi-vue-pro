package cn.iocoder.yudao.framework.pay.core.client.dto.order;

import cn.iocoder.yudao.framework.pay.core.client.exception.PayException;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 渠道支付订单 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class PayOrderRespDTO {

    /**
     * 支付状态
     *
     * 枚举：{@link PayOrderStatusRespEnum}
     */
    private Integer status;

    /**
     * 外部订单号
     *
     * 对应 PayOrderExtensionDO 的 no 字段
     */
    private String outTradeNo;

    /**
     * 支付渠道编号
     */
    private String channelOrderNo;
    /**
     * 支付渠道用户编号
     */
    private String channelUserId;

    /**
     * 支付成功时间
     */
    private LocalDateTime successTime;

    /**
     * 原始的同步/异步通知结果
     */
    private Object rawData;

    // ========== 主动发起支付时，会返回的字段 ==========

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
     * 调用渠道的错误码
     *
     * 注意：这里返回的是业务异常，而是不系统异常。
     * 如果是系统异常，则会抛出 {@link PayException}
     */
    private String channelErrorCode;
    /**
     * 调用渠道报错时，错误信息
     */
    private String channelErrorMsg;

    public PayOrderRespDTO() {
    }

    /**
     * 创建【WAITING】状态的订单返回
     */
    public PayOrderRespDTO(String displayMode, String displayContent,
                           String outTradeNo, Object rawData) {
        this.status = PayOrderStatusRespEnum.WAITING.getStatus();
        this.displayMode = displayMode;
        this.displayContent = displayContent;
        // 相对通用的字段
        this.outTradeNo = outTradeNo;
        this.rawData = rawData;
    }

    /**
     * 创建【SUCCESS】状态的订单返回
     */
    public PayOrderRespDTO(String channelOrderNo, String channelUserId, LocalDateTime successTime,
                           String outTradeNo, Object rawData) {
        this.status = PayOrderStatusRespEnum.SUCCESS.getStatus();
        this.channelOrderNo = channelOrderNo;
        this.channelUserId = channelUserId;
        this.successTime = successTime;
        // 相对通用的字段
        this.outTradeNo = outTradeNo;
        this.rawData = rawData;
    }

    /**
     * 创建【SUCCESS】或【CLOSED】状态的订单返回，适合支付渠道回调时
     */
    public PayOrderRespDTO(Integer status, String channelOrderNo, String channelUserId, LocalDateTime successTime,
                           String outTradeNo, Object rawData) {
        this.status = status;
        this.channelOrderNo = channelOrderNo;
        this.channelUserId = channelUserId;
        this.successTime = successTime;
        // 相对通用的字段
        this.outTradeNo = outTradeNo;
        this.rawData = rawData;
    }

    /**
     * 创建【CLOSED】状态的订单返回，适合调用支付渠道失败时
     *
     * 参数和 {@link #PayOrderRespDTO(String, String, String, Object)} 冲突，所以独立个方法出来
     */
    public static PayOrderRespDTO build(String channelErrorCode, String channelErrorMsg,
                                        String outTradeNo, Object rawData) {
        PayOrderRespDTO respDTO = new PayOrderRespDTO();
        respDTO.status = PayOrderStatusRespEnum.CLOSED.getStatus();
        respDTO.channelErrorCode = channelErrorCode;
        respDTO.channelErrorMsg = channelErrorMsg;
        // 相对通用的字段
        respDTO.outTradeNo = outTradeNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

}
