package cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 逻辑库存移动 DO
 * @author 李方捷
 * @table-fields : no,execute_status,id,warehouse_id
 */
@TableName("wms_stock_logic_move")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_logic_move_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockLogicMoveDO extends BaseDO {

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
     * 执行状态
     */
    private Integer executeStatus;

    /**
     * 仓库ID
     */
    private Long warehouseId;
}
