package cn.iocoder.yudao.module.wms.service.stock.bin.move;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

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
    Long createStockBinMove(@Valid WmsStockBinMoveSaveReqVO createReqVO);

    /**
     * 更新库位移动
     *
     * @param updateReqVO 更新信息
     */
    void updateStockBinMove(@Valid WmsStockBinMoveSaveReqVO updateReqVO);

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

}