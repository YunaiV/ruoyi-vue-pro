package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.AiUsageLogDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 调用用量日志 Mapper。
 */
@Mapper
public interface AiUsageLogMapper extends BaseMapperX<AiUsageLogDO> {

    /**
     * 统计指定用户当日已成功调用次数（用于每日配额检查）。
     *
     * @param userId    用户 ID
     * @param startOfDay 今日 00:00:00
     * @return 调用次数
     */
    default long countTodayByUser(String userId, LocalDateTime startOfDay) {
        return selectCount(new LambdaQueryWrapper<AiUsageLogDO>()
                .eq(AiUsageLogDO::getUserId, userId)
                .eq(AiUsageLogDO::getStatus, "OK")
                .ge(AiUsageLogDO::getCreatedAt, startOfDay));
    }

    /**
     * 统计指定租户当日已成功调用次数（用于租户级配额检查）。
     *
     * @param tenantId   租户 ID
     * @param startOfDay 今日 00:00:00
     * @return 调用次数
     */
    default long countTodayByTenant(Long tenantId, LocalDateTime startOfDay) {
        return selectCount(new LambdaQueryWrapper<AiUsageLogDO>()
                .eq(AiUsageLogDO::getTenantId, tenantId)
                .eq(AiUsageLogDO::getStatus, "OK")
                .ge(AiUsageLogDO::getCreatedAt, startOfDay));
    }
}
