package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserQuotaDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 用户配额 Mapper（STEP 26）。
 */
@Mapper
public interface DeepayUserQuotaMapper extends BaseMapperX<DeepayUserQuotaDO> {

    default DeepayUserQuotaDO selectByUserId(String userId) {
        return selectOne(new LambdaQueryWrapper<DeepayUserQuotaDO>()
                .eq(DeepayUserQuotaDO::getUserId, userId));
    }

    /**
     * 原子扣减配额：优先扣免费额度，不足时扣付费额度。
     * 返回受影响行数；0 表示配额不足，不应继续执行。
     */
    default int consumeQuota(String userId) {
        // 先尝试扣免费额度
        int rows = update(null, new LambdaUpdateWrapper<DeepayUserQuotaDO>()
                .eq(DeepayUserQuotaDO::getUserId, userId)
                .gt(DeepayUserQuotaDO::getFreeQuota, 0)
                .setSql("free_quota = free_quota - 1, used_quota = used_quota + 1")
                .set(DeepayUserQuotaDO::getUpdatedAt, LocalDateTime.now()));
        if (rows > 0) return rows;
        // 免费不足则扣付费额度
        return update(null, new LambdaUpdateWrapper<DeepayUserQuotaDO>()
                .eq(DeepayUserQuotaDO::getUserId, userId)
                .gt(DeepayUserQuotaDO::getPaidQuota, 0)
                .setSql("paid_quota = paid_quota - 1, used_quota = used_quota + 1")
                .set(DeepayUserQuotaDO::getUpdatedAt, LocalDateTime.now()));
    }

    /**
     * 为用户初始化配额记录（新用户 3 次免费额度）。
     * 使用 INSERT IGNORE 语义：若记录已存在则不操作。
     */
    default void initIfAbsent(String userId) {
        DeepayUserQuotaDO existing = selectByUserId(userId);
        if (existing == null) {
            DeepayUserQuotaDO quota = new DeepayUserQuotaDO();
            quota.setUserId(userId);
            quota.setFreeQuota(3);
            quota.setPaidQuota(0);
            quota.setUsedQuota(0);
            quota.setCreatedAt(LocalDateTime.now());
            quota.setUpdatedAt(LocalDateTime.now());
            insert(quota);
        }
    }

    /**
     * 新增付费额度（付费充值场景）。
     * 使用 MyBatis-Plus setSql 占位符避免 SQL 注入。
     *
     * @param userId 用户 ID
     * @param amount 充值次数（必须为正整数）
     */
    default void addPaidQuota(String userId, int amount) {
        if (amount <= 0) return;
        update(null, new LambdaUpdateWrapper<DeepayUserQuotaDO>()
                .eq(DeepayUserQuotaDO::getUserId, userId)
                .setSql("paid_quota = paid_quota + {0}", amount)
                .set(DeepayUserQuotaDO::getUpdatedAt, LocalDateTime.now()));
    }

}
