package cn.iocoder.yudao.module.wms.service.inbound.action;


import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.config.statemachine.ColaAction;
import cn.iocoder.yudao.module.wms.config.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import jakarta.annotation.Resource;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 17:32
 * @description:
 */
public class InboundSubmitAction extends ColaAction<Integer, InboundStatus.Event, WmsInboundDO> {

    public static final String STATE_MACHINE_NAME = "purchaseRequestStateMachine";

    @Resource
    private WmsInboundService inboundService;


    public InboundSubmitAction() {
        // 指定事件以及前后的状态与状态提取器
        super(InboundStatus.DRAFT.getValue(), InboundStatus.AUDIT.getValue(),WmsInboundDO::getStatus,InboundStatus.Event.SUBMIT);
    }


    /**
     * 触发前的判断
     **/
    @Override
    public boolean when(ColaContext<WmsInboundDO> context) {
        // 事前校验
        WmsInboundDO purchaseRequestDO=context.data();

        return context.success();
    }

    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, InboundStatus.Event event, ColaContext<WmsInboundDO> context) {
        // 逻辑处理
        WmsInboundDO inboundDO=context.data();
        // 设置新的状态
//        purchaseRequestDO.setAuditStatus(to);
//        for (PurchaseRequestItemDO item : purchaseRequestDO.getItems()) {
//            item.setAuditStatus(PurchaseRequestItemAuditStatus.UNAUDITED);
//        }
        // 保存
        inboundService.updateInbound(BeanUtils.toBean(inboundDO, WmsInboundSaveReqVO.class));
    }


}
