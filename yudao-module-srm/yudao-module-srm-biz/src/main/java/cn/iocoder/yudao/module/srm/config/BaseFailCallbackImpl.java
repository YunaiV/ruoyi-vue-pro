package cn.iocoder.yudao.module.srm.config;

import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.*;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.PURCHASE_REQUEST_NOT_EXISTS_BY_STATUS;

//状态机基本异常回调
@Component
@Slf4j
public class BaseFailCallbackImpl<S, E, C> implements FailCallback<S, E, C> {

    // 状态机描述Map
    private static final Map<Map<Class<?>, Class<?>>, String> STATE_MACHINE_MAP_CN = new HashMap<>();

    static {
        // 采购申请主表状态机
        addStateMachine(SrmPurchaseRequestDO.class, SrmOffStatus.class, "开关采购申请单");
        addStateMachine(SrmPurchaseRequestDO.class, SrmOrderStatus.class, "订购采购申请单");
        addStateMachine(SrmPurchaseRequestDO.class, SrmStorageStatus.class, "入库采购申请单");
        addStateMachine(SrmPurchaseRequestAuditReqVO.class, SrmAuditStatus.class, "审核采购申请单");

        // 采购申请子表状态机
        addStateMachine(SrmPurchaseRequestItemsDO.class, SrmOffStatus.class, "开关采购申请单子项");
        addStateMachine(SrmPurchaseRequestItemsDO.class, SrmOrderStatus.class, "订购采购申请单子项");
        addStateMachine(SrmPurchaseRequestItemsDO.class, SrmStorageStatus.class, "入库采购申请单子项");

        // 采购订单主表状态机
        addStateMachine(SrmPurchaseOrderDO.class, SrmOffStatus.class, "开关采购订单");
        addStateMachine(SrmPurchaseOrderDO.class, SrmAuditStatus.class, "审核采购订单");
        addStateMachine(SrmPurchaseOrderDO.class, SrmExecutionStatus.class, "执行采购订单");
        addStateMachine(SrmPurchaseOrderDO.class, SrmStorageStatus.class, "入库采购订单");
        addStateMachine(SrmPurchaseOrderDO.class, SrmPaymentStatus.class, "付款采购订单");

        // 采购订单子表状态机
        addStateMachine(SrmPurchaseOrderItemDO.class, SrmOffStatus.class, "开关采购订单子项");
        addStateMachine(SrmPurchaseOrderItemDO.class, SrmExecutionStatus.class, "执行采购订单子项");
        addStateMachine(SrmPurchaseOrderItemDO.class, SrmStorageStatus.class, "入库采购订单子项");
        addStateMachine(SrmPurchaseOrderItemDO.class, SrmPaymentStatus.class, "付款采购订单子项");

        // 采购入库主表状态机
        addStateMachine(SrmPurchaseInDO.class, SrmAuditStatus.class, "审核采购入库单");
        addStateMachine(SrmPurchaseInDO.class, SrmPaymentStatus.class, "付款采购入库单");

        // 采购入库子表状态机
        addStateMachine(SrmPurchaseInItemDO.class, SrmPaymentStatus.class, "付款采购入库单子项");

        // 采购退货主表状态机
        addStateMachine(SrmPurchaseReturnDO.class, SrmAuditStatus.class, "审核退货单");
        addStateMachine(SrmPurchaseReturnDO.class, SrmReturnStatus.class, "退款退货单");
    }

    private static void addStateMachine(Class<?> contextClass, Class<?> stateClass, String description) {
        Map<Class<?>, Class<?>> key = new HashMap<>();
        key.put(contextClass, stateClass);
        STATE_MACHINE_MAP_CN.put(key, description);
    }

    public void onFail(S sourceState, S targetState, E event, C context) {
        String stateMachineDesc = getStateMachineDescription(sourceState,targetState,event,context);
        String statusDesc = convertEventToDescription(sourceState);

        log.warn("{}无法在({})状态下触发({})事件，上下文：{}", stateMachineDesc, statusDesc, SrmEventEnum.valueOf(event.toString()).getDesc(),
            context.getClass().getName());
        //        throw new IllegalArgumentException(msg);
        //        throw new ServiceException(msg);
        throw exception(PURCHASE_REQUEST_NOT_EXISTS_BY_STATUS, stateMachineDesc, statusDesc, SrmEventEnum.valueOf(event.toString()).getDesc());
    }

    private String getStateMachineDescription(S sourceState, S targetState, E event, C context) {
        if (context == null || sourceState == null) {
            return "";
        }

        Map<Class<?>, Class<?>> key = new HashMap<>();
        key.put(context.getClass(), sourceState.getClass());

        return STATE_MACHINE_MAP_CN.getOrDefault(key, "");
    }

    public String convertEventToDescription(Object event) {
        try {
            // 判断event是否为ArrayValuable的实例
            if (event instanceof ArrayValuable) {
                Method method = event.getClass().getMethod("getDesc");
                return (String)method.invoke(event);
            } else {
                log.warn("Unknown event type: " + event.getClass());
            }
        } catch (Exception e) {
            log.warn("Error while converting event to description", e);
        }
        return null;
    }


}


