package cn.iocoder.yudao.adminserver.modules.pay.service.order;

import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.Collection;
import java.util.List;

/**
 * 支付订单
 * Service 接口
 *
 * @author aquan
 */
public interface PayOrderService {

    /**
     * 获得支付订单
     *
     * @param id 编号
     * @return 支付订单
     */
    PayOrderDO getOrder(Long id);

    /**
     * 获得支付订单
     * 列表
     *
     * @param ids 编号
     * @return 支付订单
     * 列表
     */
    List<PayOrderDO> getOrderList(Collection<Long> ids);

    /**
     * 获得支付订单
     * 分页
     *
     * @param pageReqVO 分页查询
     * @return 支付订单
     * 分页
     */
    PageResult<PayOrderDO> getOrderPage(PayOrderPageReqVO pageReqVO);

    /**
     * 获得支付订单
     * 列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 支付订单
     * 列表
     */
    List<PayOrderDO> getOrderList(PayOrderExportReqVO exportReqVO);

}
