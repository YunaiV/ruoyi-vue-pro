package cn.iocoder.yudao.module.tms.config.transfer.impl;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.vo.TmsTransferAuditReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.TmsTransferDO;
import cn.iocoder.yudao.module.tms.dal.mysql.transfer.TmsTransferMapper;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsAuditStatus;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class TmsTransferImpl implements Action<TmsAuditStatus, TmsEventEnum, TmsTransferAuditReqVO> {

    @Autowired
    @Lazy
    private TmsTransferMapper tmsTransferMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(TmsAuditStatus from, TmsAuditStatus to, TmsEventEnum event, TmsTransferAuditReqVO context) {

        TmsTransferDO transfer = tmsTransferMapper.selectById(context.getId());
        if (event == TmsEventEnum.AGREE || event == TmsEventEnum.REJECT) {
            //审核通过?不通过，需要有审核意见
            transfer.setAuditAdvice(context.getAuditAdvice());
        }
        //反审核的时候，审核意见制空
        if (event == TmsEventEnum.WITHDRAW_REVIEW) {
            transfer.setAuditAdvice(null);
        }

        //更新状态
        tmsTransferMapper.updateById(transfer.setAuditStatus(to.getCode()));
        log.debug("调拨审核状态机，ID: {},起始状态{} -> 目标状态{}", transfer.getId(), from.getDesc(), to.getDesc());
    }
}
