package cn.iocoder.yudao.module.tms.config.first.mile.request.impl.action;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestAuditReqVO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsAuditStatus;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class RequestAuditActionImpl implements Action<TmsAuditStatus, TmsEventEnum, TmsFirstMileRequestAuditReqVO> {

    @Autowired
    @Lazy
    TmsFirstMileRequestService firstMileRequestService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(TmsAuditStatus from, TmsAuditStatus to, TmsEventEnum event, TmsFirstMileRequestAuditReqVO context) {

        firstMileRequestService.validateFirstMileRequestExists(context.getRequestId());

        firstMileRequestService.updateFirstMileRequestStatus(context.getRequestId(), null, null, to.getCode(), context.getAuditAdvice());
    }
}
