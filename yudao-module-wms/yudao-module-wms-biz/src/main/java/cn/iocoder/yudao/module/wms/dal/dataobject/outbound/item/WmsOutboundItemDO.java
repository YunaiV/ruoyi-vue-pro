package cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 出库单详情 DO
 * @author 李方捷
 * @table-fields : company_id,outbound_status,actual_qty,bin_id,plan_qty,product_id,upstream_id,remark,id,dept_id,outbound_id
 */
@TableName("wms_outbound_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_outbound_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsOutboundItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 入库单ID
     */
    private Long outboundId;

    /**
     * 标准产品ID
     */
    private Long productId;

    /**
     * 出库状态 ; OutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库
     */
    private Integer outboundStatus;

    /**
     * 出库库位ID
     */
    private Long binId;

    /**
     * 实际出库量
     */
    private Integer actualQty;

    /**
     * 计划出库量
     */
    private Integer planQty;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源明细行ID
     */
    private Long upstreamId;
}
