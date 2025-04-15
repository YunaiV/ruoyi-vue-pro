package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderSaveReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * OMS订单 Service 接口
 *
 * @author 谷毛毛
 */
public interface OmsOrderService {

    /**
     * 创建OMS订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOrder(@Valid OmsOrderSaveReqVO createReqVO);

    /**
     * 更新OMS订单
     *
     * @param updateReqVO 更新信息
     */
    void updateOrder(@Valid OmsOrderSaveReqVO updateReqVO);

    /**
     * 删除OMS订单
     *
     * @param id 编号
     */
    void deleteOrder(Long id);

    /**
     * 获得OMS订单
     *
     * @param id 编号
     * @return OMS订单
     */
    OmsOrderDO getOrder(Long id);

    /**
     * 获得OMS订单分页
     *
     * @param pageReqVO 分页查询
     * @return OMS订单分页
     */
    PageResult<OmsOrderDO> getOrderPage(OmsOrderPageReqVO pageReqVO);

    // ==================== 子表（OMS订单项） ====================

    /**
     * 获得OMS订单项分页
     *
     * @param pageReqVO 分页查询
     * @param orderId   销售订单id
     * @return OMS订单项分页
     */
    PageResult<OmsOrderItemDO> getOrderItemPage(PageParam pageReqVO, Long orderId);

    /**
     * 创建OMS订单项
     *
     * @param orderItem 创建信息
     * @return 编号
     */
    Long createOrderItem(@Valid OmsOrderItemDO orderItem);

    /**
     * 更新OMS订单项
     *
     * @param orderItem 更新信息
     */
    void updateOrderItem(@Valid OmsOrderItemDO orderItem);

    /**
     * 删除OMS订单项
     *
     * @param id 编号
     */
    void deleteOrderItem(Long id);

    /**
     * 获得OMS订单项
     *
     * @param id 编号
     * @return OMS订单项
     */
    OmsOrderItemDO getOrderItem(Long id);

    /**
     * @Description: 按平台创建或更新订单信息
     * @return:
     */
    void createOrUpdateOrderByPlatform(List<OmsOrderSaveReqDTO> saveReqDTOs);

    /**
     * @param platformCode 平台代码
     * @Description: 根据平台获取订单列表
     * @return: @return {@link List }<{@link OmsOrderDO }>
     */
    List<OmsOrderDO> getByPlatformCode(String platformCode);


    /**
     * 获得销售订单项 List
     *
     * @param orderIds 销售订单编号数组
     * @return 销售订单项 List
     */
    List<OmsOrderItemDO> getSaleOrderItemListByOrderIds(Collection<Long> orderIds);

}