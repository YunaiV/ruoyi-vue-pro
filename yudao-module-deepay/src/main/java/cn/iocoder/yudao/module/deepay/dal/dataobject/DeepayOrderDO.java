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

    /** 实收金额（使用 BigDecimal，禁止 double/float） */
    private BigDecimal amount;

    /**
     * 货币代码（ISO 4217），全系统统一 EUR（欧元）。
     * Jeepay 创建支付单时传入此字段。
     */
    private String currency = "EUR";

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

}
