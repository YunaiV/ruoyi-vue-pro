package cn.iocoder.yudao.module.srm.api.product;


import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductUnitDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

public interface ErpProductUnitApi {


    /**
     * 获得产品单位 Map
     *
     * @param ids 编号数组
     * @return 产品单位 Map
     */
    default Map<Long, ErpProductUnitDTO> getProductUnitMap(Collection<Long> ids) {
        return convertMap(getProductUnitList(ids), ErpProductUnitDTO::getId);
    }


    /**
     * 获得产品单位列表
     *
     * @param ids 编号数组
     * @return 产品单位列表
     */
    List<ErpProductUnitDTO> getProductUnitList(Collection<Long> ids);

}
