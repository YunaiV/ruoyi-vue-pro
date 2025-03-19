package cn.iocoder.yudao.module.wms.service.stock.flow;

import java.util.*;
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
}
