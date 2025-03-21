package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMovePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMoveSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockMoveItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * ERP 库存调拨单 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpStockMoveService {

    /**
     * 创建库存调拨单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockMove(@Valid ErpStockMoveSaveReqVO createReqVO);

    /**
     * 更新库存调拨单
     *
     * @param updateReqVO 更新信息
     */
    void updateStockMove(@Valid ErpStockMoveSaveReqVO updateReqVO);

    /**
     * 更新库存调拨单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateStockMoveStatus(Long id, Integer status);

    /**
     * 删除库存调拨单
     *
     * @param ids 编号数组
     */
    void deleteStockMove(List<Long> ids);

    /**
     * 获得库存调拨单
     *
     * @param id 编号
     * @return 库存调拨单
     */
    ErpStockMoveDO getStockMove(Long id);

    /**
     * 获得库存调拨单分页
     *
     * @param pageReqVO 分页查询
     * @return 库存调拨单分页
     */
    PageResult<ErpStockMoveDO> getStockMovePage(ErpStockMovePageReqVO pageReqVO);

    // ==================== 调拨项 ====================

    /**
     * 获得库存调拨单项列表
     *
     * @param moveId 调拨编号
     * @return 库存调拨单项列表
     */
    List<ErpStockMoveItemDO> getStockMoveItemListByMoveId(Long moveId);

    /**
     * 获得库存调拨单项 List
     *
     * @param moveIds 调拨编号数组
     * @return 库存调拨单项 List
     */
    List<ErpStockMoveItemDO> getStockMoveItemListByMoveIds(Collection<Long> moveIds);

}