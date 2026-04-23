package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FX 汇率表 deepay_fx_rate。
 *
 * <p>以 EUR 为基准（rate = 1单位EUR可换多少目标货币）：</p>
 * <pre>
 * currency | rate   | 含义
 * ─────────────────────────────
 * EUR      | 1.0000 | 基准（不变）
 * USD      | 1.0800 | 1 EUR = 1.08 USD
 * CNY      | 7.8000 | 1 EUR = 7.80 CNY
 * GBP      | 0.8600 | 1 EUR = 0.86 GBP
 * AED      | 3.9700 | 1 EUR = 3.97 AED
 * </pre>
 */
@TableName("deepay_fx_rate")
@Data
public class DeepayFxRateDO {

    /** 货币代码（ISO 4217），同时作为主键 */
    @TableId
    private String currency;

    /** 相对 EUR 的汇率（6位精度） */
    private BigDecimal rate;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}
