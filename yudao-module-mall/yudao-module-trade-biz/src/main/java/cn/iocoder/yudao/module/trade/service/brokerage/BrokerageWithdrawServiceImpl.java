package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.math.Money;
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
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.time.LocalDateTime;
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
        BrokerageWithdrawDO updateObj = new BrokerageWithdrawDO()
                .setStatus(status.getStatus())
                .setAuditReason(auditReason)
                .setAuditTime(LocalDateTime.now());
        int rows = brokerageWithdrawMapper.updateByIdAndStatus(id, BrokerageWithdrawStatusEnum.AUDITING.getStatus(), updateObj);
        if (rows == 0) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        String templateCode = MessageTemplateConstants.BROKERAGE_WITHDRAW_AUDIT_APPROVE;
        if (BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.equals(status)) {
            // 3.1 通过时佣金转余额
            if (BrokerageWithdrawTypeEnum.WALLET.getType().equals(withdraw.getType())) {
                // todo
            }
        } else if (BrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            templateCode = MessageTemplateConstants.BROKERAGE_WITHDRAW_AUDIT_REJECT;

            // 3.2 驳回时需要退还用户佣金
            brokerageRecordService.addBrokerage(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        } else {
            throw new IllegalArgumentException("不支持的提现状态");
        }

        // 4. 通知用户
        Map<String, Object> templateParams = MapUtil.<String, Object>builder()
                .put("createTime", LocalDateTimeUtil.formatNormal(withdraw.getCreateTime()))
                .put("price", new Money(0, withdraw.getPrice()).toString())
                .put("reason", withdraw.getAuditReason())
                .build();
        NotifySendSingleToUserReqDTO reqDTO = new NotifySendSingleToUserReqDTO()
                .setUserId(withdraw.getUserId())
                .setTemplateCode(templateCode).setTemplateParams(templateParams);
        notifyMessageSendApi.sendSingleMessageToMember(reqDTO);
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
    public Long createBrokerageWithdraw(AppBrokerageWithdrawCreateReqVO createReqVO, Long userId) {
        // 校验提现金额
        TradeConfigDO tradeConfig = validateWithdrawPrice(createReqVO.getPrice());
        // 校验提现参数
        createReqVO.validate(validator);

        // 计算手续费
        Integer feePrice = calculateFeePrice(createReqVO.getPrice(), tradeConfig.getBrokerageWithdrawFeePercent());
        // 创建佣金提现记录
        BrokerageWithdrawDO withdraw = BrokerageWithdrawConvert.INSTANCE.convert(createReqVO, userId, feePrice);
        brokerageWithdrawMapper.insert(withdraw);

        // 创建用户佣金记录
        brokerageRecordService.addBrokerage(userId, BrokerageRecordBizTypeEnum.WITHDRAW, String.valueOf(withdraw.getId()),
                -createReqVO.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW.getTitle());

        return withdraw.getId();
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
            throw exception(BROKERAGE_WITHDRAW_MIN_PRICE, new Money(0, tradeConfig.getBrokerageWithdrawMinPrice()));
        }
        return tradeConfig;
    }
}
