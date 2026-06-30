package cn.iocoder.yudao.module.ai.dal.mysql.billing;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetUsageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * AI 预算用量 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiBudgetUsageMapper extends BaseMapperX<AiBudgetUsageDO> {

    /**
     * 查询指定用户在指定周期的用量记录
     *
     * @param userId          用户编号，0 表示租户级
     * @param periodStartTime 周期开始时间
     * @return 用量记录，不存在返回 null
     */
    default AiBudgetUsageDO selectByUserAndPeriod(Long userId, LocalDateTime periodStartTime) {
        return selectOne(new LambdaQueryWrapperX<AiBudgetUsageDO>()
                .eq(AiBudgetUsageDO::getUserId, userId)
                .eq(AiBudgetUsageDO::getPeriodStartTime, periodStartTime));
    }

    /**
     * SQL 原子累加已用金额，避免并发丢失更新
     *
     * 使用 used_amount = used_amount + delta 由数据库行锁保证原子性，
     * 无需应用层乐观锁。租户级（userId=0）为热点行，
     * 单行 UPDATE 吞吐受 InnoDB 行锁限制，约 500-2000 TPS（普通 SSD）。
     *
     * 注意：@Update 不走 MyBatis-Plus 租户插件，必须手动加 tenant_id 条件。
     *
     * @param userId          用户编号
     * @param periodStartTime 周期开始时间
     * @param deltaAmount     增量（微元）
     * @param tenantId        租户编号
     * @return 影响行数，0 表示记录不存在
     */
    @Update("UPDATE ai_budget_usage SET used_amount = used_amount + #{deltaAmount}, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId} AND period_start_time = #{periodStartTime} " +
            "AND tenant_id = #{tenantId} AND deleted = 0")
    int incrementUsedAmount(@Param("userId") Long userId,
                            @Param("periodStartTime") LocalDateTime periodStartTime,
                            @Param("deltaAmount") long deltaAmount,
                            @Param("tenantId") Long tenantId);

}
