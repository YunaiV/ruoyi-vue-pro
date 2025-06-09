package cn.iocoder.yudao.module.wms.service.stock.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseQueryDTO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 仓库库存 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockWarehouseService {

    /**
     * 创建仓库库存
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockWarehouseDO createStockWarehouse(@Valid WmsStockWarehouseSaveReqVO createReqVO);

    /**
     * 更新仓库库信息(在制数量)
     *
     * @param updateReqVO 更新信息
     */
    void updateStockWarehouse(@Valid WmsStockWarehouseSaveReqVO updateReqVO);

    /**
     * 删除仓库库存
     *
     * @param id 编号
     */
    void deleteStockWarehouse(Long id);

    /**
     * 获得仓库库存
     *
     * @param id 编号
     * @return 仓库库存
     */
    WmsStockWarehouseDO getStockWarehouse(Long id);

    /**
     * 获得仓库库存分页
     *
     * @param pageReqVO 分页查询
     * @return 仓库库存分页
     */
    PageResult<WmsStockWarehouseDO> getStockWarehousePage(WmsStockWarehousePageReqVO pageReqVO);

    WmsStockWarehouseDO getStockWarehouse(Long warehouseId, Long productId, boolean createNew);

    void insertOrUpdate(WmsStockWarehouseDO stockWarehouseDO);

    void assembleProducts(List<WmsStockWarehouseRespVO> list);

    void assembleWarehouse(List<WmsStockWarehouseRespVO> list);

    String getWarehouseProductKey(Long warehouseId, Long productId);

    void assembleStockBin(List<WmsStockWarehouseRespVO> list);

    List<WmsStockWarehouseDO> selectByWarehouse(@NotNull(message = "仓库ID不能为空") Long warehouseId);

    List<WmsStockWarehouseDO> getByProductIds(Long warehouseId, List<Long> list);

    List<WmsStockWarehouseDO> selectStockWarehouse(List<WmsWarehouseProductVO> wmsWarehouseProductVOList);

    PageResult<WmsStockWarehouseProductRespVO> getStockGroupedWarehousePage(@Valid WmsStockWarehousePageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockWarehouseDO
     */
    List<WmsStockWarehouseDO> selectByIds(List<Long> idList);

    /**
     * 查询可售库存
     *
     * @param wmsWarehouseQueryDTO 入参
     * @return 可售库存列表
     */
    Map<Long, List<WmsStockWarehouseDO>> selectSellableQty(WmsWarehouseQueryDTO wmsWarehouseQueryDTO);

    List<WmsStockWarehouseDO> selectSellableQtyList(WmsWarehouseQueryDTO wmsWarehouseQueryDTO);
}
