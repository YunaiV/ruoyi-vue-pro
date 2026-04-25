package cn.iocoder.yudao.module.deepay.dal.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * 长久记忆 Repository。
 */
public interface AiMemoryItemRepository extends MongoRepository<AiMemoryItemDocument, String> {

    /** 查询某客户的所有记忆（所有板块） */
    List<AiMemoryItemDocument> findByTenantIdAndCustomerId(Long tenantId, Long customerId);

    /** 查询某客户某板块的记忆 */
    Optional<AiMemoryItemDocument> findByTenantIdAndCustomerIdAndModule(
            Long tenantId, Long customerId, String module);

    /** 按客户 ID 删除所有记忆（合规清除） */
    void deleteByTenantIdAndCustomerId(Long tenantId, Long customerId);

    /** 按客户 ID + 板块删除（用户选择性删除） */
    void deleteByTenantIdAndCustomerIdAndModule(Long tenantId, Long customerId, String module);

}
