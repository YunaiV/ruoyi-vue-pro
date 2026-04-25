package cn.iocoder.yudao.module.deepay.dal.mongodb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 工具调用审计日志 Repository。
 */
public interface AiToolAuditLogRepository extends MongoRepository<AiToolAuditLogDocument, String> {

    Page<AiToolAuditLogDocument> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<AiToolAuditLogDocument> findByTenantIdAndSessionIdOrderByCreatedAtDesc(
            Long tenantId, String sessionId, Pageable pageable);

}
