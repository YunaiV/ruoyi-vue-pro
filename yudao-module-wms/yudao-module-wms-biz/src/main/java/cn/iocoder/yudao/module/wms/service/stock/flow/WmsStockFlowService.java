package cn.iocoder.yudao.module.wms.service.stock.flow;

import java.util.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 库存流水 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockFlowService {

    /**
     * 创建库存流水
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockFlowDO createStockFlow(@Valid WmsStockFlowSaveReqVO createReqVO);

    /**
     * 更新库存流水
     *
     * @param updateReqVO 更新信息
     */
    WmsStockFlowDO updateStockFlow(@Valid WmsStockFlowSaveReqVO updateReqVO);

    /**
     * 删除库存流水
     *
     * @param id 编号
     */
    void deleteStockFlow(Long id);

    /**
     * 获得库存流水
     *
     * @param id 编号
     * @return 库存流水
     */
    WmsStockFlowDO getStockFlow(Long id);

    /**
     * 获得库存流水分页
     *
     * @param pageReqVO 分页查询
     * @return 库存流水分页
     */
    PageResult<WmsStockFlowDO> getStockFlowPage(WmsStockFlowPageReqVO pageReqVO);

    /**
     * 查找最后一个流水
     */
    WmsStockFlowDO getLastFlow(Long warehouseId, Integer stockType, Long stockId);

    /**
     * 为入库创建流水
     * 此方法必须包含在 WmsStockWarehouseServiceImpl.inboundSingleItemTransactional 方法中
     */
    void createForInbound(Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId, WmsStockWarehouseDO stockWarehouseDO);

    /**
     * 为入库创建流水
     * 此方法必须包含在 WmsStockOwnershipServiceImpl.inboundSingleItem 方法中
     */
    void createForInbound(Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId, WmsStockOwnershipDO stockOwnershipDO);

    List<WmsStockFlowDO> selectStockFlow(Long stockType, Long stockId);
}
