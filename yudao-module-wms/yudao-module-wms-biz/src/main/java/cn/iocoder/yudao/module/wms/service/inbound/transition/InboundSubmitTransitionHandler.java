package cn.iocoder.yudao.module.wms.service.inbound.transition;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.statemachine.TransitionContext;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:12
 * @description: 提交动作
 */
@Component
public class InboundSubmitTransitionHandler extends BaseInboundTransitionHandler {

//    public InboundSubmitTransition() {
//        // 指定事件以及前后的状态
//        super(
//            // from
//            new WmsInboundAuditStatus[]{
//                WmsInboundAuditStatus.DRAFT,
//                WmsInboundAuditStatus.REJECT
//            },
//            // to
//            WmsInboundAuditStatus.AUDITING,
//            // event
//            WmsInboundAuditStatus.Event.SUBMIT
//        );
//    }

    @Override
    public boolean when(TransitionContext<WmsInboundDO> context) {
        // 检查是否有入库单明细条数
        WmsInboundRespVO inbound = inboundService.getInboundWithItemList(context.data().getId());
        if(CollectionUtils.isEmpty(inbound.getItemList())) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        return super.when(context);
    }
}
