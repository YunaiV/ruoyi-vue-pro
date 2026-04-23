package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Deepay 复盘指标表。
 *
 * <p>对应数据库表 {@code deepay_metrics}。
 * 每笔成交后写入一条记录，用于后续数据复盘分析。</p>
 */
@TableName("deepay_metrics")
@Data
public class DeepayMetricsDO {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联商品链码 */
    private String chainCode;

    /** 成交价格（元） */
    private BigDecimal price;

    /**
     * 是否已售。
     * <ul>
     *   <li>{@code 0} —— 未售（订单创建时写入）</li>
     *   <li>{@code 1} —— 已售（支付回调后更新）</li>
     * </ul>
     */
    private Integer sold;

    /** 记录时间 */
    private LocalDateTime createdAt;

}
