package cn.iocoder.yudao.framework.pay.core.client.dto.refund;

import cn.iocoder.yudao.framework.pay.core.client.exception.PayException;
import cn.iocoder.yudao.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 渠道退款订单 Response DTO
 *
 * @author jason
 */
@Data
public class PayRefundRespDTO {

    /**
     * 退款状态
     *
     * 枚举 {@link PayRefundStatusRespEnum}
     */
    private Integer status;

    /**
     * 外部退款号
     *
     * 对应 PayRefundDO 的 no 字段
     */
    private String outRefundNo;

    /**
     * 渠道退款单号
     *
     * 对应 PayRefundDO.channelRefundNo 字段
     */
    private String channelRefundNo;

    /**
     * 退款成功时间
     */
    private LocalDateTime successTime;

    /**
     * 原始的异步通知结果
     */
    private Object rawData;

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

    private PayRefundRespDTO() {
    }

    /**
     * 创建【WAITING】状态的退款返回
     */
    public static PayRefundRespDTO waitingOf(String channelRefundNo,
                                             String outRefundNo, Object rawData) {
        PayRefundRespDTO respDTO = new PayRefundRespDTO();
        respDTO.status = PayRefundStatusRespEnum.WAITING.getStatus();
        respDTO.channelRefundNo = channelRefundNo;
        // 相对通用的字段
        respDTO.outRefundNo = outRefundNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

    /**
     * 创建【SUCCESS】状态的退款返回
     */
    public static PayRefundRespDTO successOf(String channelRefundNo, LocalDateTime successTime,
                                             String outRefundNo, Object rawData) {
        PayRefundRespDTO respDTO = new PayRefundRespDTO();
        respDTO.status = PayRefundStatusRespEnum.SUCCESS.getStatus();
        respDTO.channelRefundNo = channelRefundNo;
        respDTO.successTime = successTime;
        // 相对通用的字段
        respDTO.outRefundNo = outRefundNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

    /**
     * 创建【FAILURE】状态的退款返回
     */
    public static PayRefundRespDTO failureOf(String outRefundNo, Object rawData) {
        return failureOf(null, null,
                outRefundNo, rawData);
    }

    /**
     * 创建【FAILURE】状态的退款返回
     */
    public static PayRefundRespDTO failureOf(String channelErrorCode, String channelErrorMsg,
                                             String outRefundNo, Object rawData) {
        PayRefundRespDTO respDTO = new PayRefundRespDTO();
        respDTO.status = PayRefundStatusRespEnum.FAILURE.getStatus();
        respDTO.channelErrorCode = channelErrorCode;
        respDTO.channelErrorMsg = channelErrorMsg;
        // 相对通用的字段
        respDTO.outRefundNo = outRefundNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

}
