package cn.iocoder.yudao.module.wms.service.inventory;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 盘点 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInventoryService {

    /**
     * 创建盘点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInventory(@Valid WmsInventorySaveReqVO createReqVO);

    /**
     * 更新盘点
     *
     * @param updateReqVO 更新信息
     */
    void updateInventory(@Valid WmsInventorySaveReqVO updateReqVO);

    /**
     * 删除盘点
     *
     * @param id 编号
     */
    void deleteInventory(Long id);

    /**
     * 获得盘点
     *
     * @param id 编号
     * @return 盘点
     */
    WmsInventoryDO getInventory(Long id);

    /**
     * 获得盘点分页
     *
     * @param pageReqVO 分页查询
     * @return 盘点分页
     */
    PageResult<WmsInventoryDO> getInventoryPage(WmsInventoryPageReqVO pageReqVO);

}