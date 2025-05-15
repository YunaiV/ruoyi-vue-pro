package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderPageReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;

import java.util.Collection;
import java.util.List;

/**
 * OMS订单 Service 接口
 *
 * @author 谷毛毛
 */
public interface OmsOrderService {

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
    List<OmsOrderItemDO> getOrderItemListByOrderIds(Collection<Long> orderIds);

}