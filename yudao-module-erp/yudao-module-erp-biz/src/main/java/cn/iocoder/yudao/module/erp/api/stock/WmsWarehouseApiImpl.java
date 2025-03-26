package cn.iocoder.yudao.module.erp.api.stock;

import cn.iocoder.yudao.module.erp.api.stock.dto.ErpWarehouseDTO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WmsWarehouseApiImpl implements WmsWarehouseApi {
    @Autowired
    ErpWarehouseService erpWarehouseService;

    @Override
    public Map<Long, ErpWarehouseDTO> getWarehouseMap(Collection<Long> ids) {
        Map<Long, ErpWarehouseDO> warehouseMap = erpWarehouseService.getWarehouseMap(ids);
        return warehouseMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            ErpWarehouseDTO erpWarehouseDTO = new ErpWarehouseDTO();
            BeanUtils.copyProperties(entry.getValue(), erpWarehouseDTO);
            return erpWarehouseDTO;
        }));
    }

    @Override
    public void validWarehouseList(Set<Long> longs) {
        //TODO 批量代优化
        for (Long aLong : longs) {
            erpWarehouseService.validWarehouse(aLong);
        }
    }
}
