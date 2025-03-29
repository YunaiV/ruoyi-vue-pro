package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.item;


import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ItemOffActionImpl implements Action<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestItemsDO> {

    @Autowired
    private SrmPurchaseRequestItemsMapper mapper;

    @Autowired
    private SrmPurchaseRequestMapper requestMapper;

    @Resource(name = PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    private StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestDO> machine;

    @Override
    @Transactional
    public void execute(SrmOffStatus from, SrmOffStatus to, SrmEventEnum event, SrmPurchaseRequestItemsDO context) {
        // 查询当前 Item
        SrmPurchaseRequestItemsDO itemsDO = mapper.selectById(context.getId());
        if(itemsDO == null) {
            throw ServiceExceptionUtil.exception(SrmErrorCodeConstants.PURCHASE_REQUEST_ITEM_NOT_EXISTS, context.getId());
        }
        // 更新 Item 状态
        itemsDO.setOffStatus(to.getCode());
        ThrowUtil.ifSqlThrow(mapper.updateById(itemsDO), DB_UPDATE_ERROR);
        //        log.info("更新采购项状态：ID={}，状态={} -> {}", itemsDO.getId(), from, to);

        // 查询同一个采购请求的所有子项
        List<SrmPurchaseRequestItemsDO> itemList = mapper.selectListByRequestId(itemsDO.getRequestId());
        if(itemList.isEmpty()) {
            log.warn("采购请求ID={} 没有子项", itemsDO.getRequestId());
            throw ServiceExceptionUtil.exception(SrmErrorCodeConstants.PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_ID, context.getId());
        }

        // **如果所有子项都关闭了，则传递事件给主表**
        boolean allClosed = itemList.stream().noneMatch(item -> item.getOffStatus().equals(SrmOffStatus.OPEN.getCode()));
        boolean hasOpen = itemList.stream().anyMatch(item -> item.getOffStatus().equals(SrmOffStatus.OPEN.getCode()));

        SrmPurchaseRequestDO requestDO = requestMapper.selectById(itemsDO.getRequestId());
        if(requestDO == null) {
            log.error("采购申请单不存在，ID: {}", itemsDO.getRequestId());
            throw ServiceExceptionUtil.exception(SrmErrorCodeConstants.PURCHASE_REQUEST_NOT_EXISTS, itemsDO.getRequestId());
        }

        if(allClosed) {
            log.debug("所有子项已关闭，传递事件: ID={}", requestDO.getId());
            //主表是开启状态
            if(requestDO.getOffStatus().equals(SrmOffStatus.OPEN.getCode())) {
                machine.fireEvent(SrmOffStatus.fromCode(requestDO.getOffStatus()), event, requestDO);
            }
        } else if(hasOpen) {
            log.info("存在已开启的子项，传递事件: ID={}", requestDO.getId());
            //主表不是开启状态
            if(!requestDO.getOffStatus().equals(SrmOffStatus.OPEN.getCode())) {
                machine.fireEvent(SrmOffStatus.fromCode(requestDO.getOffStatus()), event, requestDO);
            }
        }
        log.debug("item开关状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
