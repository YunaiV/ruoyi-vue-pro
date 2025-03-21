package cn.iocoder.yudao.module.wms.service.pickup.item;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 拣货单详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsPickupItemService {

    /**
     * 创建拣货单详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsPickupItemDO createPickupItem(@Valid WmsPickupItemSaveReqVO createReqVO);

    /**
     * 更新拣货单详情
     *
     * @param updateReqVO 更新信息
     */
    WmsPickupItemDO updatePickupItem(@Valid WmsPickupItemSaveReqVO updateReqVO);

    /**
     * 删除拣货单详情
     *
     * @param id 编号
     */
    void deletePickupItem(Long id);

    /**
     * 获得拣货单详情
     *
     * @param id 编号
     * @return 拣货单详情
     */
    WmsPickupItemDO getPickupItem(Long id);

    /**
     * 获得拣货单详情分页
     *
     * @param pageReqVO 分页查询
     * @return 拣货单详情分页
     */
    PageResult<WmsPickupItemDO> getPickupItemPage(WmsPickupItemPageReqVO pageReqVO);

    List<WmsPickupItemDO> selectByPickupId(Long id);
}
