package cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 所有者库存 DO
 * @author 李方捷
 * @table-fields : company_id,outbound_pending_qty,product_id,shelving_pending_qty,available_qty,id,dept_id,warehouse_id
 */
@TableName("wms_stock_ownership")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_ownership_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockOwnershipDO extends BaseDO {

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
     * 产品ID
     */
    private Long productId;

    /**
     * 库存财务主体公司ID
     */
    private Long companyId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 可用库存
     */
    private Integer availableQty;

    /**
     * 待出库库存
     */
    private Integer outboundPendingQty;

    /**
     * 待上架数量，上架是指从拣货区上架到货架
     */
    private Integer shelvingPendingQty;
}
