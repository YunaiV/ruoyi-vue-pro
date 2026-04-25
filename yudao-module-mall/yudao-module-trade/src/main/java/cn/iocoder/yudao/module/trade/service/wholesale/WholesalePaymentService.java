package cn.iocoder.yudao.module.trade.service.wholesale;

import cn.iocoder.yudao.module.trade.service.wholesale.pay.bo.WholesalePayResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.pay.bo.WholesaleRefundResultBO;

/**
 * 批发订单支付服务
 * <p>
 * 整合后台现有支付体系：
 * <ul>
 *   <li>{@code PayOrderApi}   — 创建 / 查询支付单（支付宝、微信、余额等均走此接口）</li>
 *   <li>{@code PayRefundApi}  — 发起退款单</li>
 *   <li>{@code PayWalletApi}  — 查询钱包余额，判断是否可走余额支付</li>
 *   <li>{@code PayTransferApi}— 平台向供应商打款（企业转账）</li>
 * </ul>
 *
 * @author deepay
 */
public interface WholesalePaymentService {

    /**
     * 为批发订单创建支付单
     * <p>
     * 先检查钱包余额，余额充足时标注 channelUsed=wallet；
     * 否则正常创建支付单，由用户在前端选择支付渠道提交。
     *
     * @param userId          批发商用户编号
     * @param userType        用户类型（{@code UserTypeEnum.MEMBER}=1）
     * @param merchantOrderId 商户订单号（合同编号等）
     * @param subject         商品标题（最长 32 字符）
     * @param priceInFen      支付金额（分）
     * @param userIp          用户 IP
     * @return 支付结果 BO
     */
    WholesalePayResultBO createPayOrder(Long userId, Integer userType,
                                        String merchantOrderId, String subject,
                                        Integer priceInFen, String userIp);

    /**
     * 查询支付单状态
     *
     * @param payOrderId 支付单编号
     * @return 支付结果 BO
     */
    WholesalePayResultBO getPayOrder(Long payOrderId);

    /**
     * 发起批发订单退款
     *
     * @param userId           批发商用户编号
     * @param userType         用户类型
     * @param merchantOrderId  原商户订单号
     * @param merchantRefundId 商户退款单号（需全局唯一）
     * @param refundPrice      退款金额（分）
     * @param reason           退款原因
     * @param userIp           用户 IP
     * @return 退款结果 BO
     */
    WholesaleRefundResultBO createRefund(Long userId, Integer userType,
                                          String merchantOrderId, String merchantRefundId,
                                          Integer refundPrice, String reason, String userIp);

    /**
     * 向供应商打款（平台企业转账）
     *
     * @param supplierId      供应商用户编号
     * @param transferOrderId 商户转账单号
     * @param priceInFen      转账金额（分）
     * @param userIp          发起方 IP
     * @return 转账单编号
     */
    Long createSupplierTransfer(Long supplierId, String transferOrderId,
                                 Integer priceInFen, String userIp);

}
