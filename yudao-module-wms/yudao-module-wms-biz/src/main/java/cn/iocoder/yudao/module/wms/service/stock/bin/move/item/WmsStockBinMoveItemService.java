package cn.iocoder.yudao.module.wms.service.stock.bin.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveImportExcelVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 库位移动详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockBinMoveItemService {

    /**
     * 创建库位移动详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockBinMoveItemDO createStockBinMoveItem(@Valid WmsStockBinMoveItemSaveReqVO createReqVO);

    /**
     * 更新库位移动详情
     *
     * @param updateReqVO 更新信息
     */
    WmsStockBinMoveItemDO updateStockBinMoveItem(@Valid WmsStockBinMoveItemSaveReqVO updateReqVO);

    /**
     * 删除库位移动详情
     *
     * @param id 编号
     */
    void deleteStockBinMoveItem(Long id);

    /**
     * 获得库位移动详情
     *
     * @param id 编号
     * @return 库位移动详情
     */
    WmsStockBinMoveItemDO getStockBinMoveItem(Long id);

    /**
     * 获得库位移动详情分页
     *
     * @param pageReqVO 分页查询
     * @return 库位移动详情分页
     */
    PageResult<WmsStockBinMoveItemDO> getStockBinMoveItemPage(WmsStockBinMoveItemPageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockBinMoveItemDO
     */
    List<WmsStockBinMoveItemDO> selectByIds(List<Long> idList);

    List<WmsStockBinMoveItemDO> selectByBinMoveId(Long binMoveId);

    void assembleBin(List<WmsStockBinMoveItemRespVO> stockBinMoveItemList);

    void assembleProduct(List<WmsStockBinMoveItemRespVO> stockBinMoveItemList);

    void assembleBinMove(List<WmsStockBinMoveItemRespVO> list);

    void assembleWarehouseForImp(List<WmsStockBinMoveImportExcelVO> impVOList);

    void assembleBinForImp(List<WmsStockBinMoveImportExcelVO> impVOList);

    void assembleProductForImp(List<WmsStockBinMoveImportExcelVO> impVOList);
}
