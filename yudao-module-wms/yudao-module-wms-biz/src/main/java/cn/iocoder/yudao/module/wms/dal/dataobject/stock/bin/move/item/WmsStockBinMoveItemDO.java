package cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库位移动详情 DO
 * @author 李方捷
 * @table-fields : bin_move_id,product_id,qty,id,from_bin_id,to_bin_id
 */
@TableName("wms_stock_bin_move_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_bin_move_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockBinMoveItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 库位移动表ID
     */
    private Long binMoveId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 调出库位ID
     */
    private Long fromBinId;

    /**
     * 调入库位ID
     */
    private Long toBinId;

    /**
     * 移动数量
     */
    private Integer qty;
}
