package cn.iocoder.yudao.module.wms.service.inventory.transition;



import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.enums.inventory.WmsInventoryAuditStatus;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:25
 * @description: 同意
 */
@Component
public class InventoryAgreeTransitionHandler extends BaseInventoryTransitionHandler {


    @Override
    public void perform(Integer from, Integer to, WmsInventoryAuditStatus.Event event, TransitionContext<WmsInventoryDO> context) {
        super.perform(from, to, event, context);
    }

}
