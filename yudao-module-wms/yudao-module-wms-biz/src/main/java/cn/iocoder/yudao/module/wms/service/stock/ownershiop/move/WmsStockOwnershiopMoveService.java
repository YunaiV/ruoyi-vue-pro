package cn.iocoder.yudao.module.wms.service.stock.ownershiop.move;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownershiop.move.WmsStockOwnershiopMoveDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 所有者库存移动 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockOwnershiopMoveService {

    /**
     * 创建所有者库存移动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockOwnershiopMove(@Valid WmsStockOwnershiopMoveSaveReqVO createReqVO);

    /**
     * 更新所有者库存移动
     *
     * @param updateReqVO 更新信息
     */
    void updateStockOwnershiopMove(@Valid WmsStockOwnershiopMoveSaveReqVO updateReqVO);

    /**
     * 删除所有者库存移动
     *
     * @param id 编号
     */
    void deleteStockOwnershiopMove(Long id);

    /**
     * 获得所有者库存移动
     *
     * @param id 编号
     * @return 所有者库存移动
     */
    WmsStockOwnershiopMoveDO getStockOwnershiopMove(Long id);

    /**
     * 获得所有者库存移动分页
     *
     * @param pageReqVO 分页查询
     * @return 所有者库存移动分页
     */
    PageResult<WmsStockOwnershiopMoveDO> getStockOwnershiopMovePage(WmsStockOwnershiopMovePageReqVO pageReqVO);

}