package cn.iocoder.yudao.module.fms.dal.dataobject.closing;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FMS 结账凭证 DO
 *
 * @author 芋道源码
 */
@TableName("fms_closing_voucher")
@KeySequence("fms_closing_voucher_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsClosingVoucherDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 结账方案编号
     *
     * 关联 {@link FmsClosingSchemeDO#getId()}
     */
    private Long closingId;
    /**
     * 凭证编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO#getId()}
     */
    private Long voucherId;
    /**
     * 凭证日期
     */
    private LocalDateTime voucherTime;
    /**
     * 结转金额
     */
    private BigDecimal amount;
    /**
     * 是否已结账
     */
    private Boolean closed;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;

}
