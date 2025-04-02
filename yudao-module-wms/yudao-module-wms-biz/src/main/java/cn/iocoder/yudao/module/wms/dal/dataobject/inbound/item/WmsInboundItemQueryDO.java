package cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 入库单详情 QueryDO
 * @author 李方捷
 */
@TableName("wms_inbound_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_inbound_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundItemQueryDO extends WmsInboundItemDO {

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 仓位ID
     */
    private Long binId;
}
