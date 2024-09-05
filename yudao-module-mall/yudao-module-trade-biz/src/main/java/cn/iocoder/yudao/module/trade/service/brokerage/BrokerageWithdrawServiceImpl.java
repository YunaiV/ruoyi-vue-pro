package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.BrokerageWithdrawConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageWithdrawMapper;
import cn.iocoder.yudao.module.trade.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 佣金提现 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
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
    private Validator validator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBrokerageWithdraw(Integer id, BrokerageWithdrawStatusEnum status, String auditReason) {
        // 1.1 校验存在
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        // 1.2 校验状态为审核中
        if (ObjectUtil.notEqual(BrokerageWithdrawStatusEnum.AUDITING.getStatus(), withdraw.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 2. 更新
        int rows = brokerageWithdrawMapper.updateByIdAndStatus(id, BrokerageWithdrawStatusEnum.AUDITING.getStatus(),
                new BrokerageWithdrawDO().setStatus(status.getStatus()).setAuditReason(auditReason).setAuditTime(LocalDateTime.now()));
        if (rows == 0) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        String templateCode;
        if (BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.equals(status)) {
            templateCode = MessageTemplateConstants.SMS_BROKERAGE_WITHDRAW_AUDIT_APPROVE;
            // 3.1 通过时佣金转余额
            if (BrokerageWithdrawTypeEnum.WALLET.getType().equals(withdraw.getType())) {
                // todo 疯狂：
            }
            // TODO 疯狂：调用转账接口
        } else if (BrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            templateCode = MessageTemplateConstants.SMS_BROKERAGE_WITHDRAW_AUDIT_REJECT;
            // 3.2 驳回时需要退还用户佣金
            brokerageRecordService.addBrokerage(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        } else {
            throw new IllegalArgumentException("不支持的提现状态：" + status);
        }

        // 4. 通知用户
        Map<String, Object> templateParams = MapUtil.<String, Object>builder()
                .put("createTime", LocalDateTimeUtil.formatNormal(withdraw.getCreateTime()))
                .put("price", MoneyUtils.fenToYuanStr(withdraw.getPrice()))
                .put("reason", auditReason)
                .build();
        notifyMessageSendApi.sendSingleMessageToMember(new NotifySendSingleToUserReqDTO()
                .setUserId(withdraw.getUserId()).setTemplateCode(templateCode).setTemplateParams(templateParams));
    }

    private BrokerageWithdrawDO validateBrokerageWithdrawExists(Integer id) {
        BrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        return withdraw;
    }

    @Override
    public BrokerageWithdrawDO getBrokerageWithdraw(Integer id) {
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

    @Override
    public List<BrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                               BrokerageWithdrawStatusEnum status) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return brokerageWithdrawMapper.selectCountAndSumPriceByUserIdAndStatus(userIds, status.getStatus());
    }

    /**
     * 计算提现手续费
     *
     * @param withdrawPrice 提现金额
     * @param percent       手续费百分比
     * @return 提现手续费
     */
    Integer calculateFeePrice(Integer withdrawPrice, Integer percent) {
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
    TradeConfigDO validateWithdrawPrice(Integer withdrawPrice) {
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig.getBrokerageWithdrawMinPrice() != null && withdrawPrice < tradeConfig.getBrokerageWithdrawMinPrice()) {
            throw exception(BROKERAGE_WITHDRAW_MIN_PRICE, MoneyUtils.fenToYuanStr(tradeConfig.getBrokerageWithdrawMinPrice()));
        }
        return tradeConfig;
    }
}
