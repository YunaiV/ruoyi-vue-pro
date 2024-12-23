package cn.iocoder.yudao.module.pay.service.wallet;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge.AppPayWalletRechargeCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargeDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargePackageDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletRechargeMapper;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.fenToYuanStr;
import static cn.iocoder.yudao.module.pay.convert.wallet.PayWalletRechargeConvert.INSTANCE;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.pay.enums.MessageTemplateConstants.WXA_WALLET_RECHARGER_PAID;
import static cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum.*;

/**
 * 钱包充值 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayWalletRechargeServiceImpl implements PayWalletRechargeService {

    private static final String WALLET_RECHARGE_ORDER_SUBJECT = "钱包余额充值";

    @Resource
    private PayWalletRechargeMapper walletRechargeMapper;
    @Resource
    private PayWalletService payWalletService;
    @Resource
    private PayOrderService payOrderService;
    @Resource
    private PayRefundService payRefundService;
    @Resource
    private PayWalletRechargePackageService payWalletRechargePackageService;

    @Resource
    public SocialClientApi socialClientApi;

    @Resource
    private PayProperties payProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletRechargeDO createWalletRecharge(Long userId, Integer userType, String userIp,
                                                    AppPayWalletRechargeCreateReqVO reqVO) {
        // 1.1 计算充值金额
        int payPrice;
        int bonusPrice = 0;
        if (Objects.nonNull(reqVO.getPackageId())) {
            PayWalletRechargePackageDO rechargePackage = payWalletRechargePackageService.validWalletRechargePackage(reqVO.getPackageId());
            payPrice = rechargePackage.getPayPrice();
            bonusPrice = rechargePackage.getBonusPrice();
        } else {
            payPrice = reqVO.getPayPrice();
        }
        // 1.2 插入充值记录
        PayWalletDO wallet = payWalletService.getOrCreateWallet(userId, userType);
        PayWalletRechargeDO recharge = INSTANCE.convert(wallet.getId(), payPrice, bonusPrice, reqVO.getPackageId());
        walletRechargeMapper.insert(recharge);

        // 2.1 创建支付单
        Long payOrderId = payOrderService.createOrder(new PayOrderCreateReqDTO()
                .setAppKey(payProperties.getWalletPayAppKey()).setUserIp(userIp)
                .setMerchantOrderId(recharge.getId().toString()) // 业务的订单编号
                .setSubject(WALLET_RECHARGE_ORDER_SUBJECT).setBody("")
                .setPrice(recharge.getPayPrice())
                .setExpireTime(addTime(Duration.ofHours(2L)))); // TODO @芋艿：支付超时时间
        // 2.2 更新钱包充值记录中支付订单
        walletRechargeMapper.updateById(new PayWalletRechargeDO().setId(recharge.getId()).setPayOrderId(payOrderId));
        recharge.setPayOrderId(payOrderId);
        return recharge;
    }

    @Override
    public PageResult<PayWalletRechargeDO> getWalletRechargePackagePage(Long userId, Integer userType,
                                                                        PageParam pageReqVO, Boolean payStatus) {
        PayWalletDO wallet = payWalletService.getOrCreateWallet(userId, userType);
        return walletRechargeMapper.selectPage(pageReqVO, wallet.getId(), payStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWalletRechargerPaid(Long id, Long payOrderId) {
        // 1.1 校验钱包充值是否存在
        PayWalletRechargeDO recharge = walletRechargeMapper.selectById(id);
        if (recharge == null) {
            log.error("[updateWalletRechargerPaid][recharge({}) payOrder({}) 不存在充值订单，请进行处理！]", id, payOrderId);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        // 1.2 校验钱包充值是否可以支付
        if (recharge.getPayStatus()) {
            // 特殊：如果订单已支付，且支付单号相同，直接返回，说明重复回调
            if (ObjectUtil.equals(recharge.getPayOrderId(), payOrderId)) {
                log.warn("[updateWalletRechargerPaid][recharge({}) 已支付，且支付单号相同({})，直接返回]", recharge, payOrderId);
                return;
            }
            // 异常：支付单号不同，说明支付单号错误
            log.error("[updateWalletRechargerPaid][recharge({}) 已支付，但是支付单号不同({})，请进行处理！]", recharge, payOrderId);
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        }

        // 2. 校验支付订单的合法性
        PayOrderDO payOrderDO = validatePayOrderPaid(recharge, payOrderId);

        // 3. 更新钱包充值的支付状态
        int updateCount = walletRechargeMapper.updateByIdAndPaid(id, false,
                new PayWalletRechargeDO().setId(id).setPayStatus(true).setPayTime(LocalDateTime.now())
                        .setPayChannelCode(payOrderDO.getChannelCode()));
        if (updateCount == 0) {
            throw exception(WALLET_RECHARGE_UPDATE_PAID_STATUS_NOT_UNPAID);
        }

        // 4. 更新钱包余额
        // TODO @jason：这样的话，未来提现会不会把充值的，也提现走哈。类似先充 100，送 110；然后提现 110；
        // TODO 需要钱包中加个可提现余额
        payWalletService.addWalletBalance(recharge.getWalletId(), String.valueOf(id),
                PayWalletBizTypeEnum.RECHARGE, recharge.getTotalPrice());

        // 5. 发送订阅消息
        getSelf().sendWalletRechargerPaidMessage(payOrderId, recharge);
    }

    @Async
    public void sendWalletRechargerPaidMessage(Long payOrderId, PayWalletRechargeDO walletRecharge) {
        // 1. 获得会员钱包信息
        PayWalletDO wallet = payWalletService.getWallet(walletRecharge.getWalletId());
        // 2. 构建并发送模版消息
        socialClientApi.sendWxaSubscribeMessage(new SocialWxaSubscribeMessageSendReqDTO()
                .setUserId(wallet.getUserId()).setUserType(wallet.getUserType())
                .setTemplateTitle(WXA_WALLET_RECHARGER_PAID)
                .setPage("pages/user/wallet/money") // 钱包详情界面
                .addMessage("character_string1", String.valueOf(payOrderId)) // 支付单编号
                .addMessage("amount2", fenToYuanStr(walletRecharge.getTotalPrice())) // 充值金额
                .addMessage("time3", LocalDateTimeUtil.formatNormal(walletRecharge.getCreateTime())) // 充值时间
                .addMessage("phrase4", "充值成功")); // 充值状态
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundWalletRecharge(Long id, String userIp) {
        // 1.1 获取钱包充值记录
        PayWalletRechargeDO walletRecharge = walletRechargeMapper.selectById(id);
        if (walletRecharge == null) {
            log.error("[refundWalletRecharge][钱包充值记录不存在，钱包充值记录 id({})]", id);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        // 1.2 校验钱包充值是否可以发起退款
        PayWalletDO wallet = validateWalletRechargeCanRefund(walletRecharge);

        // 2. 冻结退款的余额，暂时只处理赠送的余额也全部退回
        payWalletService.freezePrice(wallet.getId(), walletRecharge.getTotalPrice());

        // 3. 创建退款单
        String walletRechargeId = String.valueOf(id);
        String refundId = walletRechargeId + "-refund";
        Long payRefundId = payRefundService.createPayRefund(new PayRefundCreateReqDTO()
                .setAppKey(payProperties.getWalletPayAppKey()).setUserIp(userIp)
                .setMerchantOrderId(walletRechargeId)
                .setMerchantRefundId(refundId)
                .setReason("想退钱").setPrice(walletRecharge.getPayPrice()));

        // 4. 更新充值记录退款单号
        walletRechargeMapper.updateById(new PayWalletRechargeDO().setPayRefundId(payRefundId)
                .setRefundStatus(WAITING.getStatus()).setId(walletRecharge.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWalletRechargeRefunded(Long id, Long payRefundId) {
        // 1.1 获取钱包充值记录
        PayWalletRechargeDO walletRecharge = walletRechargeMapper.selectById(id);
        if (walletRecharge == null) {
            log.error("[updateWalletRechargerPaid][钱包充值记录不存在，钱包充值记录 id({})]", id);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        // 1.2 校验钱包充值是否可以更新已退款
        PayRefundDO payRefund = validateWalletRechargeCanRefunded(walletRecharge, payRefundId);

        PayWalletRechargeDO updateObj = new PayWalletRechargeDO().setId(id);
        // 退款成功
        if (PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            // 2.1 更新钱包余额
            payWalletService.reduceWalletBalance(walletRecharge.getWalletId(), id,
                    PayWalletBizTypeEnum.RECHARGE_REFUND, walletRecharge.getTotalPrice());

            updateObj.setRefundStatus(SUCCESS.getStatus()).setRefundTime(payRefund.getSuccessTime())
                    .setRefundTotalPrice(walletRecharge.getTotalPrice()).setRefundPayPrice(walletRecharge.getPayPrice())
                    .setRefundBonusPrice(walletRecharge.getBonusPrice());
        }
        // 退款失败
        if (PayRefundStatusRespEnum.isFailure(payRefund.getStatus())) {
            // 2.2 解冻余额
            payWalletService.unfreezePrice(walletRecharge.getWalletId(), walletRecharge.getTotalPrice());

            updateObj.setRefundStatus(FAILURE.getStatus());
        }
        // 3. 更新钱包充值的退款字段
        walletRechargeMapper.updateByIdAndRefunded(id, WAITING.getStatus(), updateObj);
    }

    private PayRefundDO validateWalletRechargeCanRefunded(PayWalletRechargeDO walletRecharge, Long payRefundId) {
        // 1. 校验退款订单匹配
        if (notEqual(walletRecharge.getPayRefundId(), payRefundId)) {
            log.error("[validateWalletRechargeCanRefunded][钱包充值({}) 退款单不匹配({})，请进行处理！钱包充值的数据是：{}]",
                    walletRecharge.getId(), payRefundId, toJsonString(walletRecharge));
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }

        // 2.1 校验退款订单
        PayRefundDO payRefund = payRefundService.getRefund(payRefundId);
        if (payRefund == null) {
            log.error("[validateWalletRechargeCanRefunded][payRefund({})不存在]", payRefundId);
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_NOT_FOUND);
        }
        // 2.2 校验退款金额一致
        if (notEqual(payRefund.getRefundPrice(), walletRecharge.getPayPrice())) {
            log.error("[validateWalletRechargeCanRefunded][钱包({}) payRefund({}) 退款金额不匹配，请进行处理！钱包数据是：{}，payRefund 数据是：{}]",
                    walletRecharge.getId(), payRefundId, toJsonString(walletRecharge), toJsonString(payRefund));
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH);
        }
        // 2.3 校验退款订单商户订单是否匹配
        if (notEqual(payRefund.getMerchantOrderId(), walletRecharge.getId().toString())) {
            log.error("[validateWalletRechargeCanRefunded][钱包({}) 退款单不匹配({})，请进行处理！payRefund 数据是：{}]",
                    walletRecharge.getId(), payRefundId, toJsonString(payRefund));
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }
        return payRefund;
    }

    private PayWalletDO validateWalletRechargeCanRefund(PayWalletRechargeDO walletRecharge) {
        // 校验充值订单是否支付
        if (!walletRecharge.getPayStatus()) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_NOT_PAID);
        }
        // 校验充值订单是否已退款
        if (walletRecharge.getPayRefundId() != null) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUNDED);
        }
        // 校验钱包余额是否足够
        PayWalletDO wallet = payWalletService.getWallet(walletRecharge.getWalletId());
        Assert.notNull(wallet, "用户钱包({}) 不存在", wallet.getId());
        if (wallet.getBalance() < walletRecharge.getTotalPrice()) {
            throw exception(WALLET_RECHARGE_REFUND_BALANCE_NOT_ENOUGH);
        }
        // TODO @芋艿：需要考虑下，赠送的金额，会不会导致提现超过；
        return wallet;
    }

    /**
     * 校验支付订单的合法性
     *
     * @param recharge 充值订单
     * @param payOrderId 支付订单编号
     * @return 支付订单
     */
    private PayOrderDO validatePayOrderPaid(PayWalletRechargeDO recharge, Long payOrderId) {
        // 1. 校验支付单是否存在
        PayOrderDO payOrder = payOrderService.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validatePayOrderPaid][充值订单({}) payOrder({}) 不存在，请进行处理！]",
                    recharge.getId(), payOrderId);
            throw exception(PAY_ORDER_NOT_FOUND);
        }

        // 2.1 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validatePayOrderPaid][充值订单({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]",
                    recharge.getId(), payOrderId, toJsonString(payOrder));
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 2.2 校验支付金额一致
        if (notEqual(payOrder.getPrice(), recharge.getPayPrice())) {
            log.error("[validatePayOrderPaid][充值订单({}) payOrder({}) 支付金额不匹配，请进行处理！钱包 数据是：{}，payOrder 数据是：{}]",
                    recharge.getId(), payOrderId, toJsonString(recharge), toJsonString(payOrder));
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_PRICE_NOT_MATCH);
        }
        // 2.3 校验支付订单的商户订单匹配
        if (notEqual(payOrder.getMerchantOrderId(), recharge.getId().toString())) {
            log.error("[validatePayOrderPaid][充值订单({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]",
                    recharge.getId(), payOrderId, toJsonString(payOrder));
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayWalletRechargeServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
