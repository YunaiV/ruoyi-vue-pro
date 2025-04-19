package cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库位 DO
 * @author 李方捷
 * @table-fields : zone_id,code,picking_order,name,id,status,warehouse_id
 */
@TableName("wms_warehouse_bin")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_warehouse_bin_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsWarehouseBinDO extends BaseDO {

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
     * 库区ID
     */
    private Long zoneId;

    /**
     * 拣货顺序
     */
    private Integer pickingOrder;

    /**
     * 状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用
     */
    private Integer status;
}
