package cn.iocoder.yudao.module.wms.service.stock.logic.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMoveImportExcelVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 逻辑库存移动详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockLogicMoveItemService {

    /**
     * 创建逻辑库存移动详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockLogicMoveItemDO createStockLogicMoveItem(@Valid WmsStockLogicMoveItemSaveReqVO createReqVO);

    /**
     * 更新逻辑库存移动详情
     *
     * @param updateReqVO 更新信息
     */
    WmsStockLogicMoveItemDO updateStockLogicMoveItem(@Valid WmsStockLogicMoveItemSaveReqVO updateReqVO);

    /**
     * 删除逻辑库存移动详情
     *
     * @param id 编号
     */
    void deleteStockLogicMoveItem(Long id);

    /**
     * 获得逻辑库存移动详情
     *
     * @param id 编号
     * @return 逻辑库存移动详情
     */
    WmsStockLogicMoveItemDO getStockLogicMoveItem(Long id);

    /**
     * 获得逻辑库存移动详情分页
     *
     * @param pageReqVO 分页查询
     * @return 逻辑库存移动详情分页
     */
    PageResult<WmsStockLogicMoveItemDO> getStockLogicMoveItemPage(WmsStockLogicMoveItemPageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockLogicMoveItemDO
     */
    List<WmsStockLogicMoveItemDO> selectByIds(List<Long> idList);

    List<WmsStockLogicMoveItemDO> selectByLogicMoveId(Long logicMoveId);

    void assembleProduct(List<WmsStockLogicMoveItemRespVO> itemList);

    void assembleCompanyAndDept(List<WmsStockLogicMoveItemRespVO> itemList);

    void assembleWarehouseForImp(List<WmsStockLogicMoveImportExcelVO> impVOList);

    void assembleCompanyAndDeptForImp(List<WmsStockLogicMoveImportExcelVO> impVOList);

    void assembleProductForImp(List<WmsStockLogicMoveImportExcelVO> impVOList);
}
