package cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库位移动 DO
 * @author 李方捷
 * @table-fields : no,execute_status,id,warehouse_id
 */
@TableName("wms_stock_bin_move")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_bin_move_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockBinMoveDO extends BaseDO {

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
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 执行状态
     */
    private Integer executeStatus;
}
