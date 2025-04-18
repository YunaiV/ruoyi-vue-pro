package cn.iocoder.yudao.module.wms.service.inventory.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.enums.inventory.WmsInventoryAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:23
 * @description: InventoryTransition 基类
 */
public class BaseInventoryTransitionHandler extends ApprovalHistoryTransitionHandler<WmsInventoryAuditStatus.Event, WmsInventoryDO> {

    @Resource
    @Lazy
    protected WmsInventoryService inventoryDOService;

    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, WmsInventoryAuditStatus.Event event, TransitionContext<WmsInventoryDO> context) {
        super.perform(from, to, event, context);
        WmsInventoryDO inboundDO = context.data();
        inboundDO.setAuditStatus(to);
        inventoryDOService.updateOutboundAuditStatus(inboundDO.getId(),to);
    }

}
