package cn.iocoder.yudao.module.wms.service.stock.warehouse;

import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

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

    /**
     * 入库
     */
    void inbound(WmsInboundRespVO inboundRespVO);

    WmsStockWarehouseDO getStockWarehouse(Long warehouseId, Long productId);
}
