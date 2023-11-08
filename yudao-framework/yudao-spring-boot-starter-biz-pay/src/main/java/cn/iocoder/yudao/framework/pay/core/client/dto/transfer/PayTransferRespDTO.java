package cn.iocoder.yudao.framework.pay.core.client.dto.transfer;

import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferStatusRespEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一转账 Response DTO
 *
 * @author jason
 */
@Data
public class PayTransferRespDTO {

    /**
     * 转账状态
     *
     * 关联 {@link  PayTransferStatusRespEnum#getStatus()}
     */
    private Integer status;

    /**
     * 外部转账单号
     *
     */
    private String outTransferNo;

    /**
     * 支付渠道编号
     */
    private String channelTransferNo;

    /**
     * 支付成功时间
     */
    private LocalDateTime successTime;

    /**
     * 原始的返回结果
     */
    private Object rawData;

    /**
     * 调用渠道的错误码
     */
    private String channelErrorCode;
    /**
     * 调用渠道报错时，错误信息
     */
    private String channelErrorMsg;

    /**
     * 创建【WAITING】状态的转账返回
     */
    public static PayTransferRespDTO waitingOf(String channelTransferNo,
                                             String outTransferNo, Object rawData) {
        PayTransferRespDTO respDTO = new PayTransferRespDTO();
        respDTO.status = PayTransferStatusRespEnum.WAITING.getStatus();
        respDTO.channelTransferNo = channelTransferNo;
        respDTO.outTransferNo = outTransferNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

    /**
     * 创建【IN_PROGRESS】状态的转账返回
     */
    public static PayTransferRespDTO dealingOf(String channelTransferNo,
                                               String outTransferNo, Object rawData) {
        PayTransferRespDTO respDTO = new PayTransferRespDTO();
        respDTO.status = PayTransferStatusRespEnum.IN_PROGRESS.getStatus();
        respDTO.channelTransferNo = channelTransferNo;
        respDTO.outTransferNo = outTransferNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

    /**
     * 创建【CLOSED】状态的转账返回
     */
    public static PayTransferRespDTO closedOf(String channelErrorCode, String channelErrorMsg,
                                              String outTransferNo, Object rawData) {
        PayTransferRespDTO respDTO = new PayTransferRespDTO();
        respDTO.status = PayTransferStatusRespEnum.CLOSED.getStatus();
        respDTO.channelErrorCode = channelErrorCode;
        respDTO.channelErrorMsg = channelErrorMsg;
        // 相对通用的字段
        respDTO.outTransferNo = outTransferNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

    /**
     * 创建【SUCCESS】状态的转账返回
     */
    public static PayTransferRespDTO successOf(String channelTransferNo, LocalDateTime successTime,
                                             String outTransferNo, Object rawData) {
        PayTransferRespDTO respDTO = new PayTransferRespDTO();
        respDTO.status = PayTransferStatusRespEnum.SUCCESS.getStatus();
        respDTO.channelTransferNo = channelTransferNo;
        respDTO.successTime = successTime;
        // 相对通用的字段
        respDTO.outTransferNo = outTransferNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

}
