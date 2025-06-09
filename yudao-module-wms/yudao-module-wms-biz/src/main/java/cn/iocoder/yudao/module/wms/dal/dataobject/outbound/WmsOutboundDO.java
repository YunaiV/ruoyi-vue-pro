package cn.iocoder.yudao.module.wms.dal.dataobject.outbound;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 出库单 DO
 * @author 李方捷
 * @table-fields : code,company_id,remark,audit_status,outbound_time,type,upstream_type,latest_outbound_action_id,outbound_status,upstream_id,id,upstream_code,dept_id,warehouse_id
 */
@TableName("wms_outbound")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_outbound_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsOutboundDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 状态
     */
    private Integer outboundStatus;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;

    /**
     * 计划出库时间
     */
    private LocalDateTime outboundPlanTime;

    /**
     * 出库动作ID，与flow关联
     */
    private Long latestOutboundActionId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 单据号
     */
    private String code;

    /**
     * 来源单据ID
     */
    private Long upstreamId;

    /**
     * 来源单据编码
     */
    private String upstreamCode;

    /**
     * WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单
     */
    private Integer upstreamType;



}
