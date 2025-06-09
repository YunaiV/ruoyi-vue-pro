package cn.iocoder.yudao.module.srm.config.purchase.returned.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseReturnItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseReturnMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmReturnStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class ReturnActionImpl implements Action<SrmReturnStatus, SrmEventEnum, SrmPurchaseReturnDO> {
    @Autowired
    SrmPurchaseReturnMapper srmPurchaseReturnMapper;

    @Autowired
    SrmPurchaseReturnItemMapper srmPurchaseReturnItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmReturnStatus from, SrmReturnStatus to, SrmEventEnum event, SrmPurchaseReturnDO context) {
        SrmPurchaseReturnDO returnDO = srmPurchaseReturnMapper.selectById(context.getId());

        // 获取所有子表记录
        List<SrmPurchaseReturnItemDO> items = srmPurchaseReturnItemMapper.selectListByReturnId(context.getId());
//
//        // 根据子表状态判断主表状态，退款状态
//        if (items.isEmpty()) {
//            returnDO.setRefundStatus(SrmReturnStatus.NOT_RETURN.getCode());
//        } else {
//            boolean allReturned = true;
//            boolean hasReturned = false;
//
//            for (SrmPurchaseReturnItemDO item : items) {
//                if (item.getRefundStatus() == null || item.getRefundStatus() == SrmReturnStatus.NOT_RETURN.getCode()) {
//                    allReturned = false;
//                } else if (item.getRefundStatus() == SrmReturnStatus.RETURNED.getCode()) {
//                    hasReturned = true;
//                }
//            }
//
//            if (allReturned) {
//                returnDO.setRefundStatus(SrmReturnStatus.RETURNED.getCode());
//            } else if (hasReturned) {
//                returnDO.setRefundStatus(SrmReturnStatus.PART_RETURNED.getCode());
//            } else {
//                returnDO.setRefundStatus(SrmReturnStatus.NOT_RETURN.getCode());
//            }
//        }

        //退款
        srmPurchaseReturnMapper.updateById(returnDO.setRefundStatus(to.getCode()));

        log.debug("item状态机-付款-触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
