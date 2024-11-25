package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.api.wallet.PayWalletApi;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferTypeEnum;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.BrokerageWithdrawConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageWithdrawMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
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

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
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
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private PayTransferApi payTransferApi;
    @Resource
    private SocialUserApi socialUserApi;
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
        // 1.2 校验状态为审核中
        if (ObjectUtil.notEqual(BrokerageWithdrawStatusEnum.AUDITING.getStatus(), withdraw.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 2. 更新状态
        int rows = brokerageWithdrawMapper.updateByIdAndStatus(id, BrokerageWithdrawStatusEnum.AUDITING.getStatus(),
                new BrokerageWithdrawDO().setStatus(status.getStatus()).setAuditReason(auditReason).setAuditTime(LocalDateTime.now()));
        if (rows == 0) {
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
        // 1.1 钱包
        if (BrokerageWithdrawTypeEnum.WALLET.getType().equals(withdraw.getType())) {
            payWalletApi.addWalletBalance(new PayWalletAddBalanceReqDTO()
                    .setUserId(withdraw.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue())
                    .setBizType(PayWalletBizTypeEnum.BROKERAGE_WITHDRAW.getType()).setBizId(withdraw.getId().toString())
                    .setPrice(withdraw.getPrice()));
        // 1.2 微信 API
        } else if (BrokerageWithdrawTypeEnum.WECHAT_API.getType().equals(withdraw.getType())) {
            // TODO @luchi：这里，要加个转账单号的记录；另外，调用 API 转账，是立马成功，还是有延迟的哈？
            Long payTransferId = createPayTransfer(withdraw);
        // 1.3 剩余类型，都是手动打款，所以不处理
        } else {
            // TODO 可优化：未来可以考虑，接入支付宝、银联等 API 转账，实现自动打款
            log.info("[auditBrokerageWithdrawSuccess][withdraw({}) 类型({}) 手动打款，无需处理]", withdraw.getId(), withdraw.getType());
        }

        // 2. 非支付 API，则直接体现成功
        if (!BrokerageWithdrawTypeEnum.isApi(withdraw.getType())) {
            brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
                    new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()));
        }
    }

    private Long createPayTransfer(BrokerageWithdrawDO withdraw) {
        // 1.1 获取微信 openid
        SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(
                UserTypeEnum.MEMBER.getValue(), withdraw.getUserId(), SocialTypeEnum.WECHAT_MINI_APP.getType());
        // TODO @luchi：这里，需要校验非空。如果空的话，要有业务异常哈；
        // 1.2 构建请求
        PayTransferCreateReqDTO payTransferCreateReqDTO = new PayTransferCreateReqDTO()
                .setAppKey(tradeOrderProperties.getPayAppKey())
                .setChannelCode("wx_lite").setType(PayTransferTypeEnum.WX_BALANCE.getType())
                .setMerchantTransferId(withdraw.getId().toString())
                .setPrice(withdraw.getPrice())
                .setSubject("佣金提现")
                .setOpenid(socialUser.getOpenid()).setUserIp(getClientIP());
        // 2. 发起请求
        return payTransferApi.createTransfer(payTransferCreateReqDTO);
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
        BrokerageWithdrawDO withdraw = BrokerageWithdrawConvert.INSTANCE.convert(createReqVO, userId, feePrice);
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
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        PayTransferRespDTO transfer = payTransferApi.getTransfer(payTransferId);
        // TODO @luchi：建议参考支付那，即使成功的情况下，也要各种校验；金额是否匹配、转账单号是否匹配、是否重复调用；
        if (PayTransferStatusEnum.isSuccess(transfer.getStatus())) {
            withdraw.setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus());
            // TODO @luchi：发送站内信
        } else if (PayTransferStatusEnum.isPendingStatus(transfer.getStatus())) {
            // TODO @luchi：这里，是不是不用更新哈？
            withdraw.setStatus(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus());
        } else {
            withdraw.setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus());
            // 3.2 驳回时需要退还用户佣金
            brokerageRecordService.addBrokerage(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        }
        brokerageWithdrawMapper.updateById(withdraw);
    }

    @Override
    public List<BrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                               BrokerageWithdrawStatusEnum status) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return brokerageWithdrawMapper.selectCountAndSumPriceByUserIdAndStatus(userIds, status.getStatus());
    }

}
