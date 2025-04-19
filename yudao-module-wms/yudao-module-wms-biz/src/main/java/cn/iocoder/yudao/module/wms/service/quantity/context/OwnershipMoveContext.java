package cn.iocoder.yudao.module.wms.service.quantity.context;

import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.WmsStockOwnershipMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import lombok.Data;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 8:57
 * @description:
 */
@Data
public class OwnershipMoveContext {

    private WmsStockOwnershipMoveDO ownershipMoveDO;
    private List<WmsStockOwnershipMoveItemDO> ownershipMoveItemDOS;

}
