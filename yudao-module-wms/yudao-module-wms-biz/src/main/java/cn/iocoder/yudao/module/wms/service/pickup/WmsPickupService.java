package cn.iocoder.yudao.module.wms.service.pickup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 拣货单 Service 接口
 *
 * @author 李方捷
 */
public interface WmsPickupService {

    /**
     * 创建拣货单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsPickupDO createPickup(@Valid WmsPickupSaveReqVO createReqVO);

    /**
     * 更新拣货单
     *
     * @param updateReqVO 更新信息
     */
    WmsPickupDO updatePickup(@Valid WmsPickupSaveReqVO updateReqVO);

    /**
     * 删除拣货单
     *
     * @param id 编号
     */
    void deletePickup(Long id);

    /**
     * 获得拣货单
     *
     * @param id 编号
     * @return 拣货单
     */
    WmsPickupDO getPickup(Long id);

    /**
     * 获得拣货单分页
     *
     * @param pageReqVO 分页查询
     * @return 拣货单分页
     */
    PageResult<WmsPickupDO> getPickupPage(WmsPickupPageReqVO pageReqVO);

    List<WmsPickupDO> selectByIds(List<Long> list);

    void assembleWarehouse(List<WmsPickupRespVO> list);

    void createForInventory(WmsPickupSaveReqVO pickupSaveReqVO);
}
