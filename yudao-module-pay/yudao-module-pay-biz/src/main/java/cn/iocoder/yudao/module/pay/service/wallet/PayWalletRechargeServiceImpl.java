package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge.AppPayWalletRechargeCreateReqVO;
import cn.iocoder.yudao.module.pay.convert.wallet.PayWalletRechargeConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargeDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletRechargeMapper;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 钱包充值 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayWalletRechargeServiceImpl implements PayWalletRechargeService {
    /**
     * TODO 放到 配置文件中
     */
    private static final Long WALLET_PAY_APP_ID = 8L;

    private static final String WALLET_RECHARGE_ORDER_SUBJECT = "钱包余额充值";

    @Resource
    private PayWalletRechargeMapper walletRechargeMapper;

    @Resource
    private PayWalletService payWalletService;
    @Resource
    private PayOrderService payOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletRechargeDO createWalletRecharge(Long userId, Integer userType,
                                                    AppPayWalletRechargeCreateReqVO createReqVO) {
        // 1. 新增钱包充值记录
        PayWalletDO wallet = payWalletService.getOrCreateWallet(userId, userType);
        PayWalletRechargeDO walletRecharge = PayWalletRechargeConvert.INSTANCE.convert(wallet.getId(), createReqVO);
        walletRechargeMapper.insert(walletRecharge);

        // 2.1 创建支付单
        Long payOrderId = payOrderService.createOrder(new PayOrderCreateReqDTO()
                .setAppId(WALLET_PAY_APP_ID).setUserIp(getClientIP())
                .setMerchantOrderId(walletRecharge.getId().toString()) // 业务的订单编号
                .setSubject(WALLET_RECHARGE_ORDER_SUBJECT).setBody("").setPrice(walletRecharge.getPayPrice())
                .setExpireTime(addTime(Duration.ofHours(2L))));
        // 2.2 更新钱包充值记录中支付订单
        walletRechargeMapper.updateById(new PayWalletRechargeDO().setPayOrderId(payOrderId)
                .setId(walletRecharge.getId()));
        // TODO @jason：是不是你直接返回就好啦；然后 payOrderId 设置下；
        return walletRechargeMapper.selectById(walletRecharge.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWalletRechargerPaid(Long id, Long payOrderId) {
        // 1.1 获取钱包充值记录
        PayWalletRechargeDO walletRecharge = walletRechargeMapper.selectById(id);
        if (walletRecharge == null) {
            log.error("[updateWalletRechargerPaid][钱包充值记录不存在，钱包充值记录 id({})]", id);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        // 1.2 校验钱包充值是否可以支付
        PayOrderDO payOrderDO = validateWalletRechargerCanPaid(walletRecharge, payOrderId);

        // 2. 更新钱包充值的支付状态
        int updateCount = walletRechargeMapper.updateByIdAndPaid(id,false,
                new PayWalletRechargeDO().setId(id).setPayStatus(true).setPayTime(LocalDateTime.now())
                .setPayChannelCode(payOrderDO.getChannelCode()));
        if (updateCount == 0) {
            throw exception(WALLET_RECHARGE_UPDATE_PAID_STATUS_NOT_UNPAID);
        }

        // 3. 更新钱包余额
        // TODO @jason：这样的话，未来提现会不会把充值的，也提现走哈。类似先充 100，送 110；然后提现 110；
        payWalletService.addWalletBalance(walletRecharge.getWalletId(), String.valueOf(id),
                PayWalletBizTypeEnum.RECHARGE, walletRecharge.getPrice());
    }

    private PayOrderDO validateWalletRechargerCanPaid(PayWalletRechargeDO walletRecharge, Long payOrderId) {
        // 1.1 校验充值记录的支付状态
        if (walletRecharge.getPayStatus()) {
            log.error("[validateWalletRechargerCanPaid][钱包({}) 不处于未支付状态!  钱包数据是：{}]",
                    walletRecharge.getId(), toJsonString(walletRecharge));
            throw exception(WALLET_RECHARGE_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
        // 1.2 校验支付订单匹配
        if (notEqual(walletRecharge.getPayOrderId(), payOrderId)) { // 支付单号
            log.error("[validateWalletRechargerCanPaid][钱包({}) 支付单不匹配({})，请进行处理！ 钱包数据是：{}]",
                    walletRecharge.getId(), payOrderId, toJsonString(walletRecharge));
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        }

        // 2.1 校验支付单是否存在
        PayOrderDO payOrder = payOrderService.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validateWalletRechargerCanPaid][钱包({}) payOrder({}) 不存在，请进行处理！]",
                    walletRecharge.getId(), payOrderId);
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        // 2.2 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validateWalletRechargerCanPaid][钱包({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]",
                    walletRecharge.getId(), payOrderId, toJsonString(payOrder));
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 2.3 校验支付金额一致
        if (notEqual(payOrder.getPrice(), walletRecharge.getPayPrice())) {
            log.error("[validateDemoOrderCanPaid][钱包({}) payOrder({}) 支付金额不匹配，请进行处理！钱包 数据是：{}，payOrder 数据是：{}]",
                    walletRecharge.getId(), payOrderId, toJsonString(walletRecharge), toJsonString(payOrder));
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_PRICE_NOT_MATCH);
        }
        // 2.4 校验支付订单的商户订单匹配
        if (notEqual(payOrder.getMerchantOrderId(), walletRecharge.getId().toString())) {
            log.error("[validateDemoOrderCanPaid][钱包({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]",
                    walletRecharge.getId(), payOrderId, toJsonString(payOrder));
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

}
