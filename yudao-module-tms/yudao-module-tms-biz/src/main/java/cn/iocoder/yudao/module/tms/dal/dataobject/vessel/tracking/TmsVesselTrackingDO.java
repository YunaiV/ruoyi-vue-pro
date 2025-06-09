package cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 出运跟踪信息表（由外部API更新，船） DO
 *
 * @author wdy
 */
@TableName("tms_vessel_tracking")
@KeySequence("tms_vessel_tracking_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsVesselTrackingDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 上游单据类型;调拨单、头程单、退货单
     */
    private Integer upstreamType;
    /**
     * 上游业务单ID，如调拨单ID
     */
    private Long upstreamId;
    /**
     * 预计到港时间（ETA）
     */
    private LocalDateTime arriveEstimateTime;
    /**
     * 预计离港时间（ETD）
     */
    private LocalDateTime departEstimateTime;
    /**
     * 实际到港时间（ATA）
     */
    private LocalDateTime arriveActualTime;
    /**
     * 实际离港时间（ATD）
     */
    private LocalDateTime departActualTime;
    /**
     * 提柜时间
     */
    private LocalDateTime pickupTime;
    /**
     * 还柜时间
     */
    private LocalDateTime returnTime;
    /**
     * 数据来源（API渠道标识）
     */
    private String apiSource;
    /**
     * 最近同步时间
     */
    private LocalDateTime lastSyncTime;
    /**
     * 乐观锁
     */
    @Version
    private Integer revision;
    /**
     * 中转港
     */
    private Long transitPort;
    /**
     * 目的港
     */
    private Long toPort;
    /**
     * 装运港
     */
    private Long fromPort;
    /**
     * 船公司（供应商id）
     */
    private Long carrierCompanyId;
    /**
     * 船名
     */
    private String vessel;
    /**
     * 航次
     */
    private String voyage;
    /**
     * 货代公司(供应商ID)
     */
    private Long forwarderCompanyId;
    /**
     * 箱号
     */
    private String containerNo;

    /**
     * 跟踪状态(字典)
     */
    private Integer trackingStatus;
    /**
     * 预计送仓时间
     */
    private LocalDateTime deliveryEstimateTime;
    /**
     * 实际送仓时间
     */
    private LocalDateTime deliveryActualTime;
    /**
     * 提单号
     */
    private String ladingNo;
}