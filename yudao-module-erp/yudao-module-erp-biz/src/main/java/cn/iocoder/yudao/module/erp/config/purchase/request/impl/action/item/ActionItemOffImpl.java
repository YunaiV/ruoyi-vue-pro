package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action.item;


import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
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

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;

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
        ThrowUtil.ifSqlThrow(mapper.updateById(itemsDO), DB_UPDATE_ERROR);
//        log.info("更新采购项状态：ID={}，状态={} -> {}", itemsDO.getId(), from, to);

        // 查询同一个采购请求的所有子项
        List<ErpPurchaseRequestItemsDO> itemList = mapper.selectListByRequestId(itemsDO.getRequestId());
        if (itemList.isEmpty()) {
            log.warn("采购请求ID={} 没有子项", itemsDO.getRequestId());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_ID, context.getId());
        }

        // **如果所有子项都关闭了，则传递事件给主表**
        boolean allClosed = itemList.stream().noneMatch(item -> item.getOffStatus().equals(ErpOffStatus.OPEN.getCode()));
        boolean hasOpen = itemList.stream().anyMatch(item -> item.getOffStatus().equals(ErpOffStatus.OPEN.getCode()));

        ErpPurchaseRequestDO requestDO = requestMapper.selectById(itemsDO.getRequestId());
        if (requestDO == null) {
            log.error("采购申请单不存在，ID: {}", itemsDO.getRequestId());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PURCHASE_REQUEST_NOT_EXISTS, itemsDO.getRequestId());
        }

        if (allClosed) {
            log.debug("所有子项已关闭，传递事件: ID={}", requestDO.getId());
            //主表是开启状态
            if (requestDO.getOffStatus().equals(ErpOffStatus.OPEN.getCode())) {
                machine.fireEvent(ErpOffStatus.fromCode(requestDO.getOffStatus()), event, requestDO);
            }
        } else if (hasOpen) {
            log.info("存在已开启的子项，传递事件: ID={}", requestDO.getId());
            //主表不是开启状态
            if (!requestDO.getOffStatus().equals(ErpOffStatus.OPEN.getCode())) {
                machine.fireEvent(ErpOffStatus.fromCode(requestDO.getOffStatus()), event, requestDO);
            }
        }
        log.debug("item开关状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
