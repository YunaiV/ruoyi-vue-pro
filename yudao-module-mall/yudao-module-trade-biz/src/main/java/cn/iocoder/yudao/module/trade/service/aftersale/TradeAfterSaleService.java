package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleAuditReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleConfirmReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleDeliveryReqVO;

/**
 * 交易售后 Service 接口
 *
 * @author 芋道源码
 */
public interface TradeAfterSaleService {

    /**
     * 创建交易售后
     * <p>
     * 一般是用户发起售后请求
     *
     * @param userId 会员用户编号
     * @param createReqVO 创建 Request 信息
     * @return 交易售后编号
     */
    Long createAfterSale(Long userId, AppTradeAfterSaleCreateReqVO createReqVO);

    /**
     * 审批交易售后
     *
     * @param userId 管理员用户编号
     * @param userIp 操作 IP
     * @param auditReqVO 审批 Request 信息
     */
    void auditAfterSale(Long userId, String userIp, TradeAfterSaleAuditReqVO auditReqVO);

    /**
     * 退回货物
     *
     * @param userId 会员用户编号
     * @param deliveryReqVO 退货 Request 信息
     */
    void deliveryAfterSale(Long userId, AppTradeAfterSaleDeliveryReqVO deliveryReqVO);

    /**
     * 确认收货
     *
     * @param userId 管理员用户编号
     * @param userIp 操作 IP
     * @param confirmReqVO 收货 Request 信息
     */
    void confirmAfterSale(Long userId, String userIp, TradeAfterSaleConfirmReqVO confirmReqVO);

    /**
     * 确认退款，由【pay】支付服务回调
     *
     * @param payRefundId 支付退款编号
     */
    void refundAfterSale(Long payRefundId);

}
