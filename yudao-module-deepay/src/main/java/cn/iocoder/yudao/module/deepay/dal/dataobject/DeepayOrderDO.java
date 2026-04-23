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

    /** 支付金额（元） */
    private BigDecimal amount;

    /**
     * 订单状态。
     * <ul>
     *   <li>{@code INIT} —— 已创建，待支付</li>
     *   <li>{@code PAID} —— 已支付</li>
     * </ul>
     */
    private String status;

    /** 记录创建时间 */
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    private LocalDateTime updatedAt;

}
