package cn.iocoder.yudao.module.trade.service.brokerage.withdraw;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.withdraw.vo.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.withdraw.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.withdraw.BrokerageWithdrawMapper;
import cn.iocoder.yudao.module.trade.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.record.BrokerageRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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

        // 校验存在
        BrokerageWithdrawDO withdrawDO = validateBrokerageWithdrawExists(id);
        // 校验状态为审核中
        if (!BrokerageWithdrawStatusEnum.AUDITING.getStatus().equals(withdrawDO.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 更新
        BrokerageWithdrawDO updateObj = new BrokerageWithdrawDO()
                .setStatus(status.getStatus())
                .setAuditReason(auditReason)
                .setAuditTime(LocalDateTime.now());
        int rows = brokerageWithdrawMapper.updateByIdAndStatus(id, BrokerageWithdrawStatusEnum.AUDITING.getStatus(), updateObj);
        if (rows == 0) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 驳回时需要退还用户佣金
        String templateCode = MessageTemplateConstants.BROKERAGE_WITHDRAW_AUDIT_APPROVE;
        if (BrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            templateCode = MessageTemplateConstants.BROKERAGE_WITHDRAW_AUDIT_REJECT;

            // todo @owen
//            brokerageRecordService.addBrokerage(withdrawDO.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW, withdrawDO.getPrice(), "");
        }

        // 通知用户
        Map<String, Object> templateParams = MapUtil.<String, Object>builder()
                .put("createTime", LocalDateTimeUtil.formatNormal(withdrawDO.getCreateTime()))
                .put("price", String.format("%.2f", withdrawDO.getPrice() / 100d))
                .put("reason", withdrawDO.getAuditReason())
                .build();
        NotifySendSingleToUserReqDTO reqDTO = new NotifySendSingleToUserReqDTO()
                .setUserId(withdrawDO.getUserId())
                .setTemplateCode(templateCode).setTemplateParams(templateParams);
        notifyMessageSendApi.sendSingleMessageToMember(reqDTO);
    }

    private BrokerageWithdrawDO validateBrokerageWithdrawExists(Integer id) {
        BrokerageWithdrawDO withdrawDO = brokerageWithdrawMapper.selectById(id);
        if (withdrawDO == null) {
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        return withdrawDO;
    }

    @Override
    public BrokerageWithdrawDO getBrokerageWithdraw(Integer id) {
        return brokerageWithdrawMapper.selectById(id);
    }

    @Override
    public List<BrokerageWithdrawDO> getBrokerageWithdrawList(Collection<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return brokerageWithdrawMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<BrokerageWithdrawDO> getBrokerageWithdrawPage(BrokerageWithdrawPageReqVO pageReqVO) {
        return brokerageWithdrawMapper.selectPage(pageReqVO);
    }

}
