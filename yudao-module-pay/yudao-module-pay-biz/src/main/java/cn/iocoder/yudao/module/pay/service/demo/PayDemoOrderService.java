package cn.iocoder.yudao.module.pay.service.demo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.order.PayDemoOrderCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;

import jakarta.validation.Valid;

/**
 * 示例订单 Service 接口
 *
 * @author 芋道源码
 */
public interface PayDemoOrderService {

    /**
     * 创建示例订单
     *
     * @param userId      用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDemoOrder(Long userId, @Valid PayDemoOrderCreateReqVO createReqVO);

    /**
     * 获得示例订单
     *
     * @param id 编号
     * @return 示例订单
     */
    PayDemoOrderDO getDemoOrder(Long id);

    /**
     * 获得示例订单分页
     *
     * @param pageReqVO 分页查询
     * @return 示例订单分页
     */
    PageResult<PayDemoOrderDO> getDemoOrderPage(PageParam pageReqVO);

    /**
     * 更新示例订单为已支付
     *
     * @param id 编号
     * @param payOrderId 支付订单号
     */
    void updateDemoOrderPaid(Long id, Long payOrderId);

    /**
     * 发起示例订单的退款
     *
     * @param id 编号
     * @param userIp 用户编号
     */
    void refundDemoOrder(Long id, String userIp);

    /**
     * 更新示例订单为已退款
     *
     * @param id 编号
     * @param payRefundId 退款订单号
     */
    void updateDemoOrderRefunded(Long id, Long payRefundId);

}
