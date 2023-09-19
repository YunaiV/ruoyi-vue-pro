package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageWithdrawMapper;
import cn.iocoder.yudao.module.trade.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.BROKERAGE_WITHDRAW_NOT_EXISTS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING;

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
    private NotifyMessageSendApi notifyMessageSendApi;

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

        // 3. 驳回时需要退还用户佣金
        String templateCode = MessageTemplateConstants.BROKERAGE_WITHDRAW_AUDIT_APPROVE;
        if (BrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            templateCode = MessageTemplateConstants.BROKERAGE_WITHDRAW_AUDIT_REJECT;

            // todo @owen
//            brokerageRecordService.addBrokerage(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW, withdraw.getPrice(), "");
        }

        // 4. 通知用户
        Map<String, Object> templateParams = MapUtil.<String, Object>builder()
                .put("createTime", LocalDateTimeUtil.formatNormal(withdraw.getCreateTime()))
                .put("price", String.format("%.2f", withdraw.getPrice() / 100d))
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

}
