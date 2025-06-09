package cn.iocoder.yudao.module.srm.config;

import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.*;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.PURCHASE_REQUEST_NOT_EXISTS_BY_EVENT;

//状态机基本异常回调，兜底策略
@Component
@Primary
@Getter
@Slf4j
public class BaseFailCallbackImpl<S, E, C> implements FailCallback<S, E, C> {


    // 状态机描述Map
    public static Map<Map<Class<?>, Class<?>>, String> STATE_MACHINE_MAP_CN = new HashMap<>();


    static {
        // 采购申请 - 主表
        addStateMachine(SrmPurchaseRequestDO.class, SrmOffStatus.class, "采购申请 - 开关状态");
        addStateMachine(SrmPurchaseRequestDO.class, SrmOrderStatus.class, "采购申请 - 订购状态");
        addStateMachine(SrmPurchaseRequestDO.class, SrmStorageStatus.class, "采购申请 - 入库状态");
        addStateMachine(SrmPurchaseRequestAuditReqVO.class, SrmAuditStatus.class, "采购申请 - 审核状态");

        // 采购申请 - 子表
        addStateMachine(SrmPurchaseRequestItemsDO.class, SrmOffStatus.class, "采购申请子项 - 开关状态");
        addStateMachine(SrmPurchaseRequestItemsDO.class, SrmOrderStatus.class, "采购申请子项 - 订购状态");
        addStateMachine(SrmPurchaseRequestItemsDO.class, SrmStorageStatus.class, "采购申请子项 - 入库状态");

        // 采购订单 - 主表
        addStateMachine(SrmPurchaseOrderDO.class, SrmOffStatus.class, "采购订单 - 开关状态");
        addStateMachine(SrmPurchaseOrderDO.class, SrmAuditStatus.class, "采购订单 - 审核状态");
        addStateMachine(SrmPurchaseOrderDO.class, SrmExecutionStatus.class, "采购订单 - 执行状态");
        addStateMachine(SrmPurchaseOrderDO.class, SrmStorageStatus.class, "采购订单 - 入库状态");
        addStateMachine(SrmPurchaseOrderDO.class, SrmPaymentStatus.class, "采购订单 - 付款状态");

        // 采购订单 - 子表
        addStateMachine(SrmPurchaseOrderItemDO.class, SrmOffStatus.class, "采购订单子项 - 开关状态");
        addStateMachine(SrmPurchaseOrderItemDO.class, SrmExecutionStatus.class, "采购订单子项 - 执行状态");
        addStateMachine(SrmPurchaseOrderItemDO.class, SrmStorageStatus.class, "采购订单子项 - 入库状态");
        addStateMachine(SrmPurchaseOrderItemDO.class, SrmPaymentStatus.class, "采购订单子项 - 付款状态");

        // 采购入库 - 主表
        addStateMachine(SrmPurchaseInDO.class, SrmAuditStatus.class, "采购入库单 - 审核状态");
        addStateMachine(SrmPurchaseInDO.class, SrmPaymentStatus.class, "采购入库单 - 付款状态");

        // 采购入库 - 子表
        addStateMachine(SrmPurchaseInItemDO.class, SrmPaymentStatus.class, "采购入库子项 - 付款状态");

        // 采购退货单 - 主表
        addStateMachine(SrmPurchaseReturnDO.class, SrmAuditStatus.class, "退货单 - 审核状态");
        addStateMachine(SrmPurchaseReturnDO.class, SrmReturnStatus.class, "退货单 - 退款状态");

    }


//    @Autowired(required = false)
//    public void setRegistrars(List<StateMachineDescriptorRegistrar> registrars) {
//        if (registrars == null) {
//            return;
//        }
//        for (StateMachineDescriptorRegistrar registrar : registrars) {
//            registrar.register(BaseFailCallbackImpl::addStateMachine);
//        }
//    }

    private static void addStateMachine(Class<?> contextClass, Class<?> stateClass, String description) {
        Map<Class<?>, Class<?>> key = new HashMap<>();
        key.put(contextClass, stateClass);
        STATE_MACHINE_MAP_CN.put(key, description);
    }

    public void onFail(S sourceState, S targetState, E event, C context) {
        String stateMachineDesc = getStateMachineDescription(sourceState, targetState, event, context);
        String statusDesc = convertEventToDescription(sourceState);

        log.warn("{}无法在({})状态下触发({})事件，上下文：{}", stateMachineDesc, statusDesc, SrmEventEnum.valueOf(event.toString()).getDesc(), context.getClass().getName());
        throw exception(PURCHASE_REQUEST_NOT_EXISTS_BY_EVENT, stateMachineDesc, statusDesc, SrmEventEnum.valueOf(event.toString()).getDesc());
    }

    private String getStateMachineDescription(S sourceState, S targetState, E event, C context) {
        if (context == null || sourceState == null) {
            return "";
        }

        Map<Class<?>, Class<?>> key = new HashMap<>();
        key.put(context.getClass(), sourceState.getClass());

        return STATE_MACHINE_MAP_CN.getOrDefault(key, "");
    }

    private String convertEventToDescription(Object event) {
        try {
            // 判断event是否为ArrayValuable的实例
            if (event instanceof ArrayValuable) {
                Method method = event.getClass().getMethod("getDesc");
                return (String) method.invoke(event);
            } else {
                log.warn("Unknown event type: {}", event.getClass());
            }
        } catch (Exception e) {
            log.warn("Error while converting event to description", e);
        }
        return null;
    }

}


