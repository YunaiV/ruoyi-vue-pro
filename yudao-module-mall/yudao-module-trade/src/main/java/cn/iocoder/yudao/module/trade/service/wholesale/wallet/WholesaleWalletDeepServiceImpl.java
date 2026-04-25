package cn.iocoder.yudao.module.trade.service.wholesale.wallet;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.wallet.PayWalletApi;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletPayResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletRechargeResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletSummaryBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 批发商钱包深度集成服务实现
 * <p>
 * 直接调用后台现有支付 API：
 * <ul>
 *   <li>{@code PayWalletApi.getOrCreateWallet}     — 查询余额</li>
 *   <li>{@code PayWalletApi.addWalletBalance(-price)} — 直接扣款（负值）</li>
 *   <li>{@code PayOrderApi.createOrder}            — 发起充值支付单</li>
 * </ul>
 *
 * @author deepay
 */
@Slf4j
@Service
public class WholesaleWalletDeepServiceImpl implements WholesaleWalletDeepService {

    /** 充值支付单默认过期时间（小时） */
    private static final int RECHARGE_EXPIRE_HOURS = 1;

    @Resource
    private PayWalletApi payWalletApi;

    @Resource
    private PayOrderApi payOrderApi;

    /** 应用标识（与 WholesalePaymentServiceImpl 使用同一 appKey） */
    @Value("${b2b.pay.app-key:wholesale}")
    private String payAppKey;

    // ==================== 查询余额 ====================

    @Override
    public WholesaleWalletSummaryBO getWalletSummary(Long userId, Integer userType) {
        PayWalletRespDTO wallet = payWalletApi.getOrCreateWallet(userId, userType);

        WholesaleWalletSummaryBO bo = new WholesaleWalletSummaryBO();
        bo.setUserId(userId);
        if (wallet != null) {
            bo.setWalletId(wallet.getId());
            bo.setBalance(wallet.getBalance() != null ? wallet.getBalance() : 0);
            bo.setBalanceYuan(fenToYuan(bo.getBalance()));
            bo.setTotalRecharge(wallet.getTotalRecharge() != null ? wallet.getTotalRecharge() : 0);
            bo.setTotalExpense(wallet.getTotalExpense() != null ? wallet.getTotalExpense() : 0);
            bo.setFreezePrice(wallet.getFreezePrice() != null ? wallet.getFreezePrice() : 0);
        }
        log.debug("[getWalletSummary][userId={} balance={}]", userId, bo.getBalance());
        return bo;
    }

    // ==================== 直接扣款 ====================

    @Override
    public WholesaleWalletPayResultBO deductWalletForOrder(Long userId, Integer userType,
                                                            String merchantOrderId, Integer priceInFen) {
        WholesaleWalletPayResultBO result = new WholesaleWalletPayResultBO();
        result.setMerchantOrderId(merchantOrderId);
        result.setDeductedPrice(priceInFen);

        // 1. 查询当前余额
        PayWalletRespDTO wallet = payWalletApi.getOrCreateWallet(userId, userType);
        int balance = wallet != null && wallet.getBalance() != null ? wallet.getBalance() : 0;
        result.setBalanceBefore(balance);

        // 2. 余额不足，返回失败
        if (balance < priceInFen) {
            result.setSuccess(false);
            result.setBalanceAfter(balance);
            result.setFailReason("钱包余额不足，当前余额 " + fenToYuan(balance) + " 元，需要 " + fenToYuan(priceInFen) + " 元");
            result.setMessage("余额不足，请先充值");
            log.info("[deductWalletForOrder][余额不足 userId={} balance={} required={}]",
                    userId, balance, priceInFen);
            return result;
        }

        // 3. 调用 addWalletBalance（负值表示扣款）
        try {
            PayWalletAddBalanceReqDTO reqDTO = new PayWalletAddBalanceReqDTO();
            reqDTO.setUserId(userId);
            reqDTO.setUserType(userType != null ? userType : UserTypeEnum.MEMBER.getValue());
            reqDTO.setBizType(PayWalletBizTypeEnum.PAYMENT.getType());
            reqDTO.setBizId(merchantOrderId);
            reqDTO.setPrice(-priceInFen); // 负值 = 扣款

            payWalletApi.addWalletBalance(reqDTO);

            result.setSuccess(true);
            result.setBalanceAfter(balance - priceInFen);
            result.setMessage("钱包支付成功，已扣款 " + fenToYuan(priceInFen) + " 元");
            log.info("[deductWalletForOrder][扣款成功 userId={} merchantOrderId={} price={}]",
                    userId, merchantOrderId, priceInFen);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setBalanceAfter(balance);
            result.setFailReason(e.getMessage());
            result.setMessage("钱包扣款失败：" + e.getMessage());
            log.error("[deductWalletForOrder][扣款失败 userId={} merchantOrderId={}]",
                    userId, merchantOrderId, e);
        }
        return result;
    }

    // ==================== 发起充值支付单 ====================

    @Override
    public WholesaleWalletRechargeResultBO createRechargePayOrder(Long userId, Integer userType,
                                                                   Integer rechargeYuan, String userIp) {
        // 充值金额（分）
        int rechargeFen = rechargeYuan * 100;
        // 充值支付单商户号（使用 userId + 时间戳，确保唯一）
        String rechargeOrderId = "WH_RECHARGE_" + userId + "_" + System.currentTimeMillis();

        PayOrderCreateReqDTO createReqDTO = new PayOrderCreateReqDTO();
        createReqDTO.setAppKey(payAppKey);
        createReqDTO.setUserIp(userIp != null ? userIp : "127.0.0.1");
        createReqDTO.setUserId(userId);
        createReqDTO.setUserType(userType != null ? userType : UserTypeEnum.MEMBER.getValue());
        createReqDTO.setMerchantOrderId(rechargeOrderId);
        createReqDTO.setSubject("批发商钱包充值 " + rechargeYuan + " 元");
        createReqDTO.setBody("B2B批发商userId=" + userId + " 充值 " + rechargeYuan + " 元");
        createReqDTO.setPrice(rechargeFen);
        createReqDTO.setExpireTime(LocalDateTime.now().plusHours(RECHARGE_EXPIRE_HOURS));

        Long payOrderId = payOrderApi.createOrder(createReqDTO);

        WholesaleWalletRechargeResultBO result = new WholesaleWalletRechargeResultBO();
        result.setPayOrderId(payOrderId);
        result.setRechargePrice(rechargeFen);
        result.setExpireTime(LocalDateTime.now().plusHours(RECHARGE_EXPIRE_HOURS)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.setNextAction(
                "请调用 POST /pay/order/submit，传 payOrderId=" + payOrderId
                + "，channelCode 选择 alipay_pc / alipay_wap / alipay_app / wx_pub / wx_native 等完成充值。"
                + "充值完成后余额自动增加。");

        log.info("[createRechargePayOrder][充值支付单已创建 userId={} payOrderId={} price={}]",
                userId, payOrderId, rechargeFen);
        return result;
    }

    // ==================== 工具 ====================

    private static String fenToYuan(int fen) {
        return BigDecimal.valueOf(fen)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .toPlainString();
    }

}
