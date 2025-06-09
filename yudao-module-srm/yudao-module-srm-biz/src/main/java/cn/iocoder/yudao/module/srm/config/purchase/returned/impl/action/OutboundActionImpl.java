package cn.iocoder.yudao.module.srm.config.purchase.returned.impl.action;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.srm.config.machine.outItem.SrmPurchaseOutMachineContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseReturnItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseReturnMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmOutboundStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class OutboundActionImpl implements Action<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutMachineContext> {
    @Autowired
    private SrmPurchaseReturnMapper srmPurchaseReturnMapper;

    @Autowired
    private SrmPurchaseReturnItemMapper srmPurchaseReturnItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmOutboundStatus from, SrmOutboundStatus to, SrmEventEnum event, SrmPurchaseOutMachineContext context) {
        SrmPurchaseReturnDO returnDO = srmPurchaseReturnMapper.selectById(context.getReturnId());

        if (event == SrmEventEnum.OUT_STORAGE_ADJUSTMENT) {
            // 获取所有子表记录
            List<SrmPurchaseReturnItemDO> items = srmPurchaseReturnItemMapper.selectListByReturnId(context.getReturnId());

            if (items.isEmpty()) {
                // 如果没有子项，设置为未出库状态
                to = SrmOutboundStatus.NONE_OUTBOUND;
            } else {
                boolean allOutbound = true;
                boolean hasOutbound = false;

                for (SrmPurchaseReturnItemDO item : items) {
                    if (item.getOutboundStatus() == null || item.getOutboundStatus().equals(SrmOutboundStatus.NONE_OUTBOUND.getCode())) {
                        allOutbound = false;
                    } else if (item.getOutboundStatus().equals(SrmOutboundStatus.ALL_OUTBOUND.getCode())) {
                        hasOutbound = true;
                    }
                }

                if (allOutbound) {
                    // 所有子项都已出库
                    to = SrmOutboundStatus.ALL_OUTBOUND;
                } else if (hasOutbound) {
                    // 部分子项已出库
                    to = SrmOutboundStatus.PARTIALLY_OUTBOUND;
                } else {
                    // 所有子项都未出库
                    to = SrmOutboundStatus.NONE_OUTBOUND;
                }
            }
        }


        // 更新主表状态
        srmPurchaseReturnMapper.updateById(returnDO.setOutboundStatus(to.getCode()));

        // 记录日志
        log.debug("退货单出库状态机触发({})事件：对象returnId={}，状态 {} -> {}",
            event.getDesc(), returnDO.getId(), from.getDesc(), to.getDesc());
    }
}
