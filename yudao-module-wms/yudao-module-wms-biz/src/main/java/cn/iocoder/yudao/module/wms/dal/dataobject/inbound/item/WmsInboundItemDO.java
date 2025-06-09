package cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 入库单详情 DO
 * @author 李方捷
 * @table-fields : outbound_available_qty,inbound_status,company_id,plan_qty,shelve_closed_qty,upstream_id,remark,inbound_dept_id,latest_flow_id,inbound_id,inbound_company_id,actual_qty,product_id,id,dept_id
 */
@TableName("wms_inbound_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_inbound_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 入库单ID
     */
    private Long inboundId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 入库状态 : 0-未入库 , 1-部分入库 , 2-已入库
     */
    private Integer inboundStatus;

    /**
     * 实际入库量
     */
    private Integer actualQty;

    /**
     * 批次剩余库存，出库后的剩余库存量
     */
    private Integer outboundAvailableQty;

    /**
     * 计划入库量
     */
    private Integer planQty;

    /**
     * 已上架量
     */
    private Integer shelveClosedQty;

    /**
     * 最新的流水ID
     */
    private Long latestFlowId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源明细行ID
     */
    private Long upstreamId;

}
