package cn.iocoder.yudao.module.wms.service.stock.bin;

import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.enums.stock.StockReason;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 仓位库存 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockBinService {

    /**
     * 创建仓位库存
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockBinDO createStockBin(@Valid WmsStockBinSaveReqVO createReqVO);

    /**
     * 更新仓位库存
     *
     * @param updateReqVO 更新信息
     */
    WmsStockBinDO updateStockBin(@Valid WmsStockBinSaveReqVO updateReqVO);

    /**
     * 删除仓位库存
     *
     * @param id 编号
     */
    void deleteStockBin(Long id);

    /**
     * 获得仓位库存
     *
     * @param id 编号
     * @return 仓位库存
     */
    WmsStockBinDO getStockBin(Long id);

    /**
     * 获得仓位库存分页
     *
     * @param pageReqVO 分页查询
     * @return 仓位库存分页
     */
    PageResult<WmsStockBinDO> getStockBinPage(WmsStockBinPageReqVO pageReqVO);

    List<WmsStockBinDO> selectStockBin(Long warehouseId,Long binId, Long productId);

    default List<WmsStockBinDO> selectStockBin(Long warehouseId, Long productId){
        return selectStockBin(warehouseId, null, productId);
    }

    WmsStockBinDO getStockBin(Long binId, Long productId);

    Map<Long, Map<Long, WmsStockBinDO>> getStockBinMap(Collection<Long> binIds, Collection<Long> productIds);

    void insertOrUpdate(WmsStockBinDO stockBinDO);
}
