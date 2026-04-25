package cn.iocoder.yudao.module.deepay.dal.mongodb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * 聊天会话 Repository。
 */
public interface AiChatSessionRepository extends MongoRepository<AiChatSessionDocument, String> {

    /** 按租户 + 客户分页查询会话 */
    Page<AiChatSessionDocument> findByTenantIdAndCustomerIdOrderByLastActiveAtDesc(
            Long tenantId, Long customerId, Pageable pageable);

    /** 按租户 + 客户 + 板块查询 */
    Page<AiChatSessionDocument> findByTenantIdAndCustomerIdAndModuleOrderByLastActiveAtDesc(
            Long tenantId, Long customerId, String module, Pageable pageable);

    /** 按客户 ID 删除所有会话（合规清除） */
    void deleteByTenantIdAndCustomerId(Long tenantId, Long customerId);

    /** 统计某客户的会话数 */
    long countByTenantIdAndCustomerId(Long tenantId, Long customerId);

    Optional<AiChatSessionDocument> findById(String id);

}
