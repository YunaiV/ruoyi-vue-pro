package cn.iocoder.yudao.module.deepay.service;

/**
 * 审计日志服务 — 记录所有关键业务操作，不可变，全程可追溯。
 */
public interface DeepayAuditService {

    /**
     * 记录一条审计事件。
     *
     * @param chainCode 关联链码（可为 null）
     * @param action    操作类型（CREATE / PUBLISH / PAY / REPRICE / STOP / REDESIGN / REFUND / RESTOCK / RETRY）
     * @param before    操作前状态/数值描述
     * @param after     操作后状态/数值描述
     */
    void log(String chainCode, String action, String before, String after);

}
