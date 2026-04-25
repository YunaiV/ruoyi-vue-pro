package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiMemoryItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 长久记忆 Mapper。
 */
@Mapper
public interface DeepayAiMemoryItemMapper extends BaseMapperX<DeepayAiMemoryItemDO> {

    /**
     * 查询用户在指定板块的所有记忆条目（未删除、未过期）。
     */
    default List<DeepayAiMemoryItemDO> selectByCustomerAndModule(
            Long tenantId, String customerId, String module) {
        return selectList(new LambdaQueryWrapper<DeepayAiMemoryItemDO>()
                .eq(DeepayAiMemoryItemDO::getTenantId, tenantId)
                .eq(DeepayAiMemoryItemDO::getCustomerId, customerId)
                .eq(DeepayAiMemoryItemDO::getModule, module)
                .eq(DeepayAiMemoryItemDO::getDeleted, 0)
                .and(w -> w.isNull(DeepayAiMemoryItemDO::getExpiresAt)
                        .or().gt(DeepayAiMemoryItemDO::getExpiresAt, LocalDateTime.now()))
                .orderByDesc(DeepayAiMemoryItemDO::getUpdatedAt));
    }

    /**
     * 查询用户所有记忆（跨板块，合规删除用）。
     */
    default List<DeepayAiMemoryItemDO> selectAllByCustomer(Long tenantId, String customerId) {
        return selectList(new LambdaQueryWrapper<DeepayAiMemoryItemDO>()
                .eq(DeepayAiMemoryItemDO::getTenantId, tenantId)
                .eq(DeepayAiMemoryItemDO::getCustomerId, customerId)
                .eq(DeepayAiMemoryItemDO::getDeleted, 0));
    }

    /**
     * Upsert 单条记忆：若 (tenantId, customerId, module, memKey) 已存在则更新，否则插入。
     */
    default void upsertMemory(DeepayAiMemoryItemDO item) {
        DeepayAiMemoryItemDO existing = selectOne(new LambdaQueryWrapper<DeepayAiMemoryItemDO>()
                .eq(DeepayAiMemoryItemDO::getTenantId, item.getTenantId())
                .eq(DeepayAiMemoryItemDO::getCustomerId, item.getCustomerId())
                .eq(DeepayAiMemoryItemDO::getModule, item.getModule())
                .eq(DeepayAiMemoryItemDO::getMemKey, item.getMemKey())
                .eq(DeepayAiMemoryItemDO::getDeleted, 0));
        if (existing == null) {
            insert(item);
        } else {
            update(null, new LambdaUpdateWrapper<DeepayAiMemoryItemDO>()
                    .eq(DeepayAiMemoryItemDO::getId, existing.getId())
                    .set(DeepayAiMemoryItemDO::getMemValue, item.getMemValue())
                    .set(DeepayAiMemoryItemDO::getConfidence, item.getConfidence())
                    .set(DeepayAiMemoryItemDO::getSourceSessionId, item.getSourceSessionId())
                    .set(DeepayAiMemoryItemDO::getExpiresAt, item.getExpiresAt())
                    .set(DeepayAiMemoryItemDO::getUpdatedAt, LocalDateTime.now()));
        }
    }

    /**
     * 软删除用户所有记忆（合规删除接口）。
     */
    default void softDeleteAllByCustomer(Long tenantId, String customerId) {
        update(null, new LambdaUpdateWrapper<DeepayAiMemoryItemDO>()
                .eq(DeepayAiMemoryItemDO::getTenantId, tenantId)
                .eq(DeepayAiMemoryItemDO::getCustomerId, customerId)
                .set(DeepayAiMemoryItemDO::getDeleted, 1)
                .set(DeepayAiMemoryItemDO::getUpdatedAt, LocalDateTime.now()));
    }

}
