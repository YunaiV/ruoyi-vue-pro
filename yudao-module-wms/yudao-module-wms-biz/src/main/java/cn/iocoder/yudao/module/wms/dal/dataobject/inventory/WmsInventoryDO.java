package cn.iocoder.yudao.module.wms.dal.dataobject.inventory;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 盘点 DO
 * @author 李方捷
 * @table-fields : no,creator_notes,id,audit_status,warehouse_id
 */
@TableName("wms_inventory")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_inventory_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInventoryDO extends BaseDO {

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
     * 出库单审批状态 ; WmsInventoryAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过
     */
    private Integer auditStatus;

    /**
     * 创建者备注
     */
    private String creatorNotes;
}
