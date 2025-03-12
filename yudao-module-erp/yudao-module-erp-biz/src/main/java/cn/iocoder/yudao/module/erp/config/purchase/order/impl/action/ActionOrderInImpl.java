package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.iocoder.yudao.module.erp.enums.ErpEventEnum.STOCK_ADJUSTMENT;

@Slf4j
@Component
public class ActionOrderInImpl implements Action<ErpStorageStatus, ErpEventEnum, ErpPurchaseOrderDO> {
    @Resource
    ErpPurchaseOrderMapper mapper;
    @Resource
    private ErpPurchaseOrderItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpStorageStatus from, ErpStorageStatus to, ErpEventEnum event, ErpPurchaseOrderDO context) {
        // 参数校验
        if (context == null || context.getId() == null) {
            return;
        }

        if (event == STOCK_ADJUSTMENT) {//动态调整入库数量->动态调整状态
            // 获取订单项的入库状态
            List<ErpPurchaseOrderItemDO> items = itemMapper.selectListByOrderId(context.getId());
            if (CollUtil.isEmpty(items)) {
                return;
            }

            // 判断订单项的入库状态
            boolean hasNotInStorage = false;
            boolean hasPartialInStorage = false;
            boolean allInStorage = true;

            for (ErpPurchaseOrderItemDO item : items) {
                Integer inStatus = item.getInStatus();
                if (inStatus == null || inStatus.equals(ErpStorageStatus.NONE_IN_STORAGE.getCode())) {
                    hasNotInStorage = true;
                    allInStorage = false;
                } else if (inStatus.equals(ErpStorageStatus.PARTIALLY_IN_STORAGE.getCode())) {
                    hasPartialInStorage = true;
                    allInStorage = false;
                }
            }

            // 设置订单入库状态
            if (allInStorage) {
                to = ErpStorageStatus.ALL_IN_STORAGE;
            } else if (hasPartialInStorage || (hasNotInStorage && hasPartialInStorage)) {
                to = ErpStorageStatus.PARTIALLY_IN_STORAGE;
            } else {
                to = ErpStorageStatus.NONE_IN_STORAGE;
            }
        }

        ErpPurchaseOrderDO orderDO = mapper.selectById(context.getId());
        orderDO.setInStatus(to.getCode());
        mapper.updateById(orderDO);
        //log
        log.info("入库状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
