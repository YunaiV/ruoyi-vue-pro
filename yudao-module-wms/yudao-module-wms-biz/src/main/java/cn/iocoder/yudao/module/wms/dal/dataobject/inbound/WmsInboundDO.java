package cn.iocoder.yudao.module.wms.dal.dataobject.inbound;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 入库单 DO
 * @author 李方捷
 * @table-fields : no,inbound_status,company_id,inbound_time,arrival_actual_time,audit_status,creator_comment,source_bill_id,trace_no,type,init_age,shipping_method,source_bill_no,source_bill_type,id,dept_id,arrival_plan_time,warehouse_id
 */
@TableName("wms_inbound")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_inbound_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 单据号
     */
    private String no;

    /**
     * 入库单类型
     */
    private Integer type;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 来源单据ID
     */
    private Long sourceBillId;

    /**
     * 来源单据号
     */
    private String sourceBillNo;

    /**
     * 来源单据类型
     */
    private Integer sourceBillType;

    /**
     * 跟踪号
     */
    private String traceNo;

    /**
     * 运输方式，1-海运；2-火车；3-空运；4、集卡
     */
    private Integer shippingMethod;

    /**
     * 特别说明，创建方专用
     */
    private String creatorComment;

    /**
     * 初始库龄
     */
    private Integer initAge;

    /**
     * 入库单类型 ; InboundStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过
     */
    private Integer auditStatus;

    /**
     * 入库状态
     */
    private Integer inboundStatus;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 实际到货时间
     */
    private LocalDateTime arrivalActualTime;

    /**
     * 预计到货时间
     */
    private LocalDateTime arrivalPlanTime;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;
}
