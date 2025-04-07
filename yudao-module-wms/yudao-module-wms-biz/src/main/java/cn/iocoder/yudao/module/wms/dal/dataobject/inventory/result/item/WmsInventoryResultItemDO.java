package cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.item;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库存盘点结果详情 DO
 *
 * @author 李方捷
 */
@TableName("wms_inventory_result_item")
@KeySequence("wms_inventory_result_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInventoryResultItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 盘点结果单ID
     */
    private Long resultId;
    /**
     * 产品ID
     */
    private String productId;
    /**
     * 预期库存
     */
    private Integer expectedQty;
    /**
     * 实际库存
     */
    private Integer actualQty;

}