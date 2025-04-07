package cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库存盘点结果 DO
 *
 * @author 李方捷
 */
@TableName("wms_inventory_result")
@KeySequence("wms_inventory_result_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInventoryResultDO extends BaseDO {

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
     * 创建者备注
     */
    private String creatorComment;

}