package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetUsageDO;

import java.time.LocalDateTime;

/**
 * AI 预算用量 Service 接口
 *
 * @author 芋道源码
 */
public interface AiBudgetUsageService {

    /**
     * 获得指定用户在指定周期的用量
     *
     * @param userId          用户编号，0 表示租户级
     * @param periodStartTime 周期开始时间
     * @return 用量记录，不存在返回 null
     */
    AiBudgetUsageDO getUsage(Long userId, LocalDateTime periodStartTime);

    /**
     * 增加用量（upsert：不存在则创建，存在则累加）
     *
     * @param userId          用户编号，0 表示租户级
     * @param periodStartTime 周期开始时间
     * @param deltaAmount     增加的金额（微元）
     */
    void addUsage(Long userId, LocalDateTime periodStartTime, long deltaAmount);

}
