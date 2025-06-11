package cn.iocoder.yudao.module.tms.dal.dataobject.first.mile;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 头程单 DO
 *
 * @author wdy
 */
@TableName("tms_first_mile")
@KeySequence("tms_first_mile_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsFirstMileDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer revision;
    /**
     * 编码
     */
    private String code;
    /**
     * 单据日期
     */
    private LocalDateTime billTime;
    /**
     * 物流商ID
     */
    private String carrierId;
    /**
     * 结算日期
     */
    private LocalDateTime settlementDate;
    /**
     * 应付款余额
     */
    private BigDecimal balance;
    /**
     * 审核人
     */
    private Long auditorId;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    /**
     * 审核状态
     */
    private Integer auditStatus;
    /**
     * 审核意见
     */
    private String auditAdvice;
    /**
     * 目的仓ID
     */
    private Long toWarehouseId;
    /**
     * 柜型（字典）
     */
    private Integer cabinetType;
    /**
     * 装柜日期
     */
    private LocalDateTime packTime;
    /**
     * 预计到货日期
     */
    private LocalDateTime arrivePlanTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 发货状态
     */
    private Integer outboundStatus;
    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;
    /**
     * 入库状态
     */
    private Integer inboundStatus;
    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;
    /**
     * 出口公司ID
     */
    private Long exportCompanyId;
    /**
     * 中转公司ID
     */
    private Long transitCompanyId;
}