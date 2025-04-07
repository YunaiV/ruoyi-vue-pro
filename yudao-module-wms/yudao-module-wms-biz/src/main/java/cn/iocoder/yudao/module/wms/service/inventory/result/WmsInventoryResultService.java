package cn.iocoder.yudao.module.wms.service.inventory.result;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.WmsInventoryResultDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 库存盘点结果 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInventoryResultService {

    /**
     * 创建库存盘点结果
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInventoryResult(@Valid WmsInventoryResultSaveReqVO createReqVO);

    /**
     * 更新库存盘点结果
     *
     * @param updateReqVO 更新信息
     */
    void updateInventoryResult(@Valid WmsInventoryResultSaveReqVO updateReqVO);

    /**
     * 删除库存盘点结果
     *
     * @param id 编号
     */
    void deleteInventoryResult(Long id);

    /**
     * 获得库存盘点结果
     *
     * @param id 编号
     * @return 库存盘点结果
     */
    WmsInventoryResultDO getInventoryResult(Long id);

    /**
     * 获得库存盘点结果分页
     *
     * @param pageReqVO 分页查询
     * @return 库存盘点结果分页
     */
    PageResult<WmsInventoryResultDO> getInventoryResultPage(WmsInventoryResultPageReqVO pageReqVO);

}