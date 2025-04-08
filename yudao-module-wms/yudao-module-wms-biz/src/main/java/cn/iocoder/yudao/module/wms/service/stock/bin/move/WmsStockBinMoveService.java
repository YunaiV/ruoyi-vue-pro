package cn.iocoder.yudao.module.wms.service.stock.bin.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 库位移动 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockBinMoveService {

    /**
     * 创建库位移动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockBinMoveDO createStockBinMove(@Valid WmsStockBinMoveSaveReqVO createReqVO);

    /**
     * 更新库位移动
     *
     * @param updateReqVO 更新信息
     */
    WmsStockBinMoveDO updateStockBinMove(@Valid WmsStockBinMoveSaveReqVO updateReqVO);

    /**
     * 删除库位移动
     *
     * @param id 编号
     */
    void deleteStockBinMove(Long id);

    /**
     * 获得库位移动
     *
     * @param id 编号
     * @return 库位移动
     */
    WmsStockBinMoveDO getStockBinMove(Long id);

    /**
     * 获得库位移动分页
     *
     * @param pageReqVO 分页查询
     * @return 库位移动分页
     */
    PageResult<WmsStockBinMoveDO> getStockBinMovePage(WmsStockBinMovePageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockBinMoveDO
     */
    List<WmsStockBinMoveDO> selectByIds(List<Long> idList);

    /**
     * 按 ID 集合查询 WmsStockBinMoveDO
     */
    List<WmsStockBinMoveDO> selectSimpleList(WmsStockBinMovePageReqVO reqVO);

    /**
     * 完成库位移动
     **/
    void finishMove(WmsStockBinMoveDO binMoveDO, List<WmsStockBinMoveItemDO> binMoveItemDOList);


    void assembleWarehouse(List<WmsStockBinMoveRespVO> list);
}
