package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserProfileDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户画像 Mapper — 操作 deepay_user_profile。
 *
 * <p>验收：
 * <ul>
 *   <li>能存 — {@link #upsert} 幂等写入</li>
 *   <li>能查 — {@link #selectByUserId} 按 user_id 精确查询</li>
 *   <li>一个用户一条记录 — unique(user_id)</li>
 * </ul>
 * </p>
 */
@Mapper
public interface DeepayUserProfileMapper extends BaseMapperX<DeepayUserProfileDO> {

    /** 按 user_id 精确查询。 */
    default DeepayUserProfileDO selectByUserId(String userId) {
        return selectOne(new LambdaQueryWrapper<DeepayUserProfileDO>()
                .eq(DeepayUserProfileDO::getUserId, userId));
    }

    /**
     * 幂等写入：不存在则 INSERT，存在则 UPDATE。
     */
    default void upsert(DeepayUserProfileDO profile) {
        DeepayUserProfileDO existing = selectByUserId(profile.getUserId());
        if (existing == null) {
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            insert(profile);
        } else {
            profile.setId(existing.getId());
            profile.setUpdatedAt(LocalDateTime.now());
            updateById(profile);
        }
    }

    /** 更新置信度（+delta，上限 1.0）。 */
    default void incrementConfidence(String userId, BigDecimal delta) {
        update(null, new LambdaUpdateWrapper<DeepayUserProfileDO>()
                .eq(DeepayUserProfileDO::getUserId, userId)
                .setSql("confidence = LEAST(1.0, confidence + " + delta.toPlainString() + ")")
                .set(DeepayUserProfileDO::getUpdatedAt, LocalDateTime.now()));
    }

}
