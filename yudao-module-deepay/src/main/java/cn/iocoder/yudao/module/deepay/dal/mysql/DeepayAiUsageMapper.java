package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiUsageDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 使用量 Mapper。
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
     * 原子自增当日用量（upsert 语义）。
     * 若记录已存在：daily_count+1；不存在：插入 daily_count=1。
     *
     * @return 自增后的当日次数
     */
    default int incrementDailyCount(Long tenantId, String userId, LocalDate date, String module) {
        DeepayAiUsageDO existing = selectByUserAndDate(tenantId, userId, date);
        if (existing == null) {
            DeepayAiUsageDO record = new DeepayAiUsageDO();
            record.setTenantId(tenantId);
            record.setUserId(userId);
            record.setUsageDate(date);
            record.setDailyCount(1);
            record.setModule(module != null ? module : "");
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            try {
                insert(record);
            } catch (Exception e) {
                // concurrent insert — re-fetch and increment
                update(null, new LambdaUpdateWrapper<DeepayAiUsageDO>()
                        .eq(DeepayAiUsageDO::getTenantId, tenantId)
                        .eq(DeepayAiUsageDO::getUserId, userId)
                        .eq(DeepayAiUsageDO::getUsageDate, date)
                        .setSql("daily_count = daily_count + 1")
                        .set(DeepayAiUsageDO::getUpdatedAt, LocalDateTime.now()));
                DeepayAiUsageDO updated = selectByUserAndDate(tenantId, userId, date);
                return updated != null ? updated.getDailyCount() : 1;
            }
            return 1;
        }
        update(null, new LambdaUpdateWrapper<DeepayAiUsageDO>()
                .eq(DeepayAiUsageDO::getTenantId, tenantId)
                .eq(DeepayAiUsageDO::getUserId, userId)
                .eq(DeepayAiUsageDO::getUsageDate, date)
                .setSql("daily_count = daily_count + 1")
                .set(DeepayAiUsageDO::getUpdatedAt, LocalDateTime.now()));
        DeepayAiUsageDO updated = selectByUserAndDate(tenantId, userId, date);
        return updated != null ? updated.getDailyCount() : (existing.getDailyCount() + 1);
    }

    default int getDailyCount(Long tenantId, String userId, LocalDate date) {
        DeepayAiUsageDO record = selectByUserAndDate(tenantId, userId, date);
        return record != null ? (record.getDailyCount() != null ? record.getDailyCount() : 0) : 0;
    }

}
