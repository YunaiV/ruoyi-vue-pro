package cn.iocoder.yudao.module.wms.service.inbound.transition;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:14
 * @description: 同意
 */

import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.purchase.SrmPurchaseInApi;
import cn.iocoder.yudao.module.srm.api.purchase.dto.req.SrmPurchaseInSaveReqDTO;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.api.transfer.TmsTransferApi;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsInboundItemReqDTO;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsInboundReqDTO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.quantity.InboundExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.InboundContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class InboundAgreeTransitionHandler extends BaseInboundTransitionHandler {

    @Resource
    private InboundExecutor inboundExecutor;

    @Resource
    private SrmPurchaseInApi srmPurchaseInApi;

    @Resource
    private TmsTransferApi tmsTransferApi;

    @Override
    public void perform(Integer from, Integer to, WmsInboundAuditStatus.Event event, TransitionContext<WmsInboundDO> context) {
        super.perform(from, to, event, context);
        // 调整库存
        InboundContext inboundContext=new InboundContext();
        inboundContext.setInboundId(context.data().getId());
        inboundExecutor.execute(inboundContext);
        //获取入库单
        WmsInboundRespVO inboundVO = inboundService.getInboundWithItemList(context.data().getId());
        List<WmsInboundItemRespVO> itemList = inboundVO.getItemList();
        //处理到货单逻辑
        if (inboundVO.getUpstreamType() != null && inboundVO.getUpstreamType().equals(BillType.SRM_PURCHASE_IN.getValue())) {
            //如果成功创建入库单-触发SRM入库数量联动
            SrmPurchaseInSaveReqDTO reqDTO = BeanUtils.toBean(inboundVO, SrmPurchaseInSaveReqDTO.class);
            srmPurchaseInApi.updatePurchaseInItemQty(reqDTO);
        }
        //如果成功创建入库单-触发TMS入库数量联动
        if (inboundVO.getUpstreamType() != null && inboundVO.getUpstreamType().equals(BillType.TMS_TRANSFER.getValue())) {
            TmsInboundReqDTO tmsInboundReqDTO = BeanUtils.toBean(inboundVO, TmsInboundReqDTO.class);
            tmsInboundReqDTO.setItemList(BeanUtils.toBean(inboundVO.getItemList(), TmsInboundItemReqDTO.class));
            tmsTransferApi.afterInboundAudit(tmsInboundReqDTO);
        }

    }
}
