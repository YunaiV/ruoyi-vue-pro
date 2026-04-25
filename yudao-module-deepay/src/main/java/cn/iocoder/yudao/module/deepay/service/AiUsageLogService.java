package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.AiUsageLogDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.AiUsageLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * AI 调用用量日志服务。
 *
 * <p>职责：
 * <ul>
 *   <li>异步写入每次 AI 调用记录（不阻塞主流程）</li>
 *   <li>按用户/租户统计当日调用量（用于每日配额检查）</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
public class AiUsageLogService {

    @Resource
    private AiUsageLogMapper usageLogMapper;

    /**
     * 异步记录一次 AI 调用。
     * 使用 {@code @Async} 保证不阻塞 SSE 响应流程。
     *
     * @param userId     用户 ID（可为空字符串表示匿名）
     * @param tenantId   租户 ID
     * @param module     模块名
     * @param sessionId  会话 ID（可为 null）
     * @param entityType 上下文实体类型（可为 null）
     * @param entityId   上下文实体 ID（可为 null）
     * @param status     状态（OK / RATE_LIMITED / QUOTA_EXCEEDED / ERROR）
     * @param errorMsg   错误信息（status=ERROR 时传入，其他传 null）
     */
    @Async("deepayAsyncExecutor")
    public void log(String userId, Long tenantId, String module,
                    String sessionId, String entityType, String entityId,
                    String status, String errorMsg) {
        try {
            AiUsageLogDO record = new AiUsageLogDO();
            record.setUserId(userId != null ? userId : "");
            record.setTenantId(tenantId != null ? tenantId : 0L);
            record.setModule(module != null ? module : "");
            record.setSessionId(sessionId);
            record.setEntityType(entityType);
            record.setEntityId(entityId);
            record.setStatus(status);
            record.setErrorMsg(errorMsg);
            record.setCreatedAt(LocalDateTime.now());
            usageLogMapper.insert(record);
        } catch (Exception e) {
            // 日志写入失败不影响业务，仅打印警告
            log.warn("[AiUsageLogService] 写入用量日志失败 userId={} module={}", userId, module, e);
        }
    }

    /**
     * 统计指定用户今日成功调用次数。
     *
     * @param userId 用户 ID
     * @return 今日调用次数
     */
    public long countTodayByUser(String userId) {
        if (userId == null || userId.isEmpty()) return 0L;
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        try {
            return usageLogMapper.countTodayByUser(userId, startOfDay);
        } catch (Exception e) {
            log.warn("[AiUsageLogService] 查询今日用量失败 userId={}", userId, e);
            return 0L;
        }
    }

    /**
     * 统计指定租户今日成功调用次数。
     *
     * @param tenantId 租户 ID
     * @return 今日调用次数
     */
    public long countTodayByTenant(Long tenantId) {
        if (tenantId == null) return 0L;
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        try {
            return usageLogMapper.countTodayByTenant(tenantId, startOfDay);
        } catch (Exception e) {
            log.warn("[AiUsageLogService] 查询今日用量失败 tenantId={}", tenantId, e);
            return 0L;
        }
    }
}
