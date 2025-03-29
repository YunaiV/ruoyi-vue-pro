package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class StorageActionImpl implements Action<SrmStorageStatus, SrmEventEnum, SrmPurchaseRequestDO> {

    @Autowired
    SrmPurchaseRequestItemsMapper itemsMapper;
    @Autowired
    SrmPurchaseRequestMapper requestMapper;

    @Override
    @Transactional
    public void execute(SrmStorageStatus from, SrmStorageStatus to, SrmEventEnum event, SrmPurchaseRequestDO context) {
        //如果所有的子项都是已入库，则主单已入库
        SrmPurchaseRequestDO requestDO = requestMapper.selectById(context.getId());
//        if (event == STOCK_ADJUSTMENT) {//付款调整->调整状态,根据子表来设置状态
        List<SrmPurchaseRequestItemsDO> items = itemsMapper.selectListByRequestId(context.getId());
        if (CollUtil.isEmpty(items)) {
            return;
        }
        // 判断订单项的入库状态
        boolean hasUn = false;
        boolean hasPart = false;
        boolean hasAll = true;

        for (SrmPurchaseRequestItemsDO item : items) {
            Integer inStatus = item.getInStatus();
            if (inStatus == null || inStatus.equals(SrmStorageStatus.NONE_IN_STORAGE.getCode())) {
                hasUn = true;
                hasAll = false;
            } else if (inStatus.equals(SrmPaymentStatus.PARTIALLY_PAYMENT.getCode())) {
                hasPart = true;
                hasAll = false;
            }
        }

        // 设置订单支付状态
        if (hasAll) {
            to = SrmStorageStatus.ALL_IN_STORAGE;
        } else if (hasPart || (hasUn && hasPart)) {
            to = SrmStorageStatus.PARTIALLY_IN_STORAGE;
        } else {
            to = SrmStorageStatus.NONE_IN_STORAGE;
        }
//        }

        requestDO.setInStatus(to.getCode());//设置状态
        requestMapper.updateById(requestDO);

        log.debug("采购申请项入库状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
