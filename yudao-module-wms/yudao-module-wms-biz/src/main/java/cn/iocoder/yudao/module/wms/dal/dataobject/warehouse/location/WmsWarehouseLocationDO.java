package cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.location;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库位 DO
 * @author 李方捷
 * @table-fields : code,picking_order,name,id,area_id,warehouse_id,status
 */
@TableName("wms_warehouse_location")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_warehouse_location_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsWarehouseLocationDO extends BaseDO {

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
    private Long areaId;

    /**
     * 拣货顺序
     */
    private Integer pickingOrder;

    /**
     * 状态：0-不可用；1-可用
     */
    private Integer status;
}
