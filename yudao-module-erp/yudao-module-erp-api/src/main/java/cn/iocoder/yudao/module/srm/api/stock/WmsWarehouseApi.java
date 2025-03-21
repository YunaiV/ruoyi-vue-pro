package cn.iocoder.yudao.module.srm.api.stock;

import cn.iocoder.yudao.module.srm.api.stock.dto.ErpWarehouseDTO;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface WmsWarehouseApi {


    Map<Long, ErpWarehouseDTO> getWarehouseMap(Collection<Long> ids);

    void validWarehouseList(Set<Long> longs);
}
