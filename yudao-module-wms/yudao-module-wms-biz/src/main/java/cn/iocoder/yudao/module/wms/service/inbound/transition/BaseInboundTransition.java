package cn.iocoder.yudao.module.wms.service.inbound.transition;



import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryTransition;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:17
 * @description: InboundAction 基类
 */
public class BaseInboundTransition extends ApprovalHistoryTransition<WmsInboundAuditStatus.Event, WmsInboundDO> {

    @Resource
    @Lazy
    protected WmsInboundService inboundService;

    public BaseInboundTransition(WmsInboundAuditStatus[] from, WmsInboundAuditStatus to, WmsInboundAuditStatus.Event event) {
        super(Arrays.stream(from).map(WmsInboundAuditStatus::getValue).toArray(Integer[]::new), to.getValue(), WmsInboundDO::getAuditStatus, event);
    }

    public BaseInboundTransition(WmsInboundAuditStatus from, WmsInboundAuditStatus to, WmsInboundAuditStatus.Event event) {
        super(new Integer[]{from.getValue()}, to.getValue(), WmsInboundDO::getAuditStatus, event);
    }

    /**
     * 默认的条件判断都为通过
     **/
    @Override
    public boolean when(ColaContext<WmsInboundDO> context) {
        return context.success();
    }

    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, WmsInboundAuditStatus.Event event, ColaContext<WmsInboundDO> context) {
        super.perform(from, to, event, context);
        // 变更状态值
        WmsInboundDO inboundDO = context.data();
        inboundDO.setAuditStatus(to);
        inboundService.updateInboundAuditStatus(inboundDO.getId(),to);
    }

}
