package cn.iocoder.yudao.module.trade.service.wholesale;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import cn.iocoder.yudao.module.pay.api.wallet.PayWalletApi;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;
import cn.iocoder.yudao.module.trade.service.wholesale.pay.bo.WholesalePayResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.pay.bo.WholesaleRefundResultBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 批发订单支付服务实现
 * <p>
 * 直接调用后台现有支付 API（PayOrderApi / PayRefundApi / PayWalletApi / PayTransferApi）：
 * <pre>
 *  createPayOrder()  →  PayWalletApi.getOrCreateWallet()  检查余额
 *                    →  PayOrderApi.createOrder()          建立支付单
 *  createRefund()    →  PayRefundApi.createRefund()        发起退款
 *  createSupplierTransfer() → PayTransferApi.createTransfer()  企业转账
 * </pre>
 *
 * @author deepay
 */
@Slf4j
@Service
public class WholesalePaymentServiceImpl implements WholesalePaymentService {

    /** 支付单默认过期时间（小时） */
    private static final int DEFAULT_EXPIRE_HOURS = 2;

    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private PayRefundApi payRefundApi;
    @Resource
    private PayWalletApi payWalletApi;
    @Resource
    private PayTransferApi payTransferApi;

    /** 应用标识（对应 pay_app 表中的 appKey） */
    @Value("${b2b.pay.app-key:wholesale}")
    private String payAppKey;

    // ==================== 公开接口 ====================

    @Override
    public WholesalePayResultBO createPayOrder(Long userId, Integer userType,
                                               String merchantOrderId, String subject,
                                               Integer priceInFen, String userIp) {
        WholesalePayResultBO result = new WholesalePayResultBO();
        result.setMerchantOrderId(merchantOrderId);
        result.setPrice(priceInFen);

        // 1. 查询钱包余额，给前端提示是否可走余额支付
        int walletBalance = 0;
        boolean walletSufficient = false;
        try {
            PayWalletRespDTO wallet = payWalletApi.getOrCreateWallet(userId, userType);
            if (wallet != null) {
                walletBalance = wallet.getBalance() != null ? wallet.getBalance() : 0;
                walletSufficient = walletBalance >= priceInFen;
            }
        } catch (Exception e) {
            log.warn("查询批发商钱包余额失败 userId={}: {}", userId, e.getMessage());
        }
        result.setWalletBalance(walletBalance);
        result.setWalletSufficient(walletSufficient);
        result.setChannelUsed(walletSufficient ? "wallet" : "pending_selection");

        // 2. 通过 PayOrderApi 创建支付单（支持支付宝/微信/余额，由前端提交渠道后通过
        //    /pay/order/submit 接口进行渠道支付）
        PayOrderCreateReqDTO createReqDTO = new PayOrderCreateReqDTO();
        createReqDTO.setAppKey(payAppKey);
        createReqDTO.setUserIp(userIp);
        createReqDTO.setUserId(userId);
        createReqDTO.setUserType(userType != null ? userType : UserTypeEnum.MEMBER.getValue());
        createReqDTO.setMerchantOrderId(merchantOrderId);
        createReqDTO.setSubject(abbreviate(subject, 32));
        createReqDTO.setBody("B2B批发订单 " + merchantOrderId);
        createReqDTO.setPrice(priceInFen);
        createReqDTO.setExpireTime(LocalDateTime.now().plusHours(DEFAULT_EXPIRE_HOURS));

        Long payOrderId = payOrderApi.createOrder(createReqDTO);
        result.setPayOrderId(payOrderId);
        result.setExpireTime(LocalDateTime.now().plusHours(DEFAULT_EXPIRE_HOURS)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.setStatus("WAITING");
        result.setMessage(walletSufficient
                ? "钱包余额充足，可直接余额支付；也可选择支付宝/微信支付"
                : "请通过 /pay/order/submit 接口选择支付渠道完成支付");

        log.info("批发支付单已创建 payOrderId={} merchantOrderId={} price={} walletSufficient={}",
                payOrderId, merchantOrderId, priceInFen, walletSufficient);
        return result;
    }

    @Override
    public WholesalePayResultBO getPayOrder(Long payOrderId) {
        PayOrderRespDTO dto = payOrderApi.getOrder(payOrderId);
        WholesalePayResultBO result = new WholesalePayResultBO();
        if (dto == null) {
            result.setStatus("NOT_FOUND");
            return result;
        }
        result.setPayOrderId(payOrderId);
        result.setMerchantOrderId(dto.getMerchantOrderId());
        result.setPrice(dto.getPrice());
        result.setStatus(dto.getStatus() != null ? dto.getStatus().toString() : "UNKNOWN");
        return result;
    }

    @Override
    public WholesaleRefundResultBO createRefund(Long userId, Integer userType,
                                                 String merchantOrderId, String merchantRefundId,
                                                 Integer refundPrice, String reason, String userIp) {
        PayRefundCreateReqDTO dto = new PayRefundCreateReqDTO();
        dto.setAppKey(payAppKey);
        dto.setUserIp(userIp);
        dto.setUserId(userId);
        dto.setUserType(userType != null ? userType : UserTypeEnum.MEMBER.getValue());
        dto.setMerchantOrderId(merchantOrderId);
        dto.setMerchantRefundId(merchantRefundId);
        dto.setReason(abbreviate(reason, 128));
        dto.setPrice(refundPrice);

        Long refundId = payRefundApi.createRefund(dto);

        WholesaleRefundResultBO result = new WholesaleRefundResultBO();
        result.setRefundId(refundId);
        result.setMerchantOrderId(merchantOrderId);
        result.setMerchantRefundId(merchantRefundId);
        result.setRefundPrice(refundPrice);
        result.setReason(reason);
        result.setStatus("WAITING");
        log.info("批发退款单已创建 refundId={} merchantOrderId={} price={}", refundId, merchantOrderId, refundPrice);
        return result;
    }

    @Override
    public Long createSupplierTransfer(Long supplierId, String transferOrderId,
                                        Integer priceInFen, String userIp) {
        PayTransferCreateReqDTO dto = new PayTransferCreateReqDTO();
        dto.setAppKey(payAppKey);
        dto.setUserIp(userIp);
        dto.setUserId(supplierId);
        dto.setUserType(UserTypeEnum.MEMBER.getValue());
        dto.setMerchantTransferId(transferOrderId);
        dto.setPrice(priceInFen);

        PayTransferCreateRespDTO resp = payTransferApi.createTransfer(dto);
        Long transferId = resp != null ? resp.getId() : null;
        log.info("供应商转账单已创建 transferId={} supplierId={} price={}", transferId, supplierId, priceInFen);
        return transferId;
    }

    // ==================== 工具 ====================

    private static String abbreviate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() <= maxLen ? str : str.substring(0, maxLen);
    }

}
