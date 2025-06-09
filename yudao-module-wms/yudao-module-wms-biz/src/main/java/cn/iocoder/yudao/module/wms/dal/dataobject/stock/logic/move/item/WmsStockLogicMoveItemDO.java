package cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 逻辑库存移动详情 DO
 * @author 李方捷
 * @table-fields : logic_move_id,from_dept_id,product_id,qty,from_company_id,remark,id,to_company_id,to_dept_id
 */
@TableName("wms_stock_logic_move_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_logic_move_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockLogicMoveItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 逻辑移动表ID
     */
    private Long logicMoveId;

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

    /**
     * 备注
     */
    private String remark;
}
