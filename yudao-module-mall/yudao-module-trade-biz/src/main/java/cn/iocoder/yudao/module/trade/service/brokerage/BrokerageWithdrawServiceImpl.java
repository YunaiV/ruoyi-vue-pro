package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.api.wallet.PayWalletApi;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageWithdrawMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.PAY_TRANSFER_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 佣金提现 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class BrokerageWithdrawServiceImpl implements BrokerageWithdrawService {

    @Resource
    private BrokerageWithdrawMapper brokerageWithdrawMapper;

    @Resource
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private TradeConfigService tradeConfigService;

    @Resource
    private PayTransferApi payTransferApi;
    @Resource
    private PayWalletApi payWalletApi;

    @Resource
    private Validator validator;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBrokerageWithdraw(Long id, BrokerageWithdrawStatusEnum status, String auditReason, String userIp) {
        // 1.1 校验存在
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        // 1.2 特殊：【重新转账】如果是提现失败，并且状态是审核中，那么更新状态为审核中，并且清空 transferErrorMsg
        if (BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus().equals(withdraw.getStatus())) {
            int updateCount = brokerageWithdrawMapper.updateByIdAndStatus(id, withdraw.getStatus(),
                    new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.AUDITING.getStatus()).setTransferErrorMsg(""));
            if (updateCount == 0) {
                throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
            }
            withdraw.setStatus(BrokerageWithdrawStatusEnum.AUDITING.getStatus()).setTransferErrorMsg("");
        }
        // 1.2 校验状态为审核中
        if (ObjectUtil.notEqual(BrokerageWithdrawStatusEnum.AUDITING.getStatus(), withdraw.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 2. 更新状态
        int updateCount = brokerageWithdrawMapper.updateByIdAndStatus(id, withdraw.getStatus(),
                new BrokerageWithdrawDO().setStatus(status.getStatus()).setAuditReason(auditReason).setAuditTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 3.1 审批通过的后续处理
        if (BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.equals(status)) {
            auditBrokerageWithdrawSuccess(withdraw);
            // 3.2 审批不通过的后续处理
        } else if (BrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            brokerageRecordService.addBrokerage(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        } else {
            throw new IllegalArgumentException("不支持的提现状态：" + status);
        }
    }

    private void auditBrokerageWithdrawSuccess(BrokerageWithdrawDO withdraw) {
        // 情况一：通过 API 转账
        if (BrokerageWithdrawTypeEnum.isApi(withdraw.getType())) {
            createPayTransfer(withdraw);
            return;
        }

        // 情况二：非 API 转账（手动打款）
        brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
                new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()));
    }

    private void createPayTransfer(BrokerageWithdrawDO withdraw) {
        // 1.1 获取基础信息
        String userAccount = withdraw.getUserAccount();
        String userName = withdraw.getUserName();
        String channelCode = null;
        Map<String, String> channelExtras = null;
        if (Objects.equal(withdraw.getType(), BrokerageWithdrawTypeEnum.ALIPAY_API.getType())) {
            channelCode = PayChannelEnum.ALIPAY_PC.getCode();
        } else if (Objects.equal(withdraw.getType(), BrokerageWithdrawTypeEnum.WECHAT_API.getType())) {
            channelCode = withdraw.getTransferChannelCode();
            userAccount = withdraw.getUserAccount();
            // 特殊：微信需要有报备信息
            channelExtras = PayTransferCreateReqDTO.buildWeiXinChannelExtra1000("佣金提现", "佣金提现");
        } else if (Objects.equal(withdraw.getType(), BrokerageWithdrawTypeEnum.WALLET.getType())) {
            PayWalletRespDTO wallet = payWalletApi.getOrCreateWallet(withdraw.getUserId(), UserTypeEnum.MEMBER.getValue());
            Assert.notNull(wallet, "钱包不存在");
            channelCode = PayChannelEnum.WALLET.getCode();
            userAccount = wallet.getId().toString();
        }
        // 1.2 构建请求
        PayTransferCreateReqDTO transferReqDTO = new PayTransferCreateReqDTO()
                .setAppKey(tradeOrderProperties.getPayAppKey()).setChannelCode(channelCode)
                .setMerchantTransferId(withdraw.getId().toString()).setSubject("佣金提现").setPrice(withdraw.getPrice())
                .setUserAccount(userAccount).setUserName(userName).setUserIp(getClientIP())
                .setChannelExtras(channelExtras);
        // 1.3 发起请求
        PayTransferCreateRespDTO transferRespDTO = payTransferApi.createTransfer(transferReqDTO);

        // 2. 更新提现记录
        brokerageWithdrawMapper.updateById(new BrokerageWithdrawDO().setId(withdraw.getId())
                .setPayTransferId(transferRespDTO.getId()).setTransferChannelCode(channelCode));
    }

    private BrokerageWithdrawDO validateBrokerageWithdrawExists(Long id) {
        BrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        return withdraw;
    }

    @Override
    public BrokerageWithdrawDO getBrokerageWithdraw(Long id) {
        return brokerageWithdrawMapper.selectById(id);
    }

    @Override
    public PageResult<BrokerageWithdrawDO> getBrokerageWithdrawPage(BrokerageWithdrawPageReqVO pageReqVO) {
        return brokerageWithdrawMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBrokerageWithdraw(Long userId, AppBrokerageWithdrawCreateReqVO createReqVO) {
        // 1.1 校验提现金额
        TradeConfigDO tradeConfig = validateWithdrawPrice(createReqVO.getPrice());
        // 1.2 校验提现参数
        createReqVO.validate(validator);

        // 2.1 计算手续费
        Integer feePrice = calculateFeePrice(createReqVO.getPrice(), tradeConfig.getBrokerageWithdrawFeePercent());
        // 2.2 创建佣金提现记录
        BrokerageWithdrawDO withdraw = BeanUtils.toBean(createReqVO, BrokerageWithdrawDO.class)
                .setUserId(userId).setFeePrice(feePrice);
        brokerageWithdrawMapper.insert(withdraw);

        // 3. 创建用户佣金记录
        // 注意，佣金是否充足，reduceBrokerage 已经进行校验
        brokerageRecordService.reduceBrokerage(userId, BrokerageRecordBizTypeEnum.WITHDRAW, String.valueOf(withdraw.getId()),
                createReqVO.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW.getTitle());
        return withdraw.getId();
    }

    /**
     * 计算提现手续费
     *
     * @param withdrawPrice 提现金额
     * @param percent       手续费百分比
     * @return 提现手续费
     */
    private Integer calculateFeePrice(Integer withdrawPrice, Integer percent) {
        Integer feePrice = 0;
        if (percent != null && percent > 0) {
            feePrice = MoneyUtils.calculateRatePrice(withdrawPrice, Double.valueOf(percent));
        }
        return feePrice;
    }

    /**
     * 校验提现金额要求
     *
     * @param withdrawPrice 提现金额
     * @return 分销配置
     */
    private TradeConfigDO validateWithdrawPrice(Integer withdrawPrice) {
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig.getBrokerageWithdrawMinPrice() != null && withdrawPrice < tradeConfig.getBrokerageWithdrawMinPrice()) {
            throw exception(BROKERAGE_WITHDRAW_MIN_PRICE, MoneyUtils.fenToYuanStr(tradeConfig.getBrokerageWithdrawMinPrice()));
        }
        return tradeConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrokerageWithdrawTransferred(Long id, Long payTransferId) {
        // 1.1 校验提现单是否存在
        BrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(id);
        if (withdraw == null) {
            log.error("[updateBrokerageWithdrawTransferred][withdraw({}) payTransfer({}) 不存在提现单，请进行处理！]", id, payTransferId);
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        // 1.2 校验提现单已经结束（成功或失败）
        if (ObjectUtils.equalsAny(withdraw.getStatus(), BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus(),
                BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus())) {
            // 特殊：转账单编号相同，直接返回，说明重复回调
            if (ObjectUtil.equal(withdraw.getPayTransferId(), payTransferId)) {
                log.warn("[updateBrokerageWithdrawTransferred][withdraw({}) 已结束，且转账单编号相同({})，直接返回]", withdraw, payTransferId);
                return;
            }
            // 异常：转账单编号不同，说明转账单编号错误
            log.error("[updateBrokerageWithdrawTransferred][withdraw({}) 转账单不匹配({})，请进行处理！]", withdraw, payTransferId);
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR);
        }

        // 2. 校验转账单的合法性
        PayTransferRespDTO payTransfer = validateBrokerageTransferStatusCanUpdate(withdraw, payTransferId);

        // 3. 更新提现单状态
        Integer newStatus = PayTransferStatusEnum.isSuccess(payTransfer.getStatus()) ? BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus() :
                PayTransferStatusEnum.isClosed(payTransfer.getStatus()) ? BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus() : null;
        Assert.notNull(newStatus, "转账单状态({}) 不合法", payTransfer.getStatus());
        brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), withdraw.getStatus(),
                new BrokerageWithdrawDO().setStatus(newStatus)
                        .setTransferTime(payTransfer.getSuccessTime())
                        .setTransferErrorMsg(payTransfer.getChannelErrorMsg()));
    }

    private PayTransferRespDTO validateBrokerageTransferStatusCanUpdate(BrokerageWithdrawDO withdraw, Long payTransferId) {
        // 1. 校验转账单是否存在
        PayTransferRespDTO payTransfer = payTransferApi.getTransfer(payTransferId);
        if (payTransfer == null) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 不存在，请进行处理！]", withdraw.getId(), payTransferId);
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }

        // 2.1 校验转账单已成功或关闭
        if (!PayTransferStatusEnum.isSuccessOrClosed(payTransfer.getStatus())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 未结束，请进行处理！payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_STATUS_NOT_SUCCESS_OR_CLOSED);
        }
        // 2.2 校验转账金额一致
        if (ObjectUtil.notEqual(payTransfer.getPrice(), withdraw.getPrice())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 转账金额不匹配，请进行处理！withdraw 数据是：{}，payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(withdraw), JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 2.3 校验转账订单匹配
        if (ObjectUtil.notEqual(payTransfer.getMerchantTransferId(), withdraw.getId().toString())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) 转账单不匹配({})，请进行处理！payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_MERCHANT_EXISTS);
        }
        // 2.4 校验转账渠道一致
        if (ObjectUtil.notEqual(payTransfer.getChannelCode(), withdraw.getTransferChannelCode())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 转账渠道不匹配，请进行处理！withdraw 数据是：{}，payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(withdraw), JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_CHANNEL_NOT_MATCH);
        }
        return payTransfer;
    }

    @Override
    public List<BrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                               Collection<BrokerageWithdrawStatusEnum> statuses) {
        if (CollUtil.isEmpty(userIds) || CollUtil.isEmpty(statuses)) {
            return Collections.emptyList();
        }
        return brokerageWithdrawMapper.selectCountAndSumPriceByUserIdAndStatus(userIds,
                convertSet(statuses, BrokerageWithdrawStatusEnum::getStatus));
    }

}
