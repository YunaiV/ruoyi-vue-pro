package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class ActionStorageImpl implements Action<ErpStorageStatus, SrmEventEnum, ErpPurchaseRequestDO> {

    @Autowired
    ErpPurchaseRequestItemsMapper itemsMapper;
    @Autowired
    ErpPurchaseRequestMapper requestMapper;

    @Override
    @Transactional
    public void execute(ErpStorageStatus from, ErpStorageStatus to, SrmEventEnum event, ErpPurchaseRequestDO context) {
        //如果所有的子项都是已入库，则主单已入库
        ErpPurchaseRequestDO requestDO = requestMapper.selectById(context.getId());
//        if (event == STOCK_ADJUSTMENT) {//付款调整->调整状态,根据子表来设置状态
        List<ErpPurchaseRequestItemsDO> items = itemsMapper.selectListByRequestId(context.getId());
        if (CollUtil.isEmpty(items)) {
            return;
        }
        // 判断订单项的入库状态
        boolean hasUn = false;
        boolean hasPart = false;
        boolean hasAll = true;

        for (ErpPurchaseRequestItemsDO item : items) {
            Integer inStatus = item.getInStatus();
            if (inStatus == null || inStatus.equals(ErpStorageStatus.NONE_IN_STORAGE.getCode())) {
                hasUn = true;
                hasAll = false;
            } else if (inStatus.equals(ErpPaymentStatus.PARTIALLY_PAYMENT.getCode())) {
                hasPart = true;
                hasAll = false;
            }
        }

        // 设置订单支付状态
        if (hasAll) {
            to = ErpStorageStatus.ALL_IN_STORAGE;
        } else if (hasPart || (hasUn && hasPart)) {
            to = ErpStorageStatus.PARTIALLY_IN_STORAGE;
        } else {
            to = ErpStorageStatus.NONE_IN_STORAGE;
        }
//        }

        requestDO.setInStatus(to.getCode());//设置状态
        requestMapper.updateById(requestDO);

        log.debug("采购申请项入库状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
