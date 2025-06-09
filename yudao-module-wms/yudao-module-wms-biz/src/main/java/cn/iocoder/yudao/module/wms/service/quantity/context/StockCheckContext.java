package cn.iocoder.yudao.module.wms.service.quantity.context;

import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.bin.WmsStockCheckBinDO;
import lombok.Data;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 8:57
 * @description:
 */
@Data
public class StockCheckContext {

    private WmsStockCheckDO stockCheckDO;
    private List<WmsStockCheckBinDO> wmsStockCheckBinDOList;

}
