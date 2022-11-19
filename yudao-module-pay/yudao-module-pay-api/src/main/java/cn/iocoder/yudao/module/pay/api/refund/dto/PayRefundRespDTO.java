package cn.iocoder.yudao.module.pay.api.refund.dto;

import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款单信息 Response DTO
 *
 * TODO 芋艿：还没定好字段
 *
 * @author 芋道源码
 */
@Data
public class PayRefundRespDTO {

    /**
     * 退款单编号
     */
    private Long id;

    // ========== 退款相关字段 ==========
    /**
     * 退款状态
     *
     * 枚举 {@link PayRefundStatusEnum}
     */
    private Integer status;

    // ========== 渠道相关字段 ==========
    /**
     * 退款成功时间
     */
    private LocalDateTime successTime;

}
