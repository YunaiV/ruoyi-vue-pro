package cn.iocoder.yudao.module.tms.dal.dataobject.fee;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;

/**
 * 出运订单费用明细 DO
 *
 * @author wdy
 */
@TableName("tms_fee")
@KeySequence("tms_fee_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsFeeDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 原单类型;出运订单、调拨单
     */
    private Integer upstreamType;
    /**
     * 原单ID;出运订单ID、调拨单ID
     */
    private Long upstreamId;
    /**
     * 费用类型（如运输费、关税）;字典
     */
    private Integer costType;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * 币种;名称（如 USD、CNY） 字典
     */
    private Integer currencyType;
    /**
     * 备注
     */
    private String remark;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer revision;

}