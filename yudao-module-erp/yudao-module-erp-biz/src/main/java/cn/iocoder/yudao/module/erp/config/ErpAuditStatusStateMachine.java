package cn.iocoder.yudao.module.erp.config;

import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class ErpAuditStatusStateMachine {

    private static final Map<ErpAuditStatus, Set<ErpAuditStatus>> STATE_TRANSITIONS = new EnumMap<>(ErpAuditStatus.class);

    static {
        // 定义状态转换规则
        STATE_TRANSITIONS.put(ErpAuditStatus.DRAFT, Set.of(ErpAuditStatus.SUBMITTED));
        STATE_TRANSITIONS.put(ErpAuditStatus.SUBMITTED, Set.of(ErpAuditStatus.PROCESS));
        STATE_TRANSITIONS.put(ErpAuditStatus.PROCESS, Set.of(ErpAuditStatus.PROCESSING));
        STATE_TRANSITIONS.put(ErpAuditStatus.PROCESSING, Set.of(ErpAuditStatus.APPROVE, ErpAuditStatus.REJECTED));
        STATE_TRANSITIONS.put(ErpAuditStatus.REJECTED, Set.of(ErpAuditStatus.SUBMITTED));
        STATE_TRANSITIONS.put(ErpAuditStatus.APPROVE, Set.of(ErpAuditStatus.REVERSED, ErpAuditStatus.OT_ORDERED));
        STATE_TRANSITIONS.put(ErpAuditStatus.OT_ORDERED, Set.of(ErpAuditStatus.ORDERED));
        STATE_TRANSITIONS.put(ErpAuditStatus.ORDERED, Set.of(ErpAuditStatus.PARTIALLY_ORDERED, ErpAuditStatus.ORDER_FAILED));

        // 任何状态都可以变成 CLOSED / MANUAL_CLOSED
        for (ErpAuditStatus status : ErpAuditStatus.values()) {
            STATE_TRANSITIONS.putIfAbsent(status, Set.of());
            STATE_TRANSITIONS.get(status).add(ErpAuditStatus.CLOSED);
            STATE_TRANSITIONS.get(status).add(ErpAuditStatus.MANUAL_CLOSED);
        }
    }

    /**
     * 校验是否可以流转到目标状态
     *
     * @param currentState 当前状态
     * @param nextState    目标状态
     * @return 是否允许流转
     */
    public static boolean canTransition(ErpAuditStatus currentState, ErpAuditStatus nextState) {
        return STATE_TRANSITIONS.getOrDefault(currentState, Set.of()).contains(nextState);
    }

    /**
     * 获取某个状态的所有可流转状态
     *
     * @param state 当前状态
     * @return 可流转的状态集合
     */
    public static Set<ErpAuditStatus> getAllowedTransitions(ErpAuditStatus state) {
        return STATE_TRANSITIONS.getOrDefault(state, Set.of());
    }
}
