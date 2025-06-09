package cn.iocoder.yudao.module.wms.service.quantity.context;

import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import lombok.Data;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 8:57
 * @description:
 */
@Data
public class ExchangeContext {

    private WmsExchangeDO exchangeDO;
    private List<WmsExchangeItemDO> exchangeItemDOList;

}
