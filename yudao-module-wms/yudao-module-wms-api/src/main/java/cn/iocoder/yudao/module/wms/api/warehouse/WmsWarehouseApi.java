package cn.iocoder.yudao.module.wms.api.warehouse;

import cn.iocoder.yudao.module.wms.api.inbound.dto.WmsStockWarehouseSimpleDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWareHouseUpdateReqDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseQueryDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.vo.WmsWarehouseListReqDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 仓库API 接口
 */
@Validated
public interface WmsWarehouseApi {


    Map<Long, WmsWarehouseDTO> getWarehouseMap(Collection<Long> ids);

    void validWarehouseList(Set<Long> longs);

    /**
     * 根据条件查询仓库列表
     *
     * @param reqDTO 查询条件
     * @return 仓库列表
     */
    List<WmsWarehouseDTO> selectList(@Valid WmsWarehouseListReqDTO reqDTO);

    /**
     * 更新仓库库信息(在制数量)
     *
     * @param updateReqVO 产品ID, 仓库ID，在制数量
     * @return 仓库库存
     */
    Boolean updateStockWarehouse(@Valid WmsWareHouseUpdateReqDTO updateReqVO);

    /**
     * 查询可售库存
     *
     * @param wmsWarehouseQueryDTO 仓库编号
     * @return 可售库存列表
     */
    Map<Long, List<WmsStockWarehouseSimpleDTO>> selectSellableQty(@Valid WmsWarehouseQueryDTO wmsWarehouseQueryDTO);

}
