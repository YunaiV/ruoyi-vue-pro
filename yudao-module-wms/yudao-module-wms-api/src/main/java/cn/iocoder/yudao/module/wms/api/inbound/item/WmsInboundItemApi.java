package cn.iocoder.yudao.module.wms.api.inbound.item;

import cn.iocoder.yudao.module.wms.api.inbound.item.dto.WmsInboundItemBinDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 入库单详情 API 接口
 *
 * @author 李方捷
 */
public interface WmsInboundItemApi {

    /**
     * 仓库内产品的批次库存查询，先进先出排序
     *
     * @param warehouseId 仓库编号
     * @param productIds  产品编号集合
     * @param olderFirst  是否按先进先出排序
     * @return 产品编号 -> 批次库存信息列表的映射
     */
    Map<Long, List<WmsInboundItemBinDTO>> getInboundItemBinMap(Long warehouseId, Set<Long> productIds, boolean olderFirst);
} 