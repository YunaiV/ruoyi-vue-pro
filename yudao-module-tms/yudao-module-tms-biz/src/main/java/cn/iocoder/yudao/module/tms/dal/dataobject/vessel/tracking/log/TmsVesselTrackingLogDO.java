package cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.log;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出运轨迹日志表（记录多次事件节点） DO
 *
 * @author wdy
 */
@TableName("tms_vessel_tracking_log")
@KeySequence("tms_vessel_tracking_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsVesselTrackingLogDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 关联跟踪主表ID
     */
    private Long trackingId;
    /**
     * 乐观锁
     */
    private Integer revision;
    /**
     * 事件类型;（如 ETA、ETD、ATA、ATD、PICKUP、RETURN）
     */
    private String eventType;
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
    /**
     * 地点/港口名称（可选）
     */
    private String location;
    /**
     * 备注（如API原始描述）
     */
    private String remark;
    /**
     * 外部单据类型;（如 Cainiao, Flexport）
     */
    private String externalType;
    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;

}