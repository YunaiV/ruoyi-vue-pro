package cn.iocoder.yudao.module.wms.dal.dataobject.inbound;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 入库单 DO
 * @author 李方捷
 * @table-fields : code,inbound_status,company_id,inbound_time,arrival_actual_time,remark,audit_status,trace_no,type,upstream_type,init_age,upstream_id,shipping_method,id,upstream_code,dept_id,arrival_plan_time,shelving_status,warehouse_id
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
     * 类型
     */
    private Integer type;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 跟踪号
     */
    private String traceNo;

    /**
     * 运输方式: 1-海运；2-火车；3-空运；4、集卡
     */
    private Integer shippingMethod;

    /**
     * 初始库龄
     */
    private Integer initAge;

    /**
     * 审批状态: 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过
     */
    private Integer auditStatus;

    /**
     * 入库状态
     */
    private Integer inboundStatus;

    /**
     * 财务公司ID
     */
    private Long companyId;

    /**
     * 实际到货时间
     */
    private LocalDateTime arrivalActualTime;

    /**
     * 计划到货时间
     */
    private LocalDateTime arrivalPlanTime;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

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
     * 来源单据类型: 0-入库单 , 1-出库单 , 2-盘点单
     */
    private Integer upstreamType;

    /**
     * 上架状态
     */
    private Integer shelveStatus;

    /**
     * 特别说明，创建方专用
     */
    private String remark;
}
