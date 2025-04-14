package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oms.controller.admin.order.item.vo.OmsOrderItemPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.order.item.vo.OmsOrderItemSaveReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * OMS订单项 Service 接口
 *
 * @author 谷毛毛
 */
public interface OmsOrderItemService {

    /**
     * 创建OMS订单项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOrderItem(@Valid OmsOrderItemSaveReqVO createReqVO);

    /**
     * 更新OMS订单项
     *
     * @param updateReqVO 更新信息
     */
    void updateOrderItem(@Valid OmsOrderItemSaveReqVO updateReqVO);

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
     * 获得OMS订单项分页
     *
     * @param pageReqVO 分页查询
     * @return OMS订单项分页
     */
    PageResult<OmsOrderItemDO> getOrderItemPage(OmsOrderItemPageReqVO pageReqVO);

    /**
     * @param omsOrderItemDOList OMS订购项目dolist
     * @Description: 保存批处理
     * @return:
     */
    void saveOmsOrderItemDOList(List<OmsOrderItemDO> omsOrderItemDOList);

    /**
     * @param orderIds 订单id集合
     * @Description: 按订单ID集合删除订单项
     * @return: @return int 删除数量
     */
    int deleteByOrderIds(List<Long> orderIds);

}