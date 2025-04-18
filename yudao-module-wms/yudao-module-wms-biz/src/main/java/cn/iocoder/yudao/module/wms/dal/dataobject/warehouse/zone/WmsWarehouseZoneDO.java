package cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.zone;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库区 DO
 * @author 李方捷
 * @table-fields : stock_type,code,name,id,priority,partition_type,status,warehouse_id
 */
@TableName("wms_warehouse_zone")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_warehouse_zone_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsWarehouseZoneDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 代码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 归属的仓库ID
     */
    private Long warehouseId;

    /**
     * 存货类型 ; WarehouseAreaStockType : 1-拣货 , 2-存储
     */
    private Integer stockType;

    /**
     * 分区类型 ; WarehouseAreaPartitionType : 1-标准品 , 2-不良品
     */
    private Integer partitionType;

    /**
     * 状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer priority;
}
