package cn.iocoder.yudao.module.erp.config.purchase.impl.action.item;


import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class ActionItemOffImpl implements Action<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> {

    @Autowired
    private ErpPurchaseRequestItemsMapper mapper;

    @Autowired
    private ErpPurchaseRequestMapper requestMapper;

    @Autowired
    @Qualifier(value = ErpStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    private StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> machine;

    @Override
    @Transactional
    public void execute(ErpOffStatus from, ErpOffStatus to, ErpEventEnum event, ErpPurchaseRequestItemsDO context) {
        // 查询当前 Item
        ErpPurchaseRequestItemsDO itemsDO = mapper.selectById(context.getId());
        if (itemsDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PURCHASE_REQUEST_ITEM_NOT_EXISTS, context.getId());
        }
        // 更新 Item 状态
        itemsDO.setOffStatus(to.getCode());
        mapper.updateById(itemsDO);
        log.info("更新采购项状态：ID={}，状态={} -> {}", itemsDO.getId(), from, to);

        // 查询同一个采购请求的所有子项
        List<ErpPurchaseRequestItemsDO> itemList = mapper.selectListByRequestId(itemsDO.getRequestId());
        if (itemList.isEmpty()) {
            log.warn("采购请求ID={} 没有子项", itemsDO.getRequestId());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_ID, context.getId());
        }

        // **如果所有子项都关闭了，则传递事件给主表**
        boolean allClosed = itemList.stream().allMatch(item -> item.getOffStatus().equals(ErpOffStatus.CLOSED.getCode()));
        boolean hasOpen = itemList.stream().anyMatch(item -> item.getOffStatus().equals(ErpOffStatus.OPEN.getCode()));

        ErpPurchaseRequestDO requestDO = requestMapper.selectById(itemsDO.getRequestId());
        if (requestDO == null) {
            log.error("采购申请单不存在，ID: {}", itemsDO.getRequestId());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PURCHASE_REQUEST_NOT_EXISTS, itemsDO.getRequestId());
        }

        if (allClosed) {
            log.info("所有子项已关闭，更新主表状态: ID={}", requestDO.getId());
            machine.fireEvent(ErpOffStatus.fromCode(requestDO.getOffStatus()), ErpEventEnum.AUTO_CLOSE, requestDO);
        } else if (hasOpen) {
            log.info("存在已开启的子项，更新主表状态: ID={}", requestDO.getId());
            machine.fireEvent(ErpOffStatus.fromCode(requestDO.getOffStatus()), ErpEventEnum.ACTIVATE, requestDO);
        }
        log.info("item开关状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
