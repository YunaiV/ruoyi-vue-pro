package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Deepay 订单表。
 *
 * <p>对应数据库表 {@code deepay_order}。
 * 状态流转：INIT → PAID。</p>
 */
@TableName("deepay_order")
@Data
public class DeepayOrderDO {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联商品链码 */
    private String chainCode;

    /** 下单用户 ID（与 chain_code 联合唯一，防止同一用户重复下单） */
    private Long userId;

    /** 订单状态：PENDING / PAID / CANCELLED */
    private String status;

    /** 实收金额（使用 BigDecimal，禁止 double/float） */
    private BigDecimal amount;

    /**
     * 基准金额（EUR）— 真实结算金额，等于 product.basePrice。
     * 分析、利润计算统一用此字段，不用 displayAmount。
     */
    private BigDecimal baseAmount;

    /**
     * 展示金额（用户看到的货币金额）。
     * Jeepay 创建支付单时使用此金额（分 = displayAmount × 100）。
     * 例：用户选 USD → displayAmount = baseAmount × USD汇率。
     */
    private BigDecimal displayAmount;

    /**
     * 货币代码（ISO 4217），全系统统一 EUR（欧元）。
     * Jeepay 创建支付单时传入此字段。
     */
    private String currency = "EUR";
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    private LocalDateTime updatedAt;

}
