//package cn.iocoder.yudao.module.srm.config;
//
//import cn.iocoder.yudao.module.srm.enums.status.ErpOffStatus;
//import cn.iocoder.yudao.module.srm.enums.status.ErpOrderStatus;
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
//    private static final Map<ErpOffStatus, Set<ErpOffStatus>> OFF_STATUS_TRANSITIONS = new HashMap<>();
//    // 订购状态转移
//    private static final Map<ErpOrderStatus, Set<ErpOrderStatus>> ORDER_STATUS_TRANSITIONS = new HashMap<>();
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
//        OFF_STATUS_TRANSITIONS.put(ErpOffStatus.OPEN, Set.of(ErpOffStatus.CLOSED, ErpOffStatus.MANUAL_CLOSED));
//        OFF_STATUS_TRANSITIONS.put(ErpOffStatus.MANUAL_CLOSED, Set.of(ErpOffStatus.OPEN));
//
//        // 订购状态转移
//        ORDER_STATUS_TRANSITIONS.put(ErpOrderStatus.OT_ORDERED, Set.of(ErpOrderStatus.ORDERED, ErpOrderStatus.PARTIALLY_ORDERED, ErpOrderStatus.ORDER_FAILED));
////        ORDER_STATUS_TRANSITIONS.put(ErpOrderStatus.ORDERED, Set.of(ErpOrderStatus.PARTIALLY_ORDERED, ErpOrderStatus.ORDER_FAILED));
//        ORDER_STATUS_TRANSITIONS.put(ErpOrderStatus.PARTIALLY_ORDERED, Set.of(ErpOrderStatus.ORDERED));
//        ORDER_STATUS_TRANSITIONS.put(ErpOrderStatus.ORDER_FAILED, Set.of(ErpOrderStatus.ORDERED));
//    }
//
//    // 校验审核状态变更是否合法
//    public static boolean canTransitionAudit(SrmAuditStatus currentState, SrmAuditStatus nextState) {
//        return STATUS_TRANSITIONS.getOrDefault(currentState, Collections.emptySet()).contains(nextState);
//    }
//
//    // 校验关闭状态变更是否合法
//    public static boolean canTransitionOff(ErpOffStatus currentState, ErpOffStatus nextState) {
//        return OFF_STATUS_TRANSITIONS.getOrDefault(currentState, Collections.emptySet()).contains(nextState);
//    }
//
//    // 校验订购状态变更是否合法
//    public static boolean canTransitionOrder(ErpOrderStatus currentState, ErpOrderStatus nextState) {
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
