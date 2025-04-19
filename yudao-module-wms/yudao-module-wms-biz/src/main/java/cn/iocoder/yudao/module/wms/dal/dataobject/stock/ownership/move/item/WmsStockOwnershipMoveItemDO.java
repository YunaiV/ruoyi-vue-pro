package cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 所有者库存移动详情 DO
 * @author 李方捷
 * @table-fields : ownership_move_id,from_dept_id,product_id,qty,from_company_id,id,to_company_id,to_dept_id
 */
@TableName("wms_stock_ownership_move_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_ownership_move_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockOwnershipMoveItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 所有者移动表ID
     */
    private Long ownershipMoveId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 调出财务公司ID
     */
    private Long fromCompanyId;

    /**
     * 调出部门ID
     */
    private Long fromDeptId;

    /**
     * 调入财务公司ID
     */
    private Long toCompanyId;

    /**
     * 调入部门ID
     */
    private Long toDeptId;

    /**
     * 移动数量
     */
    private Integer qty;
}
