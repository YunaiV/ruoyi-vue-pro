package cn.iocoder.yudao.module.wms.service.stock.logic.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.WmsStockLogicMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 逻辑库存移动 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockLogicMoveService {

    /**
     * 创建逻辑库存移动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockLogicMoveDO createStockLogicMove(@Valid WmsStockLogicMoveSaveReqVO createReqVO);

    /**
     * 更新逻辑库存移动
     *
     * @param updateReqVO 更新信息
     */
    WmsStockLogicMoveDO updateStockLogicMove(@Valid WmsStockLogicMoveSaveReqVO updateReqVO);

    /**
     * 删除逻辑库存移动
     *
     * @param id 编号
     */
    void deleteStockLogicMove(Long id);

    /**
     * 获得逻辑库存移动
     *
     * @param id 编号
     * @return 逻辑库存移动
     */
    WmsStockLogicMoveDO getStockLogicMove(Long id);

    /**
     * 获得逻辑库存移动分页
     *
     * @param pageReqVO 分页查询
     * @return 逻辑库存移动分页
     */
    PageResult<WmsStockLogicMoveDO> getStockLogicMovePage(WmsStockLogicMovePageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockLogicMoveDO
     */
    List<WmsStockLogicMoveDO> selectByIds(List<Long> idList);

    void finishMove(WmsStockLogicMoveDO logicMoveDO, List<WmsStockLogicMoveItemDO> logicMoveItemDOList);

    void assembleWarehouse(List<WmsStockLogicMoveRespVO> list);
}
