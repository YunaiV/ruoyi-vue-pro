package cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;

/**
 * 头程申请单 DO
 *
 * @author wdy
 */
@TableName("tms_first_mile_request")
@KeySequence("tms_first_mile_request_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsFirstMileRequestDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 乐观锁
     */
    @Version
    private Integer revision;
    /**
     * 单据编号
     */
    private String code;
    /**
     * 申请人ID
     */
    private Long requesterId;
    /**
     * 申请部门ID
     */
    private Long requestDeptId;
    /**
     * 目的仓ID
     */
    private Long toWarehouseId;
    /**
     * 审核状态
     */
    private Integer auditStatus;
    /**
     * 审核意见
     */
    private String auditAdvice;
    /**
     * 订购状态
     */
    private Integer orderStatus;
    /**
     * 关闭状态
     */
    private Integer offStatus;
    /**
     * 总重量（kg）
     */
    private BigDecimal totalWeight;
    /**
     * 总体积（m³）
     */
    private BigDecimal totalVolume;
    /**
     * 备注
     */
    private String remark;
}