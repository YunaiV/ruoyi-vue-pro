package cn.iocoder.yudao.module.wms.api.inbound.item;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.api.inbound.item.dto.WmsInboundItemBinDTO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemBinQueryDO;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 入库单详情 API 实现类
 *
 * @author wdy
 */
@Service
public class WmsInboundItemApiImpl implements WmsInboundItemApi {

    @Autowired
    private WmsInboundItemService inboundItemService;

    @Override
    public Map<Long, List<WmsInboundItemBinDTO>> getInboundItemBinMap(Long warehouseId, Set<Long> productIds, boolean olderFirst) {
        // 1. 获取库存信息
        Map<Long, List<WmsInboundItemBinQueryDO>> stockMap = inboundItemService.selectInboundItemBinMap(warehouseId, productIds, olderFirst);

        // 2. 转换为DTO
        return stockMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList())
                ));
    }

    private WmsInboundItemBinDTO convertToDTO(WmsInboundItemBinQueryDO queryDO) {
        return BeanUtils.toBean(queryDO, WmsInboundItemBinDTO.class);
    }
} 