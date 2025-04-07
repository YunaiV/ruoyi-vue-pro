package cn.iocoder.yudao.module.wms.service.inventory.result.item;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.item.WmsInventoryResultItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 库存盘点结果详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInventoryResultItemService {

    /**
     * 创建库存盘点结果详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInventoryResultItem(@Valid WmsInventoryResultItemSaveReqVO createReqVO);

    /**
     * 更新库存盘点结果详情
     *
     * @param updateReqVO 更新信息
     */
    void updateInventoryResultItem(@Valid WmsInventoryResultItemSaveReqVO updateReqVO);

    /**
     * 删除库存盘点结果详情
     *
     * @param id 编号
     */
    void deleteInventoryResultItem(Long id);

    /**
     * 获得库存盘点结果详情
     *
     * @param id 编号
     * @return 库存盘点结果详情
     */
    WmsInventoryResultItemDO getInventoryResultItem(Long id);

    /**
     * 获得库存盘点结果详情分页
     *
     * @param pageReqVO 分页查询
     * @return 库存盘点结果详情分页
     */
    PageResult<WmsInventoryResultItemDO> getInventoryResultItemPage(WmsInventoryResultItemPageReqVO pageReqVO);

}