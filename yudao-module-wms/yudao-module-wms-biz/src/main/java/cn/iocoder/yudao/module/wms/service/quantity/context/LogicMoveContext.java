package cn.iocoder.yudao.module.wms.service.quantity.context;

import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.WmsStockLogicMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import lombok.Data;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 8:57
 * @description:
 */
@Data
public class LogicMoveContext {

    private WmsStockLogicMoveDO logicMoveDO;
    private List<WmsStockLogicMoveItemDO> logicMoveItemDOS;

}
