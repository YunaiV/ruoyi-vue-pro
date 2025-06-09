package cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
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
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundItemBinQueryDO extends WmsInboundItemDO  {

    /**
     * 仓位ID
     */
    private Long binId;

    /**
     * 仓库ID
     */
    private String warehouseId;
    /**
     * 仓位可用库存
     */
    private Integer binAvailableQty;
    /**
     * 仓位可售库存
     */
    private Integer binSellableQty;
    /**
     * 仓位待出库库存
     */
    private Integer binOutboundPendingQty;
    /**
     * 上架单ID
     */
    private String pickupId;
    /**
     * 上架数量
     */
    private Integer pickupQty;
    /**
     * 上架单号
     */
    private String pickupCode;

    /**
     * 入库单号
     */
    private String inboundCode;

    /**
     * 库龄
     */
    private Integer age;


}
