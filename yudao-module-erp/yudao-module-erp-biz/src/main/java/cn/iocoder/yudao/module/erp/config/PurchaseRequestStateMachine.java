package cn.iocoder.yudao.module.erp.config;

import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.ErpOrderStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class PurchaseRequestStateMachine {
    // 审核状态转移
    private static final Map<ErpAuditStatus, Set<ErpAuditStatus>> STATUS_TRANSITIONS = new HashMap<>();
    // 关闭状态转移
    private static final Map<ErpOffStatus, Set<ErpOffStatus>> OFF_STATUS_TRANSITIONS = new HashMap<>();
    // 订购状态转移
    private static final Map<ErpOrderStatus, Set<ErpOrderStatus>> ORDER_STATUS_TRANSITIONS = new HashMap<>();

    static {
        // 审核状态转移
//        STATUS_TRANSITIONS.put(ErpAuditStatus.DRAFT, Set.of(ErpAuditStatus.SUBMITTED));
//        STATUS_TRANSITIONS.put(ErpAuditStatus.SUBMITTED, Set.of(ErpAuditStatus.PROCESS));
        STATUS_TRANSITIONS.put(ErpAuditStatus.PROCESS, Set.of(ErpAuditStatus.PROCESSING,ErpAuditStatus.APPROVE, ErpAuditStatus.REJECTED));
        STATUS_TRANSITIONS.put(ErpAuditStatus.REVERSED, Set.of(ErpAuditStatus.PROCESSING,ErpAuditStatus.APPROVE, ErpAuditStatus.REJECTED));
        STATUS_TRANSITIONS.put(ErpAuditStatus.APPROVE, Set.of(ErpAuditStatus.REVERSED));

        // 关闭状态转移
        OFF_STATUS_TRANSITIONS.put(ErpOffStatus.OPEN, Set.of(ErpOffStatus.CLOSED, ErpOffStatus.MANUAL_CLOSED));
        OFF_STATUS_TRANSITIONS.put(ErpOffStatus.MANUAL_CLOSED, Set.of(ErpOffStatus.OPEN));

        // 订购状态转移
        ORDER_STATUS_TRANSITIONS.put(ErpOrderStatus.OT_ORDERED, Set.of(ErpOrderStatus.ORDERED, ErpOrderStatus.PARTIALLY_ORDERED, ErpOrderStatus.ORDER_FAILED));
//        ORDER_STATUS_TRANSITIONS.put(ErpOrderStatus.ORDERED, Set.of(ErpOrderStatus.PARTIALLY_ORDERED, ErpOrderStatus.ORDER_FAILED));
        ORDER_STATUS_TRANSITIONS.put(ErpOrderStatus.PARTIALLY_ORDERED, Set.of(ErpOrderStatus.ORDERED));
        ORDER_STATUS_TRANSITIONS.put(ErpOrderStatus.ORDER_FAILED, Set.of(ErpOrderStatus.ORDERED));
    }

    // 校验审核状态变更是否合法
    public static boolean canTransitionAudit(ErpAuditStatus currentState, ErpAuditStatus nextState) {
        return STATUS_TRANSITIONS.getOrDefault(currentState, Collections.emptySet()).contains(nextState);
    }

    // 校验关闭状态变更是否合法
    public static boolean canTransitionOff(ErpOffStatus currentState, ErpOffStatus nextState) {
        return OFF_STATUS_TRANSITIONS.getOrDefault(currentState, Collections.emptySet()).contains(nextState);
    }

    // 校验订购状态变更是否合法
    public static boolean canTransitionOrder(ErpOrderStatus currentState, ErpOrderStatus nextState) {
        return ORDER_STATUS_TRANSITIONS.getOrDefault(currentState, Collections.emptySet()).contains(nextState);
    }
    //付款状态

    //入库状态

    //执行状态

    // 根据 Integer 值获取对应的枚举
    public static ErpAuditStatus fromValue(int value) {
        for (ErpAuditStatus status : ErpAuditStatus.values()) {
            if (status.getCode() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态值: " + value);
    }
}
