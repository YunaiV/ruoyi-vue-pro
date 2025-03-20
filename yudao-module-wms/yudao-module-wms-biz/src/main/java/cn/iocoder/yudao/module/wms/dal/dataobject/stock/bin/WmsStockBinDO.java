package cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 仓位库存 DO
 * @author 李方捷
 * @table-fields : available_quantity,outbound_pending_quantity,bin_id,product_id,sellable_quantity,id,warehouse_id
 */
@TableName("wms_stock_bin")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_bin_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockBinDO extends BaseDO {

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
     * 库位ID
     */
    private Long binId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 可用量，在库的良品数量
     */
    private Integer availableQuantity;

    /**
     * 可售量，未被单据占用的良品数量
     */
    private Integer sellableQuantity;

    /**
     * 待出库量
     */
    private Integer outboundPendingQuantity;
}
