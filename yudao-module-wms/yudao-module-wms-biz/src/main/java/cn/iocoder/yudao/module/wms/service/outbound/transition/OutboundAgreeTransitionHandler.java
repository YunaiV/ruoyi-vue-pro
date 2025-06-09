package cn.iocoder.yudao.module.wms.service.outbound.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.purchase.SrmPurchaseReturnApi;
import cn.iocoder.yudao.module.srm.api.purchase.dto.req.SrmReturnSaveItemReqDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.req.SrmReturnSaveReqDTO;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.api.transfer.TmsTransferApi;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:25
 * @description: 同意
 */
@Component
public class OutboundAgreeTransitionHandler extends BaseOutboundTransitionHandler {

    @Resource
    private SrmPurchaseReturnApi srmPurchaseReturnApi;

    @Resource
    private TmsTransferApi tmsTransferApi;

    @Override
    public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, TransitionContext<WmsOutboundDO> context) {
        super.perform(from, to, event, context);
        WmsOutboundRespVO outboundVO = outboundService.getOutboundWithItemList(context.data().getId());

        if (outboundVO.getUpstreamType() == null) {
            return;
        }
        //更新SRM退货状态机
        if (outboundVO.getUpstreamType().equals(BillType.SRM_PURCHASE_RETURN.getValue())) {
            SrmReturnSaveReqDTO srmReturnSaveReqDTO = BeanUtils.toBean(outboundVO, SrmReturnSaveReqDTO.class);
            srmReturnSaveReqDTO.setItems(BeanUtils.toBean(outboundVO.getItemList(), SrmReturnSaveItemReqDTO.class));
            srmPurchaseReturnApi.updatePurchaseReturnItemQty(srmReturnSaveReqDTO);
        }
    }
}
