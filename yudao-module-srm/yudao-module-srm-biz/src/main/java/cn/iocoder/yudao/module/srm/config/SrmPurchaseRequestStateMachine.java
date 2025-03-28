//package cn.iocoder.yudao.module.srm.config;
//
//import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
//import cn.iocoder.yudao.module.srm.enums.status.SrmOrderStatus;
//import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//
//public class PurchaseRequestStateMachine {
//    // 审核状态转移
//    private static final Map<SrmAuditStatus, Set<SrmAuditStatus>> STATUS_TRANSITIONS = new HashMap<>();
//    // 关闭状态转移
//    private static final Map<SrmOffStatus, Set<SrmOffStatus>> OFF_STATUS_TRANSITIONS = new HashMap<>();
//    // 订购状态转移
//    private static final Map<SrmOrderStatus, Set<SrmOrderStatus>> ORDER_STATUS_TRANSITIONS = new HashMap<>();
//
//    static {
//        // 审核状态转移
////        STATUS_TRANSITIONS.put(SrmAuditStatus.DRAFT, Set.of(SrmAuditStatus.SUBMITTED));
////        STATUS_TRANSITIONS.put(SrmAuditStatus.SUBMITTED, Set.of(SrmAuditStatus.PROCESS));
//        STATUS_TRANSITIONS.put(SrmAuditStatus.PENDING_REVIEW, Set.of(SrmAuditStatus.UNDER_REVIEW, SrmAuditStatus.APPROVED, SrmAuditStatus.REJECTED));
//        STATUS_TRANSITIONS.put(SrmAuditStatus.REVOKED, Set.of(SrmAuditStatus.UNDER_REVIEW, SrmAuditStatus.APPROVED, SrmAuditStatus.REJECTED));
//        STATUS_TRANSITIONS.put(SrmAuditStatus.APPROVED, Set.of(SrmAuditStatus.REVOKED));
//
//        // 关闭状态转移
//        OFF_STATUS_TRANSITIONS.put(SrmOffStatus.OPEN, Set.of(SrmOffStatus.CLOSED, SrmOffStatus.MANUAL_CLOSED));
//        OFF_STATUS_TRANSITIONS.put(SrmOffStatus.MANUAL_CLOSED, Set.of(SrmOffStatus.OPEN));
//
//        // 订购状态转移
//        ORDER_STATUS_TRANSITIONS.put(SrmOrderStatus.OT_ORDERED, Set.of(SrmOrderStatus.ORDERED, SrmOrderStatus.PARTIALLY_ORDERED, SrmOrderStatus.ORDER_FAILED));
////        ORDER_STATUS_TRANSITIONS.put(SrmOrderStatus.ORDERED, Set.of(SrmOrderStatus.PARTIALLY_ORDERED, SrmOrderStatus.ORDER_FAILED));
//        ORDER_STATUS_TRANSITIONS.put(SrmOrderStatus.PARTIALLY_ORDERED, Set.of(SrmOrderStatus.ORDERED));
//        ORDER_STATUS_TRANSITIONS.put(SrmOrderStatus.ORDER_FAILED, Set.of(SrmOrderStatus.ORDERED));
//    }
//
//    // 校验审核状态变更是否合法
//    public static boolean canTransitionAudit(SrmAuditStatus currentState, SrmAuditStatus nextState) {
//        return STATUS_TRANSITIONS.getOrDefault(currentState, Collections.emptySet()).contains(nextState);
//    }
//
//    // 校验关闭状态变更是否合法
//    public static boolean canTransitionOff(SrmOffStatus currentState, SrmOffStatus nextState) {
//        return OFF_STATUS_TRANSITIONS.getOrDefault(currentState, Collections.emptySet()).contains(nextState);
//    }
//
//    // 校验订购状态变更是否合法
//    public static boolean canTransitionOrder(SrmOrderStatus currentState, SrmOrderStatus nextState) {
//        return ORDER_STATUS_TRANSITIONS.getOrDefault(currentState, Collections.emptySet()).contains(nextState);
//    }
//    //付款状态
//
//    //入库状态
//
//    //执行状态
//
//    // 根据 Integer 值获取对应的枚举
//    public static SrmAuditStatus fromValue(int value) {
//        for (SrmAuditStatus status : SrmAuditStatus.values()) {
//            if (status.getCode() == value) {
//                return status;
//            }
//        }
//        throw new IllegalArgumentException("无效的状态值: " + value);
//    }
//}
