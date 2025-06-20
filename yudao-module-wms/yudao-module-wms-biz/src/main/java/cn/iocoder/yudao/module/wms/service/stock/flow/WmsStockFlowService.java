package cn.iocoder.yudao.module.wms.service.stock.flow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import jakarta.validation.Valid;

import java.util.List;

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

//    /**
//     * 创建仓库库存变化流水
//     */
//    void createForStockWarehouse(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockWarehouseDO stockWarehouseDO, Integer quantity, Long reasonId, Long reasonItemId);

//    /**
//     * 创建逻辑库存变化流水
//     */
//    void createForStockLogic(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockLogicDO stockLogicDO, Integer quantity, Long reasonId, Long reasonItemId);

//    /**
//     * 创建仓位库存变化流水
//     */
//    void createForStockBin(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockBinDO stockBinDO, Integer quantity, Long reasonId, Long reasonItemId, Long inboundItemFlowId);

    List<WmsStockFlowDO> selectStockFlow(Long stockType, Long stockId);

    void assembleProducts(List<WmsStockFlowRespVO> list);

    void assembleWarehouse(List<WmsStockFlowRespVO> list);

    void assembleBin(List<WmsStockFlowRespVO> list);

    void assembleCompanyAndDept(List<WmsStockFlowRespVO> list);

    void assembleInbound(List<WmsStockFlowRespVO> list);

    void assembleOutbound(List<WmsStockFlowRespVO> list);

    void assemblePickup(List<WmsStockFlowRespVO> list);

    void assembleStockWarehouse(List<WmsStockFlowRespVO> list);

    /**
     * 按 ID 集合查询 WmsStockFlowDO
     */
    List<WmsStockFlowDO> selectByIds(List<Long> idList);

    void assembleInboundItemFlow(List<WmsStockFlowRespVO> list);

    void assembleStockCheck(List<WmsStockFlowRespVO> list);

    void assembleBinMove(List<WmsStockFlowRespVO> list);

    void assembleLogicMove(List<WmsStockFlowRespVO> list);

    void assembleBinStock(List<WmsStockFlowRespVO> list);

    void assembleExchange(List<WmsStockFlowRespVO> list);

    /**
     * 创建仓库库存变化流水
     * <p>
     * * @param reason       操作类型
     * * @param direction    方向
     * * @param productId    产品id
     * * @param stockBinDO   库位库存
     * * @param quantity     数量
     * * @param reasonId     单据编号
     * * @param reasonItemId 明细行编号
     * * @param beforeQty    变更前数量
     * * @param afterQty     变更后数量
     * * @param inboundId    入库单编号
     */
    void createForStockWarehouse(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockWarehouseDO stockWarehouseDO, Integer quantity, Long reasonId, Long reasonItemId, Integer beforeQty, Integer afterQty, Long inboundId);
    /**
     * 创建库存流水 - 库位库存
     *
     * @param reason       操作类型
     * @param direction    方向
     * @param productId    产品id
     * @param stockBinDO   库位库存
     * @param quantity     数量
     * @param reasonId     单据编号
     * @param reasonItemId 明细行编号
     * @param binId        库存货位编号
     * @param beforeQty    变更前数量
     * @param afterQty     变更后数量
     * @param inboundId    入库单编号
     */
    void createForStockBin(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockBinDO stockBinDO, Integer quantity, Long reasonId, Long reasonItemId, Long binId, Integer beforeQty, Integer afterQty, Long inboundId);

    /**
     * 创建逻辑库存变化流水
     *
     * @param reason       操作类型
     * @param direction    方向
     * @param productId    产品id
     * @param stockLogicDO 逻辑库存
     * @param quantity     数量
     * @param reasonId     单据编号
     * @param reasonItemId 明细行编号
     * @param beforeQty    变更前数量
     * @param afterQty     变更后数量
     * @param inboundId    入库单编号
     */
    void createForStockLogic(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockLogicDO stockLogicDO, Integer quantity, Long reasonId, Long reasonItemId, Integer beforeQty, Integer afterQty, Long inboundId);

}
