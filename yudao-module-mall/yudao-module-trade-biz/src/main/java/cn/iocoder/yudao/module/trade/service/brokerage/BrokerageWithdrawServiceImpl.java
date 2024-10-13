package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.api.wallet.PayWalletApi;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletCreateReqDto;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferTypeEnum;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.BrokerageWithdrawConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageWithdrawMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.*;

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
    private TradeNoRedisDAO tradeNoRedisDAO;

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
                PayWalletRespDTO wallet = payWalletApi.getWalletByUserId(withdraw.getUserId());
                payWalletApi.addWallet(new PayWalletCreateReqDto()
                        .setWalletId(wallet.getId())
                        .setBizType(PayWalletBizTypeEnum.WITHDRAW)
                        .setBizId(withdraw.getId().toString())
                        .setPrice(withdraw.getPrice())
                        .setTitle("分佣提现"));
                rows = brokerageWithdrawMapper.updateByIdAndStatus(id, BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
                        new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()).setAuditReason(auditReason).setAuditTime(LocalDateTime.now()));
                if (rows == 0) {
                    throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
                }
            }else if (BrokerageWithdrawTypeEnum.ALIPAY_SMALL.getType().equals(withdraw.getType())){
                //获取openid
                SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(UserTypeEnum.MEMBER.getValue(), withdraw.getUserId(), SocialTypeEnum.WECHAT_MINI_APP.getType());
                // 微信提现
                PayTransferCreateReqDTO payTransferCreateReqDTO = getPayTransferCreateReqDTO(userIp, withdraw, socialUser);
                payTransferApi.createTransfer(payTransferCreateReqDTO);
            }
        } else if (BrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            templateCode = MessageTemplateConstants.SMS_BROKERAGE_WITHDRAW_AUDIT_REJECT;
            // 3.2 驳回时需要退还用户佣金
            brokerageRecordService.addBrokerage(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        } else {
            throw new IllegalArgumentException("不支持的提现状态：" + status);
        }

        // 4. 通知用户
//        Map<String, Object> templateParams = MapUtil.<String, Object>builder()
//                .put("createTime", LocalDateTimeUtil.formatNormal(withdraw.getCreateTime()))
//                .put("price", MoneyUtils.fenToYuanStr(withdraw.getPrice()))
//                .put("reason", auditReason)
//                .build();
//        notifyMessageSendApi.sendSingleMessageToMember(new NotifySendSingleToUserReqDTO()
//                .setUserId(withdraw.getUserId()).setTemplateCode(templateCode).setTemplateParams(templateParams));
    }

    private PayTransferCreateReqDTO getPayTransferCreateReqDTO(String userIp, BrokerageWithdrawDO withdraw, SocialUserRespDTO socialUser) {
        PayTransferCreateReqDTO payTransferCreateReqDTO = new PayTransferCreateReqDTO();
        payTransferCreateReqDTO.setAppKey(tradeOrderProperties.getPayAppKey());
        payTransferCreateReqDTO.setChannelCode("wx_lite");
        payTransferCreateReqDTO.setUserIp(userIp);
        payTransferCreateReqDTO.setType(PayTransferTypeEnum.WX_BALANCE.getType());
        payTransferCreateReqDTO.setMerchantTransferId(withdraw.getId().toString());
        payTransferCreateReqDTO.setPrice(withdraw.getPrice());
        payTransferCreateReqDTO.setSubject("佣金提现");
        payTransferCreateReqDTO.setOpenid(socialUser.getOpenid());
        return payTransferCreateReqDTO;
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

    @Override
    public List<BrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                               BrokerageWithdrawStatusEnum status) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return brokerageWithdrawMapper.selectCountAndSumPriceByUserIdAndStatus(userIds, status.getStatus());
    }

    @Override
    public void updateTransfer(Long id, Long payTransferId) {
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        PayTransferRespDTO transfer = payTransferApi.getTransfer(payTransferId);
        if(PayTransferStatusEnum.isSuccess(transfer.getStatus())){
            withdraw.setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus());
        }else if(PayTransferStatusEnum.isPendingStatus(transfer.getStatus())){
            withdraw.setStatus(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus());
        }else{
            withdraw.setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus());
            // 3.2 驳回时需要退还用户佣金
            brokerageRecordService.addBrokerage(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        }
        brokerageWithdrawMapper.updateById(withdraw);
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
