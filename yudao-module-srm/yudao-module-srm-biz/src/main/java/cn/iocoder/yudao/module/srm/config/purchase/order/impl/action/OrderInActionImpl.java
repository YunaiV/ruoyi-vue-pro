package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.iocoder.yudao.module.srm.enums.SrmEventEnum.STOCK_ADJUSTMENT;

@Slf4j
@Component
public class OrderInActionImpl implements Action<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> {
    @Resource
    SrmPurchaseOrderMapper mapper;
    @Resource
    private SrmPurchaseOrderItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmStorageStatus from, SrmStorageStatus to, SrmEventEnum event, SrmPurchaseOrderDO context) {
        // 参数校验
        if (context == null || context.getId() == null) {
            return;
        }

        if (event == STOCK_ADJUSTMENT) {//动态调整入库数量->动态调整状态
            // 获取订单项的入库状态
            List<SrmPurchaseOrderItemDO> items = itemMapper.selectListByOrderId(context.getId());
            if (CollUtil.isEmpty(items)) {
                return;
            }

            // 判断订单项的入库状态
            boolean hasNotInStorage = false;
            boolean hasPartialInStorage = false;
            boolean allInStorage = true;

            for (SrmPurchaseOrderItemDO item : items) {
                Integer inStatus = item.getInStatus();
                if (inStatus == null || inStatus.equals(SrmStorageStatus.NONE_IN_STORAGE.getCode())) {
                    hasNotInStorage = true;
                    allInStorage = false;
                } else if (inStatus.equals(SrmStorageStatus.PARTIALLY_IN_STORAGE.getCode())) {
                    hasPartialInStorage = true;
                    allInStorage = false;
                }
            }

            // 设置订单入库状态
            if (allInStorage) {
                to = SrmStorageStatus.ALL_IN_STORAGE;
            } else if (hasPartialInStorage || (hasNotInStorage && hasPartialInStorage)) {
                to = SrmStorageStatus.PARTIALLY_IN_STORAGE;
            } else {
                to = SrmStorageStatus.NONE_IN_STORAGE;
            }
        }

        SrmPurchaseOrderDO orderDO = mapper.selectById(context.getId());
        orderDO.setInStatus(to.getCode());
        mapper.updateById(orderDO);
        //log
        log.debug("入库状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
