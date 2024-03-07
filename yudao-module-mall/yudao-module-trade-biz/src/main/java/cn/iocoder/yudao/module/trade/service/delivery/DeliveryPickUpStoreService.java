package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStorePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreUpdateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 自提门店 Service 接口
 *
 * @author jason
 */
public interface DeliveryPickUpStoreService {

    /**
     * 创建自提门店
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeliveryPickUpStore(@Valid DeliveryPickUpStoreCreateReqVO createReqVO);

    /**
     * 更新自提门店
     *
     * @param updateReqVO 更新信息
     */
    void updateDeliveryPickUpStore(@Valid DeliveryPickUpStoreUpdateReqVO updateReqVO);

    /**
     * 删除自提门店
     *
     * @param id 编号
     */
    void deleteDeliveryPickUpStore(Long id);

    /**
     * 获得自提门店
     *
     * @param id 编号
     * @return 自提门店
     */
    DeliveryPickUpStoreDO getDeliveryPickUpStore(Long id);

    /**
     * 获得自提门店列表
     *
     * @param ids 编号
     * @return 自提门店列表
     */
    List<DeliveryPickUpStoreDO> getDeliveryPickUpStoreList(Collection<Long> ids);

    /**
     * 获得自提门店分页
     *
     * @param pageReqVO 分页查询
     * @return 自提门店分页
     */
    PageResult<DeliveryPickUpStoreDO> getDeliveryPickUpStorePage(DeliveryPickUpStorePageReqVO pageReqVO);

    /**
     * 获得指定状态的自提门店列表
     *
     * @param status 状态
     * @return 自提门店列表
     */
    List<DeliveryPickUpStoreDO> getDeliveryPickUpStoreListByStatus(Integer status);
}
