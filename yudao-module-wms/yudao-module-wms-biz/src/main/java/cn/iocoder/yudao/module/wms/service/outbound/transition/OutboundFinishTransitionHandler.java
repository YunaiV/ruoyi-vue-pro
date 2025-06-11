package cn.iocoder.yudao.module.wms.service.outbound.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.api.first.mile.TmsFistMileApi;
import cn.iocoder.yudao.module.tms.api.transfer.TmsTransferApi;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsOutboundItemReqDTO;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsOutboundReqDTO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.outbound.item.WmsOutboundItemService;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundFinishExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:26
 * @description: 执行出库
 */
@Component
public class OutboundFinishTransitionHandler extends BaseOutboundTransitionHandler {

    @Resource
    private OutboundFinishExecutor outboundFinishExecutor;

    @Resource
    private WmsInboundService inboundService;

    @Resource
    private WmsOutboundItemService outboundItemService;

    @Resource
    private TmsTransferApi tmsTransferApi;

    @Resource
    private TmsFistMileApi tmsFistMileApi;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, TransitionContext<WmsOutboundDO> context) {
        super.perform(from, to, event, context);
        // 调整库存
        OutboundContext outboundContext = new OutboundContext();
        outboundContext.setOutboundId(context.data().getId());
        outboundFinishExecutor.execute(outboundContext);


        WmsOutboundDO outboundDO = context.data();
        List<WmsOutboundItemDO> outboundItemDOS = outboundItemService.selectByOutboundId(outboundDO.getId());

        BillType billType = BillType.parse(outboundDO.getUpstreamType());
        //更新TMS调拨单
        if (billType == BillType.TMS_TRANSFER) {
            TmsOutboundReqDTO reqDTO = BeanUtils.toBean(context.data(), TmsOutboundReqDTO.class);
            reqDTO.setItems(BeanUtils.toBean(outboundItemDOS, TmsOutboundItemReqDTO.class));
            //默认全部出库
            reqDTO.setOutboundStatus(WmsOutboundStatus.ALL.getValue());
            tmsTransferApi.afterOutboundAudit(reqDTO);
        }
        //更新TMS头程单
        if (billType == BillType.TMS_FIRST_MILE) {
            TmsOutboundReqDTO reqDTO = BeanUtils.toBean(context.data(), TmsOutboundReqDTO.class);
            reqDTO.setItems(BeanUtils.toBean(outboundItemDOS, TmsOutboundItemReqDTO.class));
            //默认全部出库
            reqDTO.setOutboundStatus(WmsOutboundStatus.ALL.getValue());
            tmsFistMileApi.outboundAudit(reqDTO);
        }
     }
}
