package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayCustomerProfileDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户画像 Mapper — 封装所有对 deepay_customer_profile 的操作。
 *
 * <p>验收条件：
 * <ul>
 *   <li>能存 — {@link #upsert} 幂等写入</li>
 *   <li>能查 — {@link #selectByCustomerId} 精确查询</li>
 *   <li>一个客户一条记录 — 由 unique(customer_id) 索引保证</li>
 * </ul>
 * </p>
 */
@Mapper
public interface DeepayCustomerProfileMapper extends BaseMapperX<DeepayCustomerProfileDO> {

    /** 按 customer_id 精确查询（一个客户一条记录）。 */
    default DeepayCustomerProfileDO selectByCustomerId(Long customerId) {
        return selectOne(new LambdaQueryWrapper<DeepayCustomerProfileDO>()
                .eq(DeepayCustomerProfileDO::getCustomerId, customerId));
    }

    /**
     * 幂等写入：不存在则 INSERT，存在则 UPDATE。
     * 使用标准 MyBatis-Plus 接口实现（不依赖 MySQL 方言）。
     */
    default void upsert(DeepayCustomerProfileDO profile) {
        DeepayCustomerProfileDO existing = selectByCustomerId(profile.getCustomerId());
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

    /**
     * 更新置信度分数（PreferenceLearningAgent 调用，每次 +delta，上限 1.0）。
     */
    default void incrementConfidence(Long customerId, BigDecimal delta) {
        update(null, new LambdaUpdateWrapper<DeepayCustomerProfileDO>()
                .eq(DeepayCustomerProfileDO::getCustomerId, customerId)
                .setSql("confidence_score = LEAST(1.0, confidence_score + " + delta.toPlainString() + ")")
                .set(DeepayCustomerProfileDO::getUpdatedAt, LocalDateTime.now()));
    }

    /**
     * 更新风格权重 JSON 字符串（PreferenceLearningAgent 已在 Java 层计算好新值）。
     */
    default void updateStyleWeights(Long customerId, String styleWeightsJson) {
        update(null, new LambdaUpdateWrapper<DeepayCustomerProfileDO>()
                .eq(DeepayCustomerProfileDO::getCustomerId, customerId)
                .set(DeepayCustomerProfileDO::getStyleWeights, styleWeightsJson)
                .set(DeepayCustomerProfileDO::getUpdatedAt, LocalDateTime.now()));
    }

}
