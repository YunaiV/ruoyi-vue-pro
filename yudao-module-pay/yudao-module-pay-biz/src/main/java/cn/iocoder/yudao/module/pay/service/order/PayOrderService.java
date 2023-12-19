package cn.iocoder.yudao.module.pay.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 支付订单 Service 接口
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
     *
     * @param appId           应用编号
     * @param merchantOrderId 商户订单编号
     * @return 支付订单
     */
    PayOrderDO getOrder(Long appId, String merchantOrderId);

    /**
     * 获得支付订单列表
     *
     * @param ids 编号数组
     * @return 支付订单列表
     */
    List<PayOrderDO> getOrderList(Collection<Long> ids);

    /**
     * 获得指定应用的订单数量
     *
     * @param appId 应用编号
     * @return 订单数量
     */
    Long getOrderCountByAppId(Long appId);

    /**
     * 获得支付订单分页
     *
     * @param pageReqVO 分页查询
     * @return 支付订单分页
     */
    PageResult<PayOrderDO> getOrderPage(PayOrderPageReqVO pageReqVO);

    /**
     * 获得支付订单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 支付订单列表
     */
    List<PayOrderDO> getOrderList(PayOrderExportReqVO exportReqVO);

    /**
     * 创建支付单
     *
     * @param reqDTO 创建请求
     * @return 支付单编号
     */
    Long createOrder(@Valid PayOrderCreateReqDTO reqDTO);

    /**
     * 提交支付
     * 此时，会发起支付渠道的调用
     *
     * @param reqVO  提交请求
     * @param userIp 提交 IP
     * @return 提交结果
     */
    PayOrderSubmitRespVO submitOrder(@Valid PayOrderSubmitReqVO reqVO,
                                     @NotEmpty(message = "提交 IP 不能为空") String userIp);

    /**
     * 通知支付单成功
     *
     * @param channelId 渠道编号
     * @param notify    通知
     */
    void notifyOrder(Long channelId, PayOrderRespDTO notify);

    /**
     * 更新支付订单的退款金额
     *
     * @param id              编号
     * @param incrRefundPrice 增加的退款金额
     */
    void updateOrderRefundPrice(Long id, Integer incrRefundPrice);

    /**
     * 更新支付订单价格
     *
     * @param id 支付单编号
     * @param payPrice   支付单价格
     */
    void updatePayOrderPrice(Long id, Integer payPrice);

    /**
     * 获得支付订单
     *
     * @param id 编号
     * @return 支付订单
     */
    PayOrderExtensionDO getOrderExtension(Long id);

    /**
     * 获得支付订单
     *
     * @param no 支付订单 no
     * @return 支付订单
     */
    PayOrderExtensionDO getOrderExtensionByNo(String no);

    /**
     * 同步订单的支付状态
     *
     * @param minCreateTime 最小创建时间
     * @return 同步到已支付的订单数量
     */
    int syncOrder(LocalDateTime minCreateTime);

    /**
     * 将已过期的订单，状态修改为已关闭
     *
     * @return 过期的订单数量
     */
    int expireOrder();

}
