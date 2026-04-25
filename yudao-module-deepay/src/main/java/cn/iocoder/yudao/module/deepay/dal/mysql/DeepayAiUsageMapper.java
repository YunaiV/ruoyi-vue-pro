package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiUsageDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 使用量 Mapper。
 *
 * <p>使用 {@code INSERT INTO ... ON DUPLICATE KEY UPDATE} 原子 upsert，
 * 避免先查后写的 TOCTOU 竞态条件。</p>
 */
@Mapper
public interface DeepayAiUsageMapper extends BaseMapperX<DeepayAiUsageDO> {

    default DeepayAiUsageDO selectByUserAndDate(Long tenantId, String userId, LocalDate date) {
        return selectOne(new LambdaQueryWrapper<DeepayAiUsageDO>()
                .eq(DeepayAiUsageDO::getTenantId, tenantId)
                .eq(DeepayAiUsageDO::getUserId, userId)
                .eq(DeepayAiUsageDO::getUsageDate, date));
    }

    /**
     * 原子 upsert：INSERT ... ON DUPLICATE KEY UPDATE。
     * 利用 UK (tenant_id, user_id, usage_date) 保证并发安全，
     * 避免先查后写的竞态条件。
     *
     * @param tenantId  租户 ID
     * @param userId    用户标识
     * @param date      统计日期
     * @param module    板块名
     * @param now       当前时间
     */
    @Update("""
        INSERT INTO deepay_ai_usage (tenant_id, user_id, usage_date, daily_count, module, created_at, updated_at)
        VALUES (#{tenantId}, #{userId}, #{date}, 1, #{module}, #{now}, #{now})
        ON DUPLICATE KEY UPDATE
          daily_count = daily_count + 1,
          updated_at  = #{now}
        """)
    void upsertDailyCount(
            @Param("tenantId") Long      tenantId,
            @Param("userId")   String    userId,
            @Param("date")     LocalDate date,
            @Param("module")   String    module,
            @Param("now")      LocalDateTime now);

    /**
     * 自增当日用量（原子 upsert）并返回当日总次数。
     *
     * @return 自增后的当日次数
     */
    default int incrementDailyCount(Long tenantId, String userId, LocalDate date, String module) {
        upsertDailyCount(
                tenantId != null ? tenantId : 0L,
                userId,
                date,
                module != null ? module : "",
                LocalDateTime.now());
        DeepayAiUsageDO updated = selectByUserAndDate(
                tenantId != null ? tenantId : 0L, userId, date);
        return updated != null && updated.getDailyCount() != null ? updated.getDailyCount() : 1;
    }

    default int getDailyCount(Long tenantId, String userId, LocalDate date) {
        DeepayAiUsageDO record = selectByUserAndDate(tenantId, userId, date);
        return record != null ? (record.getDailyCount() != null ? record.getDailyCount() : 0) : 0;
    }

}
