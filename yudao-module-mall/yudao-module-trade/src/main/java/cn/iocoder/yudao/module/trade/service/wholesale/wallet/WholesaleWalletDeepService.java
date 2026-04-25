package cn.iocoder.yudao.module.trade.service.wholesale.wallet;

import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletPayResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletRechargeResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletSummaryBO;

/**
 * 批发商钱包深度集成服务
 * <p>
 * 在 B2B 批发业务层提供：
 * <ol>
 *   <li>查询钱包余额摘要（{@link #getWalletSummary}）</li>
 *   <li>钱包直接扣款（{@link #deductWalletForOrder}）—— 无需跳转支付渠道，直接从余额中扣除</li>
 *   <li>发起充值支付单（{@link #createRechargePayOrder}）—— 生成充值支付单，用户通过支付宝/微信完成充值</li>
 * </ol>
 *
 * @author deepay
 */
public interface WholesaleWalletDeepService {

    /**
     * 获取批发商钱包摘要（余额、累计充值、累计消费）
     *
     * @param userId   批发商用户编号
     * @param userType 用户类型
     * @return 钱包摘要 BO
     */
    WholesaleWalletSummaryBO getWalletSummary(Long userId, Integer userType);

    /**
     * 使用钱包余额直接扣款支付批发订单
     * <p>
     * 调用 {@code PayWalletApi.addWalletBalance}（负值扣款），业务类型=PAYMENT。
     * 若余额不足，返回失败结果（不抛异常）。
     *
     * @param userId          批发商用户编号
     * @param userType        用户类型
     * @param merchantOrderId 商户订单号（去重用）
     * @param priceInFen      扣款金额（分）
     * @return 扣款结果 BO
     */
    WholesaleWalletPayResultBO deductWalletForOrder(Long userId, Integer userType,
                                                     String merchantOrderId, Integer priceInFen);

    /**
     * 发起钱包充值支付单
     * <p>
     * 通过 {@code PayOrderApi.createOrder} 创建支付单，用户通过 POST /pay/order/submit
     * 选择支付宝或微信完成充值，充值完成后余额自动增加。
     *
     * @param userId       批发商用户编号
     * @param userType     用户类型
     * @param rechargeYuan 充值金额（元）
     * @param userIp       用户 IP
     * @return 充值支付单信息 BO
     */
    WholesaleWalletRechargeResultBO createRechargePayOrder(Long userId, Integer userType,
                                                            Integer rechargeYuan, String userIp);

}
