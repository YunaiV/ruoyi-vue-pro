package cn.iocoder.yudao.module.wms.service.stock.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import jakarta.validation.Valid;

import java.util.List;

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
     * 更新仓库库存
     *
     * @param updateReqVO 更新信息
     */
    WmsStockWarehouseDO updateStockWarehouse(@Valid WmsStockWarehouseSaveReqVO updateReqVO);

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

    WmsStockWarehouseDO getStockWarehouse(Long warehouseId, Long productId);

    WmsStockWarehouseDO getByWarehouseIdAndProductId(Long warehouseId, Long productId);

    void insertOrUpdate(WmsStockWarehouseDO stockWarehouseDO);

    void assembleProducts(List<WmsStockWarehouseRespVO> list);

    void assembleWarehouse(List<WmsStockWarehouseRespVO> list);

    String getWarehouseProductKey(Long warehouseId, Long productId);

    void assembleStockBin(List<WmsStockWarehouseRespVO> list);
}
