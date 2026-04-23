package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表 deepay_order
 */
@TableName("deepay_order")
@Data
public class DeepayOrderDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 全局唯一支付 ID */
    private String paymentId;

    /** 关联链码 */
    private String chainCode;

    /** 下单用户 ID（与 chain_code 联合唯一，防止同一用户重复下单） */
    private Long userId;

    /** 订单状态：PENDING / PAID / CANCELLED */
    private String status;

    /** 实收金额 */
    private BigDecimal amount;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

}
